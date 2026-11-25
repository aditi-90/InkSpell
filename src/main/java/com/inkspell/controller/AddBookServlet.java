package com.inkspell.controller;

import com.google.gson.Gson;
import com.inkspell.service.BookService;
import com.inkspell.service.BookService.BookResult;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/add-book")
public class AddBookServlet extends HttpServlet {

  private final BookService bookService = new BookService();

  @Override
  protected void doGet(HttpServletRequest r, HttpServletResponse s)
      throws ServletException, IOException {
    HttpSession session = r.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      s.sendRedirect(r.getContextPath() + "/login?next=/add-book");
      return;
    }

    r.setAttribute("genres", BookService.getAvailableGenres());
    r.getRequestDispatcher("/WEB-INF/views/add-book.jsp").forward(r, s);
  }

  @Override
  protected void doPost(HttpServletRequest r, HttpServletResponse s)
      throws ServletException, IOException {
    boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(r.getHeader("X-Requested-With"))
        || (r.getHeader("Accept") != null && r.getHeader("Accept").contains("application/json"));

    HttpSession session = r.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      if (isAjax) {
        s.setStatus(401);
        s.setContentType("application/json;charset=UTF-8");
        s.getWriter().write("{\"error\":\"Please enter the portal to continue.\"}");
      } else {
        s.sendRedirect(r.getContextPath() + "/login?next=/add-book");
      }
      return;
    }

    // CSRF verification
    String paramCsrf = r.getParameter("csrf");
    String headerCsrf = r.getHeader("X-CSRF-Token");
    String sessionCsrf = (String) session.getAttribute("csrf");
    if (sessionCsrf == null
        || (!sessionCsrf.equals(paramCsrf) && !sessionCsrf.equals(headerCsrf))) {
      handleError(
          r,
          s,
          "Your session token has expired. Please refresh the page and try again.",
          403,
          isAjax);
      return;
    }

    long userId = PageServlet.user(r);
    String title = r.getParameter("title");
    String author = r.getParameter("author");
    String genre = r.getParameter("genre");
    String description = r.getParameter("description");
    String coverUrl = r.getParameter("coverUrl");
    String publicationYear = r.getParameter("publicationYear");
    String isbn = r.getParameter("isbn");
    String totalPages = r.getParameter("totalPages");
    String submitAction = r.getParameter("submitAction");
    boolean addToCollection = !"catalogue".equalsIgnoreCase(submitAction);

    try {
      BookResult result =
          bookService.addBook(
              userId,
              title,
              author,
              genre,
              description,
              coverUrl,
              isbn,
              publicationYear,
              totalPages,
              addToCollection);

      if (isAjax) {
        s.setStatus(200);
        s.setContentType("application/json;charset=UTF-8");
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", result.getStatus().name());
        resp.put("message", result.getMessage());
        resp.put("bookId", result.getBookId());
        resp.put("book", result.getBook());
        resp.put("isSuccess", result.isSuccess());
        resp.put("isDuplicateNotice", result.isDuplicateNotice());
        new Gson().toJson(resp, s.getWriter());
      } else {
        r.setAttribute("result", result);
        if (result.isSuccess()) {
          r.setAttribute("successMessage", result.getMessage());
          r.setAttribute("createdBookId", result.getBookId());
          r.setAttribute("createdBook", result.getBook());
        } else {
          r.setAttribute("infoMessage", result.getMessage());
          r.setAttribute("existingBookId", result.getBookId());
          r.setAttribute("existingBook", result.getBook());
        }
        r.setAttribute("genres", BookService.getAvailableGenres());
        r.getRequestDispatcher("/WEB-INF/views/add-book.jsp").forward(r, s);
      }
    } catch (IllegalArgumentException e) {
      preserveFields(r, title, author, genre, description, coverUrl, publicationYear, isbn, totalPages);
      handleError(r, s, e.getMessage(), 400, isAjax);
    } catch (SQLException e) {
      getServletContext().log("Failed to persist book; SQLState=" + e.getSQLState(), e);
      preserveFields(r, title, author, genre, description, coverUrl, publicationYear, isbn, totalPages);
      handleError(
          r,
          s,
          "The library shelves encountered an unexpected issue while saving this tome. Please try again.",
          500,
          isAjax);
    } catch (Exception e) {
      getServletContext().log("Unexpected error in AddBookServlet", e);
      preserveFields(r, title, author, genre, description, coverUrl, publicationYear, isbn, totalPages);
      handleError(
          r,
          s,
          "An unexpected error occurred while adding this story. Please try again.",
          500,
          isAjax);
    }
  }

  private void preserveFields(
      HttpServletRequest r,
      String title,
      String author,
      String genre,
      String description,
      String coverUrl,
      String publicationYear,
      String isbn,
      String totalPages) {
    r.setAttribute("title", title);
    r.setAttribute("author", author);
    r.setAttribute("genre", genre);
    r.setAttribute("description", description);
    r.setAttribute("coverUrl", coverUrl);
    r.setAttribute("publicationYear", publicationYear);
    r.setAttribute("isbn", isbn);
    r.setAttribute("totalPages", totalPages);
    r.setAttribute("genres", BookService.getAvailableGenres());
  }

  private void handleError(
      HttpServletRequest r, HttpServletResponse s, String error, int statusCode, boolean isAjax)
      throws ServletException, IOException {
    if (isAjax) {
      s.setStatus(statusCode);
      s.setContentType("application/json;charset=UTF-8");
      new Gson().toJson(Map.of("error", error), s.getWriter());
    } else {
      r.setAttribute("error", error);
      r.setAttribute("genres", BookService.getAvailableGenres());
      r.getRequestDispatcher("/WEB-INF/views/add-book.jsp").forward(r, s);
    }
  }
}
