package com.inkspell.service;

import com.inkspell.dao.Sql;
import com.inkspell.util.Database;
import java.sql.*;
import java.util.*;

public class AdminService {
  public void saveBook(Map<String, String> values) throws SQLException {
    String title = Validation.text(values.get("title"), 200, "Title"),
        author = Validation.text(values.get("author"), 150, "Author"),
        description = Validation.text(values.get("description"), 10000, "Description");
    int genre = Validation.integer(values.get("genreId"), 1, 1000, "Genre");
    String cover = Validation.cover(values.get("cover"));
    String isbn = values.get("isbn");
    if (isbn != null && !isbn.isBlank() && !isbn.matches("(?:[0-9]{13}|[0-9]{9}[0-9Xx])"))
      throw new IllegalArgumentException("ISBN must contain 10 or 13 characters, without hyphens.");
    Integer
        year =
            optional(
                values.get("year"), 1, java.time.Year.now().getValue() + 5, "Publication year"),
        pages = optional(values.get("pages"), 1, 100000, "Total pages");
    if ((cover == null) && (isbn != null && !isbn.isBlank()))
      cover = "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";
    boolean featured = "on".equals(values.get("featured"));
    long id =
        values.get("id") == null || values.get("id").isBlank()
            ? 0
            : Validation.integer(values.get("id"), 1, Integer.MAX_VALUE, "Book");
    try (Connection c = Database.open()) {
      c.setAutoCommit(false);
      try {
        // Serialize catalog changes so concurrent admin requests cannot create two featured books.
        Sql.query(c, "SELECT id FROM genres ORDER BY id FOR UPDATE");
        if (id > 0) {
          var existing = Sql.query(c, "SELECT id FROM books WHERE id=?", id);
          if (existing.isEmpty()) throw new IllegalArgumentException("Book not found.");
          if (pages != null
              && !Sql.query(
                      c,
                      "SELECT id FROM user_library WHERE book_id=? AND current_page>? LIMIT 1",
                      id,
                      pages)
                  .isEmpty())
            throw new IllegalArgumentException(
                "Page count cannot be lower than a reader's recorded progress.");
        }
        if (featured) Sql.update(c, "UPDATE books SET is_featured=0 WHERE is_featured=1");
        if (id == 0)
          Sql.update(
              c,
              "INSERT INTO"
                  + " books(title,author,genre_id,description,cover_image_url,isbn,publication_year,total_pages,is_featured)"
                  + " VALUES(?,?,?,?,?,?,?,?,?)",
              title,
              author,
              genre,
              description,
              cover,
              isbn,
              year,
              pages,
              featured);
        else
          Sql.update(
              c,
              "UPDATE books SET"
                  + " title=?,author=?,genre_id=?,description=?,cover_image_url=?,isbn=?,publication_year=?,total_pages=?,is_featured=?"
                  + " WHERE id=?",
              title,
              author,
              genre,
              description,
              cover,
              isbn,
              year,
              pages,
              featured,
              id);
        if (Sql.query(c, "SELECT id FROM books WHERE is_featured=1 LIMIT 1").isEmpty())
          Sql.update(
              c,
              "UPDATE books SET is_featured=1 WHERE id=(SELECT id FROM (SELECT id FROM books ORDER"
                  + " BY id LIMIT 1) first_book)");
        c.commit();
      } catch (Exception e) {
        c.rollback();
        throw e;
      }
    }
  }

  private Integer optional(String v, int min, int max, String label) {
    return v == null || v.isBlank() ? null : Validation.integer(v, min, max, label);
  }

  public void feature(long id) throws SQLException {
    try (Connection c = Database.open()) {
      c.setAutoCommit(false);
      try {
        Sql.query(c, "SELECT id FROM genres ORDER BY id FOR UPDATE");
        if (Sql.query(c, "SELECT id FROM books WHERE id=?", id).isEmpty())
          throw new IllegalArgumentException("Book not found.");
        Sql.update(c, "UPDATE books SET is_featured=0 WHERE is_featured=1");
        Sql.update(c, "UPDATE books SET is_featured=1 WHERE id=?", id);
        c.commit();
      } catch (Exception e) {
        c.rollback();
        throw e;
      }
    }
  }
}
