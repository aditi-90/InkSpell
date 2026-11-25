package com.inkspell;

import static org.junit.jupiter.api.Assertions.*;

import com.inkspell.dao.Sql;
import com.inkspell.util.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Explicit destructive-demo test. Opt in only against a disposable local seeded database. */
@EnabledIfSystemProperty(named = "inkspell.cleanupTest", matches = "true")
class DemoCleanupTest {
  @Test
  void cleanupPreservesSavedBooksAndRestoresSeeds() throws Exception {
    String email = "cleanup-" + UUID.randomUUID() + "@example.invalid";
    long book =
        ((Number)
                Sql.query("SELECT id FROM books WHERE is_demo=1 ORDER BY id LIMIT 1")
                    .getFirst()
                    .get("id"))
            .longValue();
    try {
      Sql.update(
          "INSERT INTO users(full_name,email,password_hash) VALUES(?,?,?)",
          "Cleanup reader",
          email,
          Passwords.hash(UUID.randomUUID().toString() + "A1"));
      long user =
          ((Number) Sql.query("SELECT id FROM users WHERE email=?", email).getFirst().get("id"))
              .longValue();
      Sql.update("INSERT INTO user_library(user_id,book_id) VALUES(?,?)", user, book);
      ClearDemo.main(new String[0]);
      assertEquals(1, Sql.query("SELECT id FROM books WHERE id=?", book).size());
      assertTrue(
          Sql.query(
                  "SELECT id FROM books b WHERE b.is_demo=1 AND NOT EXISTS (SELECT 1 FROM"
                      + " user_library l WHERE l.book_id=b.id)")
              .isEmpty());
      assertTrue(Sql.query("SELECT id FROM site_testimonials WHERE is_placeholder=1").isEmpty());
    } finally {
      Sql.update("DELETE FROM users WHERE email=?", email);
      DatabaseSetup.main(new String[0]);
      var chosen = Sql.query("SELECT id FROM books WHERE isbn='9780307744432'");
      if (!chosen.isEmpty())
        new com.inkspell.service.AdminService()
            .feature(((Number) chosen.getFirst().get("id")).longValue());
    }
  }
}
