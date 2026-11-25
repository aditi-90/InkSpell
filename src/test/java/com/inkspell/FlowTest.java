package com.inkspell;

import static org.junit.jupiter.api.Assertions.*;

import com.inkspell.dao.*;
import com.inkspell.service.RecommendationService;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Exercises real HTTP sessions against the running WAR and its configured MySQL database. */
@EnabledIfSystemProperty(named = "inkspell.integration", matches = "true")
class FlowTest {
  static class Reader {
    HttpClient client =
        HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    String csrf;

    HttpResponse<String> get(String path) throws Exception {
      var result =
          client.send(
              HttpRequest.newBuilder(URI.create("http://localhost:8080" + path)).GET().build(),
              HttpResponse.BodyHandlers.ofString());
      Matcher m =
          Pattern.compile("name=\"csrf-token\" content=\"([^\"]+)\"").matcher(result.body());
      if (m.find()) csrf = m.group(1);
      return result;
    }

    HttpResponse<String> post(String path, String... values) throws Exception {
      Map<String, String> data = new LinkedHashMap<>();
      for (int i = 0; i < values.length; i += 2) data.put(values[i], values[i + 1]);
      if (csrf != null) data.put("csrf", csrf);
      String body =
          data.entrySet().stream()
              .map(
                  e ->
                      URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                          + "="
                          + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
              .reduce((a, b) -> a + "&" + b)
              .orElse("");
      return client.send(
          HttpRequest.newBuilder(URI.create("http://localhost:8080" + path))
              .header("Content-Type", "application/x-www-form-urlencoded")
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build(),
          HttpResponse.BodyHandlers.ofString());
    }
  }

