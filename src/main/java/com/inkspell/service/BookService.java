package com.inkspell.service;

import com.inkspell.dao.*;
import com.inkspell.model.BookItem;
import com.inkspell.util.Database;
import java.sql.*;
import java.time.Year;
import java.util.*;

/**
 * Service for adding new books to InkSpell's catalogue and personal collections,
 * with robust duplicate prevention, input validation, and transactional persistence.
 */
public class BookService {

  public static final List<String> SUPPORTED_GENRES =
      List.of(
          "Fantasy",
          "Mystery",
          "Thriller",
          "Horror",
          "Romance",
          "Dark Romance",
          "Science Fiction",
          "Contemporary Fiction",
          "Historical Fiction",
          "Young Adult",
          "Gothic Fiction",
          "Psychological Thriller",
          "Literature",
          "Classics",
          "Other");

  public static List<String> getAvailableGenres() {
    try {
      var rows = Sql.query("SELECT name FROM genres ORDER BY name");
      if (!rows.isEmpty()) {
        List<String> list = new ArrayList<>();
        for (var row : rows) {
          list.add(String.valueOf(row.get("name")));
        }
        return list;
      }
    } catch (Exception ignored) {}
    return SUPPORTED_GENRES;
  }

  public static com.inkspell.model.ReadingStatus parseReadingStatus(String rawStatus) {
    if (rawStatus == null || rawStatus.isBlank()) {
      return com.inkspell.model.ReadingStatus.WANT_TO_READ;
    }
    String normalized = rawStatus.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    if ("TO_READ".equals(normalized) || "WANT_TO_READ".equals(normalized)) {
      return com.inkspell.model.ReadingStatus.WANT_TO_READ;
    }
    if ("CURRENTLY_READING".equals(normalized) || "READING".equals(normalized)) {
      return com.inkspell.model.ReadingStatus.CURRENTLY_READING;
    }
    if ("COMPLETED".equals(normalized) || "FINISHED".equals(normalized) || "READ".equals(normalized)) {
      return com.inkspell.model.ReadingStatus.COMPLETED;
    }
    try {
      return com.inkspell.model.ReadingStatus.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      return com.inkspell.model.ReadingStatus.WANT_TO_READ;
    }
  }

  public enum ResultStatus {
    CREATED_IN_COLLECTION,
    CREATED_IN_CATALOGUE,
    EXISTING_ADDED_TO_COLLECTION,
    ALREADY_IN_COLLECTION,
    EXISTING_IN_CATALOGUE
  }

  public static class BookResult {
    private final ResultStatus status;
    private final String message;
    private final long bookId;
    private final Map<String, Object> book;

    public BookResult(ResultStatus status, String message, long bookId, Map<String, Object> book) {
      this.status = status;
      this.message = message;
      this.bookId = bookId;
      this.book = book;
    }

    public ResultStatus getStatus() {
      return status;
    }

    public String getMessage() {
      return message;
    }

    public long getBookId() {
      return bookId;
    }

    public Map<String, Object> getBook() {
      return book;
    }

    public boolean isSuccess() {
      return status == ResultStatus.CREATED_IN_COLLECTION
          || status == ResultStatus.CREATED_IN_CATALOGUE
          || status == ResultStatus.EXISTING_ADDED_TO_COLLECTION;
    }

    public boolean isDuplicateNotice() {
      return status == ResultStatus.ALREADY_IN_COLLECTION
          || status == ResultStatus.EXISTING_IN_CATALOGUE;
    }
  }

