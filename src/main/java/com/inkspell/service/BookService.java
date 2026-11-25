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
    return SUPPORTED_GENRES;
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
   * Checks whether a book already exists in InkSpell by ISBN or normalized (title + author).
   */
  public Optional<Map<String, Object>> findDuplicate(String isbn, String title, String author)
      throws SQLException {
    // 1. Primary check: ISBN
    if (isbn != null && !isbn.isBlank()) {
      String cleanIsbn = isbn.replaceAll("[^0-9Xx]", "").toUpperCase(Locale.ROOT);
      if (!cleanIsbn.isEmpty()) {
        // In-memory catalogue check
        for (BookItem b : BookCatalogue.getAll()) {
          if (b.getIsbn() != null) {
            String bIsbn = b.getIsbn().replaceAll("[^0-9Xx]", "").toUpperCase(Locale.ROOT);
            if (bIsbn.equals(cleanIsbn)) {
              return Optional.of(b.toMap());
            }
          }
        }
        // MySQL check
        var rows =
            Sql.query(
                CatalogDao.SELECT
                    + " WHERE REPLACE(REPLACE(b.isbn, '-', ''), ' ', '') = ? LIMIT 1",
                cleanIsbn);
        if (!rows.isEmpty()) {
          return Optional.of(rows.getFirst());
        }
      }
    }

    // 2. Secondary check: Normalized Title + Author
    String normTitle = normalize(title);
    String normAuthor = normalize(author);

    if (!normTitle.isEmpty() && !normAuthor.isEmpty()) {
      // In-memory catalogue check
      for (BookItem b : BookCatalogue.getAll()) {
        if (normalize(b.getTitle()).equals(normTitle)
            && normalize(b.getAuthor()).equals(normAuthor)) {
          return Optional.of(b.toMap());
        }
      }
      // MySQL check
      var rows =
          Sql.query(
              CatalogDao.SELECT
                  + " WHERE LOWER(TRIM(b.title)) = ? AND LOWER(TRIM(b.author)) = ? LIMIT 1",
              normTitle,
              normAuthor);
      if (!rows.isEmpty()) {
        return Optional.of(rows.getFirst());
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

    // 3. Duplicate Prevention
    Optional<Map<String, Object>> duplicate = findDuplicate(cleanIsbn, title, author);

    if (duplicate.isPresent()) {
      Map<String, Object> existing = duplicate.get();
      long existingId = ((Number) existing.get("id")).longValue();
      boolean inCollection = isBookInUserCollection(userId, existingId);

      if (inCollection) {
        return new BookResult(
            ResultStatus.ALREADY_IN_COLLECTION,
            "\"" + existing.get("title") + "\" by " + existing.get("author")
                + " is already resting on your collection shelf.",
            existingId,
            existing);
      }

      if (addToCollection) {
        // Book exists in catalogue; directly place onto user's shelf without duplicating the book
        Sql.update(
            "INSERT IGNORE INTO user_library(user_id, book_id, reading_status, current_page) VALUES(?, ?, 'WANT_TO_READ', 0)",
            userId,
            existingId);
        try {
          ReadingDashboardService.recordActivity(
              userId, existingId, "SHELVED", "Placed \"" + existing.get("title") + "\" on personal shelves");
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
                + " is already inscribed in the library catalogue.",
            existingId,
            existing);
      }
    }

    // 4. Persistence in MySQL
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

        // If requested, directly link book to user's collection with 'WANT_TO_READ' status
        if (addToCollection) {
          try (PreparedStatement psLib =
              c.prepareStatement(
                  "INSERT INTO user_library(user_id, book_id, reading_status, current_page) VALUES(?, ?, 'WANT_TO_READ', 0)")) {
            psLib.setLong(1, userId);
            psLib.setLong(2, newBookId);
            psLib.executeUpdate();
          }
        }

        c.commit();
      } catch (Exception ex) {
        c.rollback();
        throw ex;
      }
    }

    if (addToCollection) {
      try {
        ReadingDashboardService.recordActivity(
            userId, newBookId, "SHELVED", "Inscribed new story \"" + title + "\" into collection");
      } catch (Exception ignored) {}
    }

    // 5. In-Memory Catalogue Registration
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