  @Test
  void realReadingAndAdminFlows() throws Exception {
    String suffix = UUID.randomUUID().toString().substring(0, 8),
        email = "flow-" + suffix + "@example.invalid",
        otherEmail = "other-" + suffix + "@example.invalid",
        password = "Read" + UUID.randomUUID() + "9";
    Reader a = new Reader(), b = new Reader();
    long book =
        ((Number) Sql.query("SELECT id FROM books ORDER BY id LIMIT 1").getFirst().get("id"))
            .longValue();
    long originalFeatured =
        ((Number)
                Sql.query("SELECT id FROM books ORDER BY is_featured DESC,id LIMIT 1")
                    .getFirst()
                    .get("id"))
            .longValue();
    long testBook = 0;
    try {
      assertEquals(200, a.get("/").statusCode());
      assertEquals(200, a.get("/css/app.css").statusCode());
      assertEquals(404, a.get("/no-such-door").statusCode());
      assertEquals(302, b.get("/library").statusCode());
      assertEquals(
          403, new Reader().post("/login", "email", email, "password", password).statusCode());
      a.get("/register");
      assertTrue(
          a.post(
                  "/register",
                  "fullName",
                  "Reader",
                  "email",
                  "bad",
                  "password",
                  "short",
                  "confirmPassword",
                  "short")
              .body()
              .contains("valid email"));
      assertEquals(
          302,
          a.post(
                  "/register",
                  "fullName",
                  "Test Reader",
                  "email",
                  email,
                  "password",
                  password,
                  "confirmPassword",
                  password)
              .statusCode());
      assertEquals(200, a.get("/dashboard").statusCode());
      b.get("/register");
      assertTrue(
          b.post(
                  "/register",
                  "fullName",
                  "Duplicate",
                  "email",
                  email,
                  "password",
                  password,
                  "confirmPassword",
                  password)
              .body()
              .contains("already registered"));
      assertEquals(
          302,
          b.post(
                  "/register",
                  "fullName",
                  "Other Reader",
                  "email",
                  otherEmail,
                  "password",
                  password,
                  "confirmPassword",
                  password)
              .statusCode());
      assertEquals(200, b.get("/preferences").statusCode());
      assertEquals(403, b.get("/admin").statusCode());
      assertTrue(a.get("/discover?q=Hobbit").body().contains("The Hobbit"));
      assertTrue(a.get("/discover?q=Tolkien").body().contains("The Hobbit"));
      assertTrue(a.get("/discover?genre=Fantasy&sort=rating").body().contains("The Hobbit"));
      assertTrue(a.get("/discover?q=nosuchbookabcdef").body().contains("No stories found"));
      assertEquals(200, a.get("/discover?page=2&sort=recent").statusCode());
      for (String sort : List.of("title", "author", "year", "rating", "popular", "recent"))
        assertEquals(200, a.get("/discover?sort=" + sort).statusCode());
      assertEquals(200, a.post("/api/action", "action", "shelf", "bookId", "" + book).statusCode());
      assertEquals(200, a.post("/api/action", "action", "shelf", "bookId", "" + book).statusCode());
      long
          user =
              ((Number) Sql.query("SELECT id FROM users WHERE email=?", email).getFirst().get("id"))
                  .longValue(),
          other =
              ((Number)
                      Sql.query("SELECT id FROM users WHERE email=?", otherEmail)
                          .getFirst()
                          .get("id"))
                  .longValue();
      assertEquals(
          1,
          ((Number)
                  Sql.query(
                          "SELECT COUNT(*) n FROM user_library WHERE user_id=? AND book_id=?",
                          user,
                          book)
                      .getFirst()
                      .get("n"))
              .intValue());
      assertEquals(
          200,
          a.post(
                  "/api/action",
                  "action",
                  "save",
                  "bookId",
                  "" + book,
                  "status",
                  "CURRENTLY_READING",
                  "page",
                  "12",
                  "totalPages",
                  "100",
                  "notes",
                  "<script>alert('private')</script>")
              .statusCode());
      assertEquals(
          400,
          a.post(
                  "/api/action",
                  "action",
                  "save",
                  "bookId",
                  "" + book,
                  "status",
                  "CURRENTLY_READING",
                  "page",
                  "101")
              .statusCode());
      assertEquals(
          12,
          ((Number) new CatalogDao().library(user, "", false).getFirst().get("progress"))
              .intValue());
      assertEquals(
          400,
          a.post(
                  "/api/action",
                  "action",
                  "save",
                  "bookId",
                  "" + book,
                  "status",
                  "CURRENTLY_READING",
                  "page",
                  "-1")
              .statusCode());
      String detail = a.get("/book?id=" + book).body();
      assertTrue(detail.contains("&lt;script&gt;"));
      assertFalse(b.get("/book?id=" + book).body().contains("alert('private')"));
      assertEquals(
          200, b.post("/api/action", "action", "remove", "bookId", "" + book).statusCode());
      assertEquals(
          1,
          Sql.query("SELECT id FROM user_library WHERE user_id=? AND book_id=?", user, book)
              .size());
      assertEquals(
          200,
          a.post(
                  "/api/action",
                  "action",
                  "save",
                  "bookId",
                  "" + book,
                  "status",
                  "COMPLETED",
                  "page",
                  "12",
                  "notes",
                  "My reading notes")
              .statusCode());
      assertNotNull(
          Sql.query(
                  "SELECT date_completed FROM user_library WHERE user_id=? AND book_id=?",
                  user,
                  book)
              .getFirst()
              .get("date_completed"));
      assertEquals(
          100,
          ((Number) new CatalogDao().library(user, "", false).getFirst().get("progress"))
              .intValue());
      for (String rating : List.of("4", "5"))
        assertEquals(
            200,
            a.post("/api/action", "action", "rate", "bookId", "" + book, "rating", rating)
                .statusCode());
      assertEquals(
          400,
          a.post("/api/action", "action", "rate", "bookId", "" + book, "rating", "6").statusCode());
      assertEquals(
          200,
          b.post("/api/action", "action", "rate", "bookId", "" + book, "rating", "3").statusCode());
      assertEquals(
          4.0,
          ((Number)
                  Sql.query(
                          "SELECT AVG(rating) avg FROM ratings WHERE book_id=? AND user_id IN"
                              + " (?,?)",
                          book,
                          user,
                          other)
                      .getFirst()
                      .get("avg"))
              .doubleValue());
      a.post("/api/action", "action", "bookmark", "bookId", "" + book);
      assertEquals(
          1,
          Sql.query("SELECT id FROM bookmarks WHERE user_id=? AND book_id=?", user, book).size());
      a.post("/api/action", "action", "bookmark", "bookId", "" + book);
      assertTrue(
          Sql.query("SELECT id FROM bookmarks WHERE user_id=? AND book_id=?", user, book)
              .isEmpty());
      int genre =
          ((Number)
                  Sql.query("SELECT genre_id FROM books WHERE id=?", book)
                      .getFirst()
                      .get("genre_id"))
              .intValue();
      assertEquals(
          200, a.post("/api/action", "action", "preferences", "genres", "" + genre).statusCode());
      assertTrue(
          new RecommendationService()
              .recommend(user).stream()
                  .noneMatch(row -> ((Number) row.get("id")).longValue() == book));
      for (String path :
          List.of(
              "/library",
              "/library?status=COMPLETED",
              "/bookmarks",
              "/profile",
              "/assistant",
              "/preferences",
              "/dashboard")) assertEquals(200, a.get(path).statusCode(), path);
      assertEquals(400, a.post("/api/chat", "message", "").statusCode());
      assertTrue(
          a.post("/api/chat", "message", "Suggest a fantasy book")
              .body()
              .contains("Gemini API key"));
      assertEquals(200, a.post("/api/chat", "reset", "true").statusCode());
      assertEquals(
          200, a.post("/api/action", "action", "profile", "fullName", "<Reader>").statusCode());
      assertTrue(a.get("/profile").body().contains("&lt;Reader&gt;"));
      assertEquals(
          400,
          a.post(
                  "/api/action",
                  "action",
                  "password",
                  "currentPassword",
                  "wrong",
                  "newPassword",
                  password)
              .statusCode());
      String newPassword = "Next" + UUID.randomUUID() + "8";
      assertEquals(
          200,
          a.post(
                  "/api/action",
                  "action",
                  "password",
                  "currentPassword",
                  password,
                  "newPassword",
                  newPassword)
              .statusCode());
      password = newPassword;
      Sql.update("UPDATE users SET role='ADMIN' WHERE id=?", user);
      a.post("/logout");
      a.get("/login");
      assertTrue(
          a.post("/login", "email", email, "password", "wrong")
              .body()
              .contains("Email or password is incorrect"));
      assertEquals(302, a.post("/login", "email", email, "password", password).statusCode());
      assertEquals(200, a.get("/admin").statusCode());
      assertEquals(
          302,
          a.post(
                  "/admin",
                  "action",
                  "book",
                  "title",
                  "Flow Tome " + suffix,
                  "author",
                  "Test Author",
                  "description",
                  "A disposable test record.",
                  "genreId",
                  "" + genre,
                  "pages",
                  "100",
                  "featured",
                  "on")
              .statusCode());
      testBook =
          ((Number)
                  Sql.query("SELECT id FROM books WHERE title=?", "Flow Tome " + suffix)
                      .getFirst()
                      .get("id"))
              .longValue();
      assertEquals(1, Sql.query("SELECT id FROM books WHERE is_featured=1").size());
      assertEquals(
          302,
          a.post(
                  "/admin",
                  "action",
                  "book",
                  "id",
                  "" + testBook,
                  "title",
                  "Flow Tome " + suffix,
                  "author",
                  "Edited Author",
                  "description",
                  "Edited test record.",
                  "genreId",
                  "" + genre,
                  "pages",
                  "100")
              .statusCode());
      assertEquals(
          200,
          a.post(
                  "/api/action",
                  "action",
                  "save",
                  "bookId",
                  "" + testBook,
                  "status",
                  "CURRENTLY_READING",
                  "page",
                  "50")
              .statusCode());
      assertEquals(
          400,
          a.post(
                  "/api/action",
                  "action",
                  "save",
                  "bookId",
                  "" + testBook,
                  "status",
                  "CURRENTLY_READING",
                  "page",
                  "101")
              .statusCode());
      assertTrue(
          a.post("/admin", "action", "deleteBook", "id", "" + testBook)
              .body()
              .contains("cannot be deleted"));
      assertEquals(
          302,
          a.post(
                  "/admin",
                  "action",
                  "testimonial",
                  "quote",
                  "A test echo",
                  "name",
                  "Flow " + suffix,
                  "label",
                  "Disposable test",
                  "sortOrder",
                  "100",
                  "placeholder",
                  "on")
              .statusCode());
      long echo =
          ((Number)
                  Sql.query("SELECT id FROM site_testimonials WHERE name=?", "Flow " + suffix)
                      .getFirst()
                      .get("id"))
              .longValue();
      assertEquals(
          302,
          a.post(
                  "/admin",
                  "action",
                  "testimonial",
                  "id",
                  "" + echo,
                  "quote",
                  "An edited echo",
                  "name",
                  "Flow " + suffix,
                  "label",
                  "Disposable test",
                  "sortOrder",
                  "100",
                  "placeholder",
                  "on")
              .statusCode());
      assertEquals(
          302, a.post("/admin", "action", "deleteTestimonial", "id", "" + echo).statusCode());
      a.post("/api/action", "action", "remove", "bookId", "" + testBook);
      assertEquals(302, a.post("/admin", "action", "deleteBook", "id", "" + testBook).statusCode());
      testBook = 0;
      assertEquals(
          200,
          a.post("/api/action", "action", "deleteAccount", "currentPassword", password)
              .statusCode());
      assertTrue(Sql.query("SELECT id FROM user_library WHERE user_id=?", user).isEmpty());
      assertEquals(302, a.get("/dashboard").statusCode());
      b.post("/logout");
      assertEquals(302, b.get("/library").statusCode());
    } finally {
      Sql.update("DELETE FROM users WHERE email IN (?,?)", email, otherEmail);
      Sql.update("DELETE FROM site_testimonials WHERE name=?", "Flow " + suffix);
      if (testBook > 0) Sql.update("DELETE FROM books WHERE id=?", testBook);
      new com.inkspell.service.AdminService().feature(originalFeatured);
    }
  }
}
