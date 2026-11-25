package com.inkspell.controller;

import com.google.gson.Gson;
import com.inkspell.dao.*;
import com.inkspell.service.*;
import com.inkspell.util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/action")
public class ActionServlet extends HttpServlet {
  protected void doPost(HttpServletRequest r, HttpServletResponse s) throws IOException {
    try {
      long user = PageServlet.user(r);
      String action = Validation.text(r.getParameter("action"), 30, "Action");
      if (Set.of("shelf", "save", "remove", "bookmark", "rate").contains(action)) {
        long book = Validation.integer(r.getParameter("bookId"), 1, Integer.MAX_VALUE, "Book");
        new CatalogDao().book(book);
        switch (action) {
          case "shelf" -> {
            int affected =
                Sql.update(
                    "INSERT IGNORE INTO user_library(user_id,book_id) VALUES(?,?)", user, book);
            if (affected > 0) {
              try {
                var b = new CatalogDao().book(book);
                String title = b != null ? String.valueOf(b.get("title")) : "a book";
                ReadingDashboardService.recordActivity(
                    user, book, "SHELVED", "Placed \"" + title + "\" on personal shelves");
              } catch (Exception ignored) {}
            }
          }
          case "save" ->
              new LibraryService()
                  .save(
                      user,
                      book,
                      r.getParameter("status"),
                      r.getParameter("page"),
                      r.getParameter("notes"),
                      r.getParameter("totalPages"));
          case "remove" -> {
            try {
              var b = new CatalogDao().book(book);
              String title = b != null ? String.valueOf(b.get("title")) : "a book";
              ReadingDashboardService.recordActivity(
                  user, book, "REMOVED", "Removed \"" + title + "\" from collection");
            } catch (Exception ignored) {}
            Sql.update("DELETE FROM user_library WHERE user_id=? AND book_id=?", user, book);
          }
          case "rate" -> {
            int rating = Validation.integer(r.getParameter("rating"), 1, 5, "Rating");
            Sql.update(
                "INSERT INTO ratings(user_id,book_id,rating) VALUES(?,?,?) ON DUPLICATE KEY"
                    + " UPDATE rating=VALUES(rating)",
                user,
                book,
                rating);
            try {
              var b = new CatalogDao().book(book);
              String title = b != null ? String.valueOf(b.get("title")) : "a book";
              ReadingDashboardService.recordActivity(
                  user, book, "RATED", "Awarded " + rating + " stars to \"" + title + "\"");
            } catch (Exception ignored) {}
          }
          case "bookmark" -> {
            try (Connection c = Database.open()) {
              c.setAutoCommit(false);
              try {
                Sql.query(c, "SELECT id FROM users WHERE id=? FOR UPDATE", user);
                if (Sql.update(c, "DELETE FROM bookmarks WHERE user_id=? AND book_id=?", user, book)
                    == 0)
                  Sql.update(c, "INSERT INTO bookmarks(user_id,book_id) VALUES(?,?)", user, book);
                c.commit();
              } catch (Exception e) {
                c.rollback();
                throw e;
              }
            }
          }
        }
      } else
        switch (action) {
          case "preferences" -> {
            String[] ids = r.getParameterValues("genres");
            try (Connection c = Database.open()) {
              c.setAutoCommit(false);
              try {
                Sql.update(c, "DELETE FROM user_preferred_genres WHERE user_id=?", user);
                if (ids != null)
                  for (String id : ids)
                    Sql.update(
                        c,
                        "INSERT IGNORE INTO user_preferred_genres(user_id,genre_id) VALUES(?,?)",
                        user,
                        Validation.integer(id, 1, 1000, "Genre"));
                c.commit();
              } catch (Exception e) {
                c.rollback();
                throw e;
              }
            }
          }
          case "profile" -> {
            String name = Validation.text(r.getParameter("fullName"), 100, "Full name");
            Sql.update("UPDATE users SET full_name=? WHERE id=?", name, user);
            ((Map<String, Object>) r.getSession().getAttribute("user")).put("full_name", name);
          }
          case "setGoal" -> {
            int year = Validation.integer(r.getParameter("year"), 2000, 2100, "Year");
            int target = Validation.integer(r.getParameter("target"), 1, 1000, "Goal target");
            new ReadingDashboardService().setGoal(user, year, target);
          }
          case "password", "deleteAccount" -> {
            if (!RateLimiter.allow("account:" + user, 8, 60000))
              throw new IllegalArgumentException(
                  "Please wait a minute before trying your password again.");
            var account = Sql.query("SELECT password_hash FROM users WHERE id=?", user).getFirst();
            if (!Passwords.verify(
                r.getParameter("currentPassword"), account.get("password_hash").toString()))
              throw new IllegalArgumentException("Current password is incorrect.");
            if (action.equals("password")) {
              String pw = Validation.password(r.getParameter("newPassword"));
              Sql.update("UPDATE users SET password_hash=? WHERE id=?", Passwords.hash(pw), user);
              r.changeSessionId();
            } else {
              Sql.update("DELETE FROM users WHERE id=?", user);
              r.getSession().invalidate();
            }
          }
          default -> throw new IllegalArgumentException("Unknown action.");
        }
      json(s, 200, Map.of("message", "Your reading sanctuary has been updated."));
    } catch (IllegalArgumentException e) {
      json(s, 400, Map.of("error", e.getMessage()));
    } catch (Exception e) {
      getServletContext()
          .log(
              "Reading action failed",
              e instanceof SQLException
                  ? new Exception("SQLState=" + ((SQLException) e).getSQLState())
                  : new Exception(e.getClass().getSimpleName()));
      json(s, 500, Map.of("error", "That change could not be saved. Please try again."));
    }
  }

  public static void json(HttpServletResponse s, int status, Object data) throws IOException {
    s.setStatus(status);
    s.setContentType("application/json;charset=UTF-8");
    new Gson().toJson(data, s.getWriter());
  }
}
