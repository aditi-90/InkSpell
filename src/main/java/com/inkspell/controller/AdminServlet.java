package com.inkspell.controller;

import com.inkspell.dao.*;
import com.inkspell.service.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
  protected void doGet(HttpServletRequest r, HttpServletResponse s)
      throws IOException, ServletException {
    try {
      if (r.getParameter("q") != null && r.getParameter("q").length() > 200)
        throw new IllegalArgumentException("Search is limited to 200 characters.");
      CatalogDao dao = new CatalogDao();
      r.setAttribute("genres", dao.genres());
      r.setAttribute(
          "books",
          Sql.query(
              CatalogDao.SELECT
                  + " WHERE b.title LIKE ? OR b.author LIKE ? ORDER BY b.title LIMIT 500",
              "%" + Optional.ofNullable(r.getParameter("q")).orElse("") + "%",
              "%" + Optional.ofNullable(r.getParameter("q")).orElse("") + "%"));
      r.setAttribute(
          "testimonials", Sql.query("SELECT * FROM site_testimonials ORDER BY sort_order,id"));
      r.setAttribute("contentRows", Sql.query("SELECT * FROM site_content ORDER BY `key`"));
      if (r.getParameter("edit") != null)
        r.setAttribute(
            "editBook",
            dao.book(Validation.integer(r.getParameter("edit"), 1, Integer.MAX_VALUE, "Book")));
      if (r.getParameter("testimonial") != null) {
        var rows =
            Sql.query(
                "SELECT * FROM site_testimonials WHERE id=?",
                Validation.integer(
                    r.getParameter("testimonial"), 1, Integer.MAX_VALUE, "Testimonial"));
        if (!rows.isEmpty()) r.setAttribute("editTestimonial", rows.getFirst());
      }
      r.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(r, s);
    } catch (Exception e) {
      r.setAttribute(
          "error", "The content room could not open. Check your database configuration.");
      r.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(r, s);
    }
  }

  protected void doPost(HttpServletRequest r, HttpServletResponse s)
      throws IOException, ServletException {
    try {
      String action = Optional.ofNullable(r.getParameter("action")).orElse("");
      switch (action) {
        case "book" -> {
          Map<String, String> values = new HashMap<>();
          for (String k :
              List.of(
                  "id",
                  "title",
                  "author",
                  "description",
                  "genreId",
                  "cover",
                  "isbn",
                  "year",
                  "pages",
                  "featured")) values.put(k, r.getParameter(k));
          new AdminService().saveBook(values);
        }
        case "feature" ->
            new AdminService()
                .feature(Validation.integer(r.getParameter("id"), 1, Integer.MAX_VALUE, "Book"));
        case "deleteBook" -> {
          try {
            Sql.update(
                "DELETE FROM books WHERE id=?",
                Validation.integer(r.getParameter("id"), 1, Integer.MAX_VALUE, "Book"));
          } catch (SQLException e) {
            if (e.getErrorCode() == 1451)
              throw new IllegalArgumentException(
                  "This book is saved in a reader's collection and cannot be deleted.");
            throw e;
          }
          var remaining = Sql.query("SELECT id FROM books ORDER BY is_featured DESC,id LIMIT 1");
          if (!remaining.isEmpty())
            new AdminService().feature(((Number) remaining.getFirst().get("id")).longValue());
        }
        case "content" -> {
          String key = Validation.text(r.getParameter("key"), 100, "Content key"),
              value = Validation.text(r.getParameter("value"), 10000, "Content");
          if (Sql.update("UPDATE site_content SET `value`=? WHERE `key`=?", value, key) == 0)
            throw new IllegalArgumentException("Content key not found.");
        }
        case "testimonial" -> {
          String quote = Validation.text(r.getParameter("quote"), 1000, "Quote"),
              name = Validation.text(r.getParameter("name"), 100, "Name"),
              label = Validation.text(r.getParameter("label"), 100, "Label");
          int order = Validation.integer(r.getParameter("sortOrder"), 0, 10000, "Sort order");
          boolean placeholder = "on".equals(r.getParameter("placeholder"));
          if (r.getParameter("id") == null || r.getParameter("id").isBlank())
            Sql.update(
                "INSERT INTO site_testimonials(quote,name,label,is_placeholder,sort_order)"
                    + " VALUES(?,?,?,?,?)",
                quote,
                name,
                label,
                placeholder,
                order);
          else
            Sql.update(
                "UPDATE site_testimonials SET quote=?,name=?,label=?,is_placeholder=?,sort_order=?"
                    + " WHERE id=?",
                quote,
                name,
                label,
                placeholder,
                order,
                Validation.integer(r.getParameter("id"), 1, Integer.MAX_VALUE, "Testimonial"));
        }
        case "deleteTestimonial" ->
            Sql.update(
                "DELETE FROM site_testimonials WHERE id=?",
                Validation.integer(r.getParameter("id"), 1, Integer.MAX_VALUE, "Testimonial"));
        default -> throw new IllegalArgumentException("Choose a valid content action.");
      }
      r.getSession().setAttribute("adminFlash", "The archive has been updated.");
      s.sendRedirect(r.getContextPath() + "/admin");
    } catch (IllegalArgumentException e) {
      r.setAttribute("error", e.getMessage());
      doGet(r, s);
    } catch (SQLException e) {
      r.setAttribute("error", "The change could not be saved. Check the fields and try again.");
      getServletContext().log("Admin write failed; SQLState=" + e.getSQLState());
      doGet(r, s);
    }
  }
}
