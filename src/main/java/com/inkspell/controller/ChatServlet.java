package com.inkspell.controller;

import com.inkspell.dao.*;
import com.inkspell.service.*;
import com.inkspell.util.RateLimiter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/chat")
public class ChatServlet extends HttpServlet {
  private final GeminiService gemini = new GeminiService();

  protected void doPost(HttpServletRequest r, HttpServletResponse s) throws IOException {
    HttpSession session = r.getSession();
    try {
      if ("true".equals(r.getParameter("reset"))) {
        session.removeAttribute("chat");
        ActionServlet.json(s, 200, Map.of("message", "Conversation cleared."));
        return;
      }
      String message = Validation.text(r.getParameter("message"), 1500, "Message");
      if (!RateLimiter.allow("chat:" + PageServlet.user(r), 8, 60000)) {
        ActionServlet.json(
            s,
            429,
            Map.of("error", "Please pause a moment. You can send eight messages each minute."));
        return;
      }
      List<Map<String, String>> prior = (List<Map<String, String>>) session.getAttribute("chat");
      List<Map<String, String>> history =
          prior == null ? new ArrayList<>() : new ArrayList<>(prior);
      while (history.size() > 10) {
        history.removeFirst();
        history.removeFirst();
      }
      history.add(Map.of("role", "user", "text", message));
      List<String> genres = new ArrayList<>();
      for (var row :
          Sql.query(
              "SELECT g.name FROM genres g JOIN user_preferred_genres p ON g.id=p.genre_id WHERE"
                  + " p.user_id=?",
              PageServlet.user(r))) genres.add(row.get("name").toString());
      var catalog =
          new CatalogDao()
              .search(
                  message.length() > 100 ? message.substring(0, 100) : message, "", "popular", 1);
      if (catalog.isEmpty()) catalog = new RecommendationService().recommend(PageServlet.user(r));
      String reply = gemini.reply(history, catalog, genres);
      history.add(Map.of("role", "model", "text", reply));
      session.setAttribute("chat", history);
      ActionServlet.json(s, 200, Map.of("reply", reply));
    } catch (IllegalArgumentException e) {
      ActionServlet.json(s, 400, Map.of("error", e.getMessage()));
    } catch (Exception e) {
      ActionServlet.json(
          s,
          502,
          Map.of(
              "error",
              "The connection to the Librarian was interrupted. Please try again shortly."));
    }
  }
}
