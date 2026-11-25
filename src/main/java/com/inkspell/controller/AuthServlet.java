package com.inkspell.controller;

import com.inkspell.dao.Sql;
import com.inkspell.service.Validation;
import com.inkspell.util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {
  protected void doGet(HttpServletRequest r, HttpServletResponse s)
      throws ServletException, IOException {
    if (r.getServletPath().equals("/logout")) {
      s.sendRedirect(r.getContextPath() + "/profile");
      return;
    }
    String next = r.getParameter("next");
    if (next != null
        && next.matches(
            "/(?:book\\?id=[0-9]+|dashboard|library|bookmarks|preferences|profile|assistant|add-book)"))
      r.getSession().setAttribute("returnTo", next);
    r.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(r, s);
  }

  protected void doPost(HttpServletRequest r, HttpServletResponse s)
      throws IOException, ServletException {
    if (r.getServletPath().equals("/logout")) {
      r.getSession().invalidate();
      s.sendRedirect(r.getContextPath() + "/");
      return;
    }
    try {
      if (!RateLimiter.allow("auth:" + r.getRemoteAddr(), 15, 900000))
        throw new IllegalArgumentException(
            "Too many attempts. Please wait 15 minutes before trying again.");
      String email = Validation.email(r.getParameter("email")),
          password = r.getParameter("password");
      if (r.getServletPath().equals("/register")) {
        String name = Validation.text(r.getParameter("fullName"), 100, "Full name");
        Validation.password(password);
        if (!java.util.Objects.equals(password, r.getParameter("confirmPassword")))
          throw new IllegalArgumentException("Passwords do not match.");
        Sql.update(
            "INSERT INTO users(full_name,email,password_hash) VALUES(?,?,?)",
            name,
            email,
            Passwords.hash(password));
      }
      var users =
          Sql.query(
              "SELECT id,full_name,email,role,created_at,password_hash FROM users WHERE email=?",
              email);
      if (users.isEmpty()
          || !Passwords.verify(password, users.getFirst().get("password_hash").toString()))
        throw new IllegalArgumentException("Email or password is incorrect.");
      var user = users.getFirst();
      user.remove("password_hash");
      r.changeSessionId();
      r.getSession().setAttribute("user", user);
      r.getSession().setAttribute("role", user.get("role"));
      String next = (String) r.getSession().getAttribute("returnTo");
      r.getSession().removeAttribute("returnTo");
      s.sendRedirect(
          r.getContextPath()
              + (next != null && next.startsWith("/") && !next.startsWith("//")
                  ? next
                  : "/dashboard"));
    } catch (IllegalArgumentException e) {
      r.setAttribute("error", e.getMessage());
      doGet(r, s);
    } catch (SQLException e) {
      r.setAttribute(
          "error",
          e.getErrorCode() == 1062
              ? "That email is already registered. Please log in."
              : "The reading room cannot connect to its database. Check the local configuration.");
      getServletContext()
          .log("Authentication database operation failed; SQLState=" + e.getSQLState());
      doGet(r, s);
    }
  }
}