  /**
   * Checks whether a book already exists in InkSpell by title and author (or consistent ISBN).
   * A completely different title will never trigger a false duplicate match.
   */
  public Optional<Map<String, Object>> findDuplicate(String isbn, String title, String author)
      throws SQLException {
    String normTitle = normalize(title);
    String normAuthor = normalize(author);

    if (normTitle.isEmpty()) {
      return Optional.empty();
    }

    // 1. Primary check: Genuine Title + Author match in MySQL books table
    if (!normAuthor.isEmpty()) {
      var rows =
          Sql.query(
              CatalogDao.SELECT
                  + " WHERE LOWER(TRIM(b.title)) = ? AND LOWER(TRIM(b.author)) = ? ORDER BY b.id ASC LIMIT 1",
              normTitle,
              normAuthor);
      if (!rows.isEmpty()) {
        return Optional.of(rows.getFirst());
      }

      // Also check in-memory catalogue for title + author
      for (BookItem b : BookCatalogue.getAll()) {
        if (normalize(b.getTitle()).equals(normTitle)
            && normalize(b.getAuthor()).equals(normAuthor)) {
          return Optional.of(b.toMap());
        }
      }
    } else {
      // If author is unspecified, check by title alone in MySQL
      var rows =
          Sql.query(
              CatalogDao.SELECT
                  + " WHERE LOWER(TRIM(b.title)) = ? ORDER BY b.id ASC LIMIT 1",
              normTitle);
      if (!rows.isEmpty()) {
        return Optional.of(rows.getFirst());
      }
    }

    // 2. Secondary check: ISBN match ONLY when title also genuinely aligns
    // Under no circumstances should an unrelated book match merely due to an ISBN.
    if (isbn != null && !isbn.isBlank()) {
      String cleanIsbn = isbn.replaceAll("[^0-9Xx]", "").toUpperCase(Locale.ROOT);
      if (cleanIsbn.length() == 10 || cleanIsbn.length() == 13) {
        var rows =
            Sql.query(
                CatalogDao.SELECT
                    + " WHERE REPLACE(REPLACE(b.isbn, '-', ''), ' ', '') = ? ORDER BY b.id ASC LIMIT 1",
                cleanIsbn);
        if (!rows.isEmpty()) {
          Map<String, Object> existing = rows.getFirst();
          String existingTitle = normalize(String.valueOf(existing.get("title")));
          if (existingTitle.equals(normTitle) || existingTitle.contains(normTitle) || normTitle.contains(existingTitle)) {
            return Optional.of(existing);
          }
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Verifies if a book is already present in a specific user's collection.
   */
  public boolean isBookInUserCollection(long userId, long bookId) throws SQLException {
    var rows =
        Sql.query(
            "SELECT 1 FROM user_library WHERE user_id=? AND book_id=? LIMIT 1", userId, bookId);
    return !rows.isEmpty();
  }

  /**
   * Retrieves an existing genre ID or creates a new entry in the MySQL `genres` table.
   */
  public int getOrCreateGenre(String genreName) throws SQLException {
    String cleanGenre = Validation.text(genreName, 60, "Genre");
    var rows = Sql.query("SELECT id FROM genres WHERE LOWER(name) = LOWER(?) LIMIT 1", cleanGenre);
    if (!rows.isEmpty()) {
      return ((Number) rows.getFirst().get("id")).intValue();
    }
    try (Connection c = Database.open()) {
      try (PreparedStatement ps =
          c.prepareStatement(
              "INSERT INTO genres(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, cleanGenre);
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
          if (rs.next()) {
            return rs.getInt(1);
          }
        }
      }
    }
    // Fallback query if generated keys weren't returned
    var refetch =
        Sql.query("SELECT id FROM genres WHERE LOWER(name) = LOWER(?) LIMIT 1", cleanGenre);
    if (!refetch.isEmpty()) {
      return ((Number) refetch.getFirst().get("id")).intValue();
    }
    return 1;
  }

  /**
   * Validates and persists a new book, with duplicate prevention and optional collection attachment.
   * Default reading status is WANT_TO_READ for backwards compatibility.
   */
  public BookResult addBook(
      long userId,
      String rawTitle,
      String rawAuthor,
      String rawGenre,
      String rawDescription,
      String rawCoverUrl,
      String rawIsbn,
      String rawYear,
      String rawPages,
      boolean addToCollection)
      throws SQLException {
    return addBook(
        userId,
        rawTitle,
        rawAuthor,
        rawGenre,
        rawDescription,
        rawCoverUrl,
        rawIsbn,
        rawYear,
        rawPages,
        "WANT_TO_READ",
        addToCollection);
  }

  /**
   * Validates and permanently persists a new book into MySQL, associating it with the current user's
   * collection under their chosen reading status (WANT_TO_READ, CURRENTLY_READING, or COMPLETED).
   */
  public BookResult addBook(
      long userId,
      String rawTitle,
      String rawAuthor,
      String rawGenre,
      String rawDescription,
      String rawCoverUrl,
      String rawIsbn,
      String rawYear,
      String rawPages,
      String rawStatus,
      boolean addToCollection)
      throws SQLException {

    // 1. Validate required fields
    String title = Validation.text(rawTitle, 200, "Book title");
    String author = Validation.text(rawAuthor, 150, "Author name");
    String genre = Validation.text(rawGenre, 60, "Genre");
    String description = Validation.text(rawDescription, 5000, "Description");

    // 2. Validate optional fields
    String cleanIsbn = null;
    if (rawIsbn != null && !rawIsbn.isBlank()) {
      cleanIsbn = rawIsbn.replaceAll("[^0-9Xx]", "");
      if (cleanIsbn.length() != 10 && cleanIsbn.length() != 13) {
        throw new IllegalArgumentException(
            "ISBN must be 10 or 13 digits (hyphens are permitted, e.g. 978-0-547-92822-7).");
      }
    }

    Integer pubYear = null;
    if (rawYear != null && !rawYear.isBlank()) {
      pubYear = Validation.integer(rawYear.trim(), 1000, Year.now().getValue() + 5, "Publication year");
    }

    Integer totalPages = null;
    if (rawPages != null && !rawPages.isBlank()) {
      totalPages = Validation.integer(rawPages.trim(), 1, 100000, "Total number of pages");
    }

    String coverUrl = null;
    if (rawCoverUrl != null && !rawCoverUrl.isBlank()) {
      String trimmedCover = rawCoverUrl.trim();
      if (trimmedCover.length() > 1000) {
        throw new IllegalArgumentException("Cover image URL cannot exceed 1,000 characters.");
      }
      if (!trimmedCover.startsWith("http://")
          && !trimmedCover.startsWith("https://")
          && !trimmedCover.startsWith("data:image/")) {
        throw new IllegalArgumentException(
            "Please provide a valid web image URL (starting with https:// or http://).");
      }
      coverUrl = trimmedCover;
    } else if (cleanIsbn != null && cleanIsbn.length() == 13) {
      // Automatic fallback to Open Library if ISBN is provided
      coverUrl = "https://covers.openlibrary.org/b/isbn/" + cleanIsbn + "-L.jpg";
    }

    // 3. Resolve Reading Status
    com.inkspell.model.ReadingStatus status = parseReadingStatus(rawStatus);

    // 4. Duplicate Prevention
    Optional<Map<String, Object>> duplicate = findDuplicate(cleanIsbn, title, author);

    if (duplicate.isPresent()) {
      Map<String, Object> existing = duplicate.get();
      long existingId = ((Number) existing.get("id")).longValue();
      boolean inCollection = isBookInUserCollection(userId, existingId);

      if (inCollection) {
        return new BookResult(
            ResultStatus.ALREADY_IN_COLLECTION,
            "\"" + existing.get("title") + "\" by " + existing.get("author")
                + " already exists in your personal collection.",
            existingId,
            existing);
      }

      if (addToCollection) {
        int curPage = (status == com.inkspell.model.ReadingStatus.COMPLETED && totalPages != null) ? totalPages : 0;
        java.sql.Date startDate = (status == com.inkspell.model.ReadingStatus.CURRENTLY_READING || status == com.inkspell.model.ReadingStatus.COMPLETED)
            ? new java.sql.Date(System.currentTimeMillis()) : null;
        java.sql.Date compDate = (status == com.inkspell.model.ReadingStatus.COMPLETED)
            ? new java.sql.Date(System.currentTimeMillis()) : null;

        Sql.update(
            "INSERT INTO user_library(user_id, book_id, reading_status, current_page, date_started, date_completed) VALUES(?, ?, ?, ?, ?, ?)",
            userId,
            existingId,
            status.name(),
            curPage,
            startDate,
            compDate);

        try {
          String actType = status == com.inkspell.model.ReadingStatus.COMPLETED ? "COMPLETED" : (status == com.inkspell.model.ReadingStatus.CURRENTLY_READING ? "STARTED" : "SHELVED");
          String actDesc = status == com.inkspell.model.ReadingStatus.COMPLETED
              ? "Completed reading \"" + existing.get("title") + "\""
              : (status == com.inkspell.model.ReadingStatus.CURRENTLY_READING
                  ? "Began reading \"" + existing.get("title") + "\""
                  : "Placed \"" + existing.get("title") + "\" on personal shelves");
          ReadingDashboardService.recordActivity(userId, existingId, actType, actDesc);
        } catch (Exception ignored) {}

        return new BookResult(
            ResultStatus.EXISTING_ADDED_TO_COLLECTION,
            "\"" + existing.get("title") + "\" was already recorded in our archives and has now been added to your collection!",
            existingId,
            existing);
      } else {
        return new BookResult(
            ResultStatus.EXISTING_IN_CATALOGUE,
            "\"" + existing.get("title") + "\" by " + existing.get("author")
                + " already exists in the library book catalog.",
            existingId,
            existing);
      }
    }

    // 5. Persistence in MySQL
    int genreId = getOrCreateGenre(genre);
    long newBookId;

    try (Connection c = Database.open()) {
      c.setAutoCommit(false);
      try {
        String insertSql =
            "INSERT INTO books(title, author, genre_id, description, cover_image_url, isbn, publication_year, total_pages, is_featured, is_demo)"
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, 0, 0)";
        try (PreparedStatement ps = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
          ps.setString(1, title);
          ps.setString(2, author);
          ps.setInt(3, genreId);
          ps.setString(4, description);
          if (coverUrl != null && !coverUrl.isBlank()) {
            ps.setString(5, coverUrl);
          } else {
            ps.setNull(5, Types.VARCHAR);
          }
          if (cleanIsbn != null && !cleanIsbn.isBlank()) {
            ps.setString(6, cleanIsbn);
          } else {
            ps.setNull(6, Types.VARCHAR);
          }
          if (pubYear != null) {
            ps.setInt(7, pubYear);
          } else {
            ps.setNull(7, Types.INTEGER);
          }
          if (totalPages != null) {
            ps.setInt(8, totalPages);
          } else {
            ps.setNull(8, Types.INTEGER);
          }
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              newBookId = rs.getLong(1);
            } else {
              throw new SQLException("Failed to obtain generated ID for new book.");
            }
          }
        }

        // Directly link book to user's collection with the chosen reading status
        if (addToCollection) {
          int curPage = (status == com.inkspell.model.ReadingStatus.COMPLETED && totalPages != null) ? totalPages : 0;
          java.sql.Date startDate = (status == com.inkspell.model.ReadingStatus.CURRENTLY_READING || status == com.inkspell.model.ReadingStatus.COMPLETED)
              ? new java.sql.Date(System.currentTimeMillis()) : null;
          java.sql.Date compDate = (status == com.inkspell.model.ReadingStatus.COMPLETED)
              ? new java.sql.Date(System.currentTimeMillis()) : null;

          try (PreparedStatement psLib =
              c.prepareStatement(
                  "INSERT INTO user_library(user_id, book_id, reading_status, current_page, date_started, date_completed) VALUES(?, ?, ?, ?, ?, ?)")) {
            psLib.setLong(1, userId);
            psLib.setLong(2, newBookId);
            psLib.setString(3, status.name());
            psLib.setInt(4, curPage);
            psLib.setDate(5, startDate);
            psLib.setDate(6, compDate);
            psLib.executeUpdate();
          }
        }

        c.commit();
      } catch (Exception ex) {
        c.rollback();
        throw ex;
      }
    }

    // Verify persistence directly from MySQL
    var verifyRows = Sql.query("SELECT id FROM books WHERE id=?", newBookId);
    if (verifyRows.isEmpty()) {
      throw new SQLException("Verification failed: book was not persisted in database.");
    }

    if (addToCollection) {
      try {
        String actType = status == com.inkspell.model.ReadingStatus.COMPLETED ? "COMPLETED" : (status == com.inkspell.model.ReadingStatus.CURRENTLY_READING ? "STARTED" : "SHELVED");
        String actDesc = status == com.inkspell.model.ReadingStatus.COMPLETED
            ? "Completed reading \"" + title + "\""
            : (status == com.inkspell.model.ReadingStatus.CURRENTLY_READING
                ? "Began reading \"" + title + "\""
                : "Inscribed new story \"" + title + "\" into collection");
        ReadingDashboardService.recordActivity(userId, newBookId, actType, actDesc);
      } catch (Exception ignored) {}
    }

    // 6. In-Memory Catalogue Registration
    BookItem newItem =
        new BookItem(
            newBookId,
            title,
            author,
            genre,
            description,
            coverUrl != null ? coverUrl : "",
            cleanIsbn != null ? cleanIsbn : "",
            0.0,
            0,
            pubYear != null ? pubYear : 0,
            totalPages != null ? totalPages : 0,
            "English",
            List.of(genre),
            false,
            1,
            Collections.emptyList());
    BookCatalogue.addCustomBook(newItem);

    Map<String, Object> bookMap = newItem.toMap();

    if (addToCollection) {
      return new BookResult(
          ResultStatus.CREATED_IN_COLLECTION,
          "Your new story has found its place on the shelf!",
          newBookId,
          bookMap);
    } else {
      return new BookResult(
          ResultStatus.CREATED_IN_CATALOGUE,
          "The new tome has been safely inscribed into the library catalogue!",
          newBookId,
          bookMap);
    }
  }

  private static String normalize(String s) {
    if (s == null) return "";
    return s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }
}
