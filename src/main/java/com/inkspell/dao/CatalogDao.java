package com.inkspell.dao;

import com.inkspell.model.BookItem;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Access Object for InkSpell's book catalogue and user libraries.
 * Seamlessly integrates the hardcoded 108-book collection with MySQL persistence.
 */
public class CatalogDao {
  public static final String SELECT =
      "SELECT b.*,g.name genre,COALESCE((SELECT AVG(rating) FROM ratings r WHERE r.book_id=b.id),0)"
          + " avg_rating,(SELECT COUNT(*) FROM ratings r WHERE r.book_id=b.id) rating_count,(SELECT"
          + " COUNT(*) FROM user_library l WHERE l.book_id=b.id) popularity FROM books b JOIN"
          + " genres g ON b.genre_id=g.id";

  /**
   * Searches the catalogue with pagination.
   * Leverages the 108-book catalogue with full metadata.
   */
  public List<Map<String, Object>> search(String query, String genre, String sort, int page)
      throws SQLException {
    return search(query, genre, sort, page, 12);
  }

  /**
   * Searches the catalogue with custom page size.
   * If page <= 0, returns all matches without pagination.
   */
  public List<Map<String, Object>> search(
      String query, String genre, String sort, int page, int pageSize) throws SQLException {
    if (query != null && query.length() > 200)
      throw new IllegalArgumentException("Search must be 200 characters or fewer.");
    if (genre != null && genre.length() > 60)
      throw new IllegalArgumentException("Genre must be 60 characters or fewer.");

    List<BookItem> items = BookCatalogue.search(query, genre, sort, page, pageSize);
    return items.stream().map(BookItem::toMap).collect(Collectors.toList());
  }

  /**
   * Returns total matching book count for a given query and genre.
   */
  public int countMatches(String query, String genre) {
    return BookCatalogue.countMatches(query, genre);
  }

  /**
   * Retrieves a single book by ID, including sample reviews, tags, and language.
   */
  public Map<String, Object> book(long id) throws SQLException {
    Optional<BookItem> item = BookCatalogue.getById(id);
    if (item.isPresent()) {
      return item.get().toMap();
    }
    var rows = Sql.query(SELECT + " WHERE b.id=?", id);
    if (rows.isEmpty()) throw new IllegalArgumentException("That book could not be found.");
    return rows.getFirst();
  }

  /**
   * Returns all catalogue genres.
   */
  public List<Map<String, Object>> genres() throws SQLException {
    List<Map<String, Object>> list = new ArrayList<>();
    int id = 1;
    for (String g : BookCatalogue.getGenres()) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("id", id++);
      map.put("name", g);
      list.add(map);
    }
    return list;
  }

  /**
   * Retrieves a user's library entries, preserving their statuses and progress.
   */
  public List<Map<String, Object>> library(long user, String status, boolean bookmarks)
      throws SQLException {
    if (bookmarks)
      return Sql.query(
          SELECT
              + " JOIN bookmarks m ON m.book_id=b.id WHERE m.user_id=? ORDER BY m.created_at DESC",
          user);
    List<Map<String, Object>> rows =
        Sql.query(
            "SELECT b.*,g.name genre,l.*,b.id id,COALESCE(r.rating,0)"
                + " own_rating,COALESCE(l.total_pages_override,b.total_pages) total_pages,CASE WHEN"
                + " COALESCE(l.total_pages_override,b.total_pages)>0 THEN"
                + " ROUND(l.current_page*100.0/COALESCE(l.total_pages_override,b.total_pages)) ELSE"
                + " 0 END progress FROM user_library l JOIN books b ON b.id=l.book_id JOIN genres g"
                + " ON g.id=b.genre_id LEFT JOIN ratings r ON r.book_id=b.id AND r.user_id=l.user_id"
                + " WHERE l.user_id=? AND (?='' OR l.reading_status=?) ORDER BY l.updated_at DESC",
            user,
            status,
            status);

    // Enrich rows with catalogue metadata if available
    for (Map<String, Object> row : rows) {
      Object idObj = row.get("id");
      if (idObj instanceof Number num) {
        BookCatalogue.getById(num.longValue())
            .ifPresent(
                b -> {
                  row.putIfAbsent("short_description", b.getShortDescription());
                  row.putIfAbsent("tags", b.getTags());
                  row.putIfAbsent("language", b.getLanguage());
                });
      }
    }
    return rows;
  }
}
