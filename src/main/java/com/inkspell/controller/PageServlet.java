package com.inkspell.controller;

import com.inkspell.dao.*;
import com.inkspell.service.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(
    urlPatterns = {
      "",
      "/",
      "/discover",
      "/book",
      "/dashboard",
      "/library",
      "/bookmarks",
      "/preferences",
      "/profile",
      "/assistant"
    })
public class PageServlet extends HttpServlet {
  public static long user(HttpServletRequest r) {
    return ((Number) ((Map<?, ?>) r.getSession().getAttribute("user")).get("id")).longValue();
  }

  protected void doGet(HttpServletRequest r, HttpServletResponse s)
      throws IOException, ServletException {
    String path = r.getServletPath();
    if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) {
      getServletContext().getNamedDispatcher("default").forward(r, s);
      return;
    }
    CatalogDao dao = new CatalogDao();
    String view = "home";
    try {
      r.setAttribute("genres", dao.genres());
      if (r.getSession().getAttribute("user") != null) {
        Set<Long> hearts = new HashSet<>(), shelved = new HashSet<>();
        for (var row : Sql.query("SELECT book_id FROM bookmarks WHERE user_id=?", user(r)))
          hearts.add(((Number) row.get("book_id")).longValue());
        for (var row : Sql.query("SELECT book_id FROM user_library WHERE user_id=?", user(r)))
          shelved.add(((Number) row.get("book_id")).longValue());
        r.setAttribute("bookmarkedIds", hearts);
        r.setAttribute("shelvedIds", shelved);
      }
      switch (path) {
        case "", "/" -> {
          r.setAttribute("books", dao.search("", "", "popular", 1));
          var featured = Sql.query(CatalogDao.SELECT + " WHERE b.is_featured=1 LIMIT 1");
          if (!featured.isEmpty()) r.setAttribute("featured", featured.getFirst());
          r.setAttribute(
              "testimonials",
              Sql.query("SELECT * FROM site_testimonials ORDER BY sort_order,id LIMIT 3"));
          Map<String, String> content = new HashMap<>();
          for (var row : Sql.query("SELECT * FROM site_content"))
            content.put(row.get("key").toString(), row.get("value").toString());
          r.setAttribute("content", content);
        }
        case "/discover" -> {
          view = "discover";
          int page =
              Validation.integer(
                  Optional.ofNullable(r.getParameter("page")).orElse("1"), 1, 100000, "Page");
          String q = Optional.ofNullable(r.getParameter("q")).orElse("");
          String genre = Optional.ofNullable(r.getParameter("genre")).orElse("");
          String sort = Optional.ofNullable(r.getParameter("sort")).orElse("recommended");

          int totalMatches = dao.countMatches(q, genre);
          int pageSize = 12;
          int totalPages = Math.max(1, (int) Math.ceil((double) totalMatches / pageSize));

          r.setAttribute("page", page);
          r.setAttribute("pageSize", pageSize);
          r.setAttribute("totalMatches", totalMatches);
          r.setAttribute("totalPages", totalPages);
          r.setAttribute("currentQuery", q);
          r.setAttribute("currentGenre", genre);
          r.setAttribute("currentSort", sort);
          r.setAttribute("books", dao.search(q, genre, sort, page, pageSize));
          r.setAttribute(
              "allCatalogueBooksJson",
              new com.google.gson.Gson().toJson(BookCatalogue.getAllAsMaps()));

          if (r.getSession().getAttribute("user") != null)
            r.setAttribute("recommendations", new RecommendationService().recommend(user(r)));
          if (page == 1 && q.isBlank() && genre.isBlank()) {
            r.setAttribute("featuredBooks", dao.search("", "", "recommended", 1, 3));
            r.setAttribute("popularBooks", dao.search("", "", "popular", 1, 6));
            r.setAttribute("recentBooks", dao.search("", "", "year", 1, 6));
          }
        }
        case "/book" -> {
          view = "book";
          long id = Validation.integer(r.getParameter("id"), 1, Integer.MAX_VALUE, "Book");
          r.setAttribute("book", dao.book(id));
          if (r.getSession().getAttribute("user") != null) {
            var records =
                Sql.query(
                    "SELECT l.*,r.rating FROM user_library l LEFT JOIN ratings r ON"
                        + " r.user_id=l.user_id AND r.book_id=l.book_id WHERE l.user_id=? AND"
                        + " l.book_id=?",
                    user(r),
                    id);
            if (!records.isEmpty()) r.setAttribute("record", records.getFirst());
            var ratings =
                Sql.query("SELECT rating FROM ratings WHERE user_id=? AND book_id=?", user(r), id);
            if (!ratings.isEmpty()) r.setAttribute("ownRating", ratings.getFirst().get("rating"));
            r.setAttribute(
                "bookmarked",
                !Sql.query("SELECT id FROM bookmarks WHERE user_id=? AND book_id=?", user(r), id)
                    .isEmpty());
          }
        }
        case "/dashboard", "/library", "/bookmarks" -> {
          view = path.substring(1);
          String status = Optional.ofNullable(r.getParameter("status")).orElse("");
          r.setAttribute("books", dao.library(user(r), status, path.equals("/bookmarks")));
          if (path.equals("/dashboard")) {
            var collection = dao.library(user(r), "", false);
            r.setAttribute(
                "books",
                collection.stream()
                    .filter(b -> "CURRENTLY_READING".equals(b.get("reading_status")))
                    .toList());
            r.setAttribute(
                "recentlyAdded",
                collection.stream()
                    .sorted(
                        Comparator.comparing(
                            b -> b.get("date_added").toString(), Comparator.reverseOrder()))
                    .limit(6)
                    .toList());
            r.setAttribute("recommendations", new RecommendationService().recommend(user(r)));

            Integer reqYear = null;
            String yp = r.getParameter("year");
            if (yp != null && !yp.isBlank()) {
              try {
                reqYear = Integer.parseInt(yp.trim());
              } catch (Exception ignored) {}
            }
            Map<String, Object> dashboardData;
            try {
              ReadingDashboardService dashboardService = new ReadingDashboardService();
              dashboardData = dashboardService.getDashboardData(user(r), reqYear);
            } catch (Exception ex) {
              System.err.println("Notice: Could not load reading dashboard data: " + ex.getMessage());
              dashboardData = new HashMap<>();
              dashboardData.put("stats", Map.of("total", 0, "completed", 0, "reading", 0, "wanted", 0, "totalPages", 0L, "completionRate", 0));
              dashboardData.put("rhythm", Map.of("year", java.time.Year.now().getValue(), "availableYears", List.of(), "months", List.of(), "hasActivity", false));
              dashboardData.put("genres", Map.of("genreList", List.of(), "highlight", "", "totalGenres", 0));
              dashboardData.put("quest", Map.of("year", java.time.Year.now().getValue(), "hasGoal", false, "target", 12, "completed", 0, "percent", 0, "visualPercent", 0, "subtitle", ""));
              dashboardData.put("achievements", List.of());
              dashboardData.put("unlockedAchievements", 0);
              dashboardData.put("totalAchievements", 0);
              dashboardData.put("timeline", List.of());
              dashboardData.put("activities", List.of());
            }
            r.setAttribute("dashboard", dashboardData);
            r.setAttribute("stats", dashboardData.get("stats"));
            r.setAttribute("rhythm", dashboardData.get("rhythm"));
            r.setAttribute("genresData", dashboardData.get("genres"));
            r.setAttribute("quest", dashboardData.get("quest"));
            r.setAttribute("achievements", dashboardData.get("achievements"));
            r.setAttribute("timeline", dashboardData.get("timeline"));
            r.setAttribute("activities", dashboardData.get("activities"));
            r.setAttribute("unlockedAchievements", dashboardData.get("unlockedAchievements"));
            r.setAttribute("totalAchievements", dashboardData.get("totalAchievements"));
          }
        }
        case "/preferences" -> {
          view = "preferences";
          Set<Integer> selected = new HashSet<>();
          for (var row :
              Sql.query("SELECT genre_id FROM user_preferred_genres WHERE user_id=?", user(r)))
            selected.add(((Number) row.get("genre_id")).intValue());
          r.setAttribute("selectedGenres", selected);
        }
        case "/profile" -> view = "profile";
        case "/assistant" -> view = "assistant";
        default -> {
          s.sendError(404);
          return;
        }
      }
    } catch (IllegalArgumentException e) {
      s.setStatus(404);
      r.setAttribute("error", e.getMessage());
      view = "error";
    } catch (java.sql.SQLException e) {
      s.setStatus(503);
      r.setAttribute(
          "error",
          "The library shelves are temporarily unavailable. Configure the MySQL connection and run"
              + " the database setup scripts, then refresh.");
      getServletContext().log("Page database operation failed; SQLState=" + e.getSQLState());
      view = "error";
    }
    r.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(r, s);
  }
}
