package com.inkspell.service;

import com.inkspell.dao.Sql;
import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service providing analytics, reading stats, monthly rhythms,
 * genre distributions, reading goals, achievements, milestones,
 * and activity logs for InkSpell readers.
 */
public class ReadingDashboardService {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
  private static volatile boolean tablesInitialized = false;

  public ReadingDashboardService() {
    ensureTables();
  }

  /**
   * Idempotently ensures the required schema tables exist in MySQL.
   */
  public static synchronized void ensureTables() {
    if (tablesInitialized) return;
    try {
      Sql.update(
          "CREATE TABLE IF NOT EXISTS user_reading_goals ("
              + " id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, year INT NOT NULL,"
              + " target_books INT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
              + " updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
              + " UNIQUE(user_id, year), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,"
              + " CHECK(target_books > 0)"
              + ")");

      Sql.update(
          "CREATE TABLE IF NOT EXISTS reading_activity ("
              + " id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, book_id BIGINT,"
              + " activity_type VARCHAR(40) NOT NULL, description VARCHAR(255) NOT NULL,"
              + " created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
              + " FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,"
              + " FOREIGN KEY(book_id) REFERENCES books(id) ON DELETE SET NULL,"
              + " INDEX(user_id, created_at)"
              + ")");
      tablesInitialized = true;
    } catch (Exception e) {
      System.err.println("Notice: Table verification in ReadingDashboardService: " + e.getMessage());
    }
  }

  /**
   * Records a user action in the reading activity log.
   */
  public static void recordActivity(long userId, Long bookId, String type, String description) {
    ensureTables();
    try {
      if (description != null && description.length() > 255) {
        description = description.substring(0, 252) + "...";
      }
      Sql.update(
          "INSERT INTO reading_activity(user_id, book_id, activity_type, description) VALUES (?, ?, ?, ?)",
          userId,
          bookId,
          type,
          description);
    } catch (Exception e) {
      System.err.println("Could not record reading activity: " + e.getMessage());
    }
  }

  /**
   * Sets or updates an annual reading goal for the user.
   */
  public void setGoal(long userId, int year, int targetBooks) throws SQLException {
    ensureTables();
    if (targetBooks <= 0) {
      throw new IllegalArgumentException("Goal must be at least 1 book.");
    }
    if (targetBooks > 1000) {
      throw new IllegalArgumentException("Goal cannot exceed 1,000 books.");
    }
    Sql.update(
        "INSERT INTO user_reading_goals(user_id, year, target_books) VALUES(?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE target_books = VALUES(target_books)",
        userId,
        year,
        targetBooks);

    recordActivity(
        userId,
        null,
        "GOAL",
        "Set your " + year + " reading quest to " + targetBooks + " stories");
  }

  /**
   * Fetches comprehensive dashboard data for the given user.
   */
  public Map<String, Object> getDashboardData(long userId, Integer requestedYear) throws SQLException {
    ensureTables();

    int currentYear = Year.now().getValue();
    int selectedYear = (requestedYear != null && requestedYear >= 2000 && requestedYear <= 2100)
        ? requestedYear
        : currentYear;

    // 1. Fetch entire user collection with book & genre details
    List<Map<String, Object>> collection =
        Sql.query(
            "SELECT ul.*, b.title, b.author, b.total_pages, b.cover_image_url, b.publication_year, "
                + "g.name AS genre_name, r.rating "
                + "FROM user_library ul "
                + "JOIN books b ON ul.book_id = b.id "
                + "JOIN genres g ON b.genre_id = g.id "
                + "LEFT JOIN ratings r ON r.user_id = ul.user_id AND r.book_id = ul.book_id "
                + "WHERE ul.user_id = ? "
                + "ORDER BY ul.date_added DESC",
            userId);

    // 2. Compute Overview Statistics (Section 2)
    int totalCollected = collection.size();
    int completedCount = 0;
    int currentlyReadingCount = 0;
    int waitingCount = 0;
    long totalPagesRead = 0;

    Set<Integer> recordedYears = new TreeSet<>(Collections.reverseOrder());
    recordedYears.add(currentYear);

    for (Map<String, Object> row : collection) {
      String status = String.valueOf(row.get("reading_status"));
      int bookPages = 0;
      if (row.get("total_pages_override") != null) {
        bookPages = ((Number) row.get("total_pages_override")).intValue();
      } else if (row.get("total_pages") != null) {
        bookPages = ((Number) row.get("total_pages")).intValue();
      }

      int currentPage = 0;
      if (row.get("current_page") != null) {
        currentPage = ((Number) row.get("current_page")).intValue();
      }

      if ("COMPLETED".equals(status)) {
        completedCount++;
        totalPagesRead += (bookPages > 0) ? bookPages : currentPage;
      } else if ("CURRENTLY_READING".equals(status)) {
        currentlyReadingCount++;
        totalPagesRead += currentPage;
      } else if ("WANT_TO_READ".equals(status)) {
        waitingCount++;
        if (currentPage > 0) {
          totalPagesRead += currentPage;
        }
      }

      // Check dates for years
      Object dateCompleted = row.get("date_completed");
      if (dateCompleted != null) {
        String ds = dateCompleted.toString();
        if (ds.length() >= 4) {
          try {
            recordedYears.add(Integer.parseInt(ds.substring(0, 4)));
          } catch (Exception ignored) {}
        }
      }
      Object dateAdded = row.get("date_added");
      if (dateAdded != null) {
        String ds = dateAdded.toString();
        if (ds.length() >= 4) {
          try {
            recordedYears.add(Integer.parseInt(ds.substring(0, 4)));
          } catch (Exception ignored) {}
        }
      }
    }

    int completionRate = (totalCollected > 0)
        ? (int) Math.round((completedCount * 100.0) / totalCollected)
        : 0;

    Map<String, Object> stats = new HashMap<>();
    stats.put("total", totalCollected);
    stats.put("completed", completedCount);
    stats.put("reading", currentlyReadingCount);
    stats.put("wanted", waitingCount);
    stats.put("totalPages", totalPagesRead);
    stats.put("completionRate", completionRate);

    // 3. Compute Reading Rhythm (Section 3: Monthly activity)
    String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    int[] monthlyCompleted = new int[12];
    int[] monthlyPages = new int[12];
    int[] monthlyAdded = new int[12];

    int yearTotalCompleted = 0;
    int yearTotalPages = 0;

    for (Map<String, Object> row : collection) {
      int pages = 0;
      if (row.get("total_pages_override") != null) {
        pages = ((Number) row.get("total_pages_override")).intValue();
      } else if (row.get("total_pages") != null) {
        pages = ((Number) row.get("total_pages")).intValue();
      }

      Object dc = row.get("date_completed");
      if (dc != null) {
        String ds = dc.toString();
        if (ds.length() >= 7) {
          try {
            int y = Integer.parseInt(ds.substring(0, 4));
            int m = Integer.parseInt(ds.substring(5, 7));
            if (y == selectedYear && m >= 1 && m <= 12) {
              monthlyCompleted[m - 1]++;
              monthlyPages[m - 1] += pages;
              yearTotalCompleted++;
              yearTotalPages += pages;
            }
          } catch (Exception ignored) {}
        }
      }

      Object da = row.get("date_added");
      if (da != null) {
        String ds = da.toString();
        if (ds.length() >= 7) {
          try {
            int y = Integer.parseInt(ds.substring(0, 4));
            int m = Integer.parseInt(ds.substring(5, 7));
            if (y == selectedYear && m >= 1 && m <= 12) {
              monthlyAdded[m - 1]++;
            }
          } catch (Exception ignored) {}
        }
      }
    }

    int maxCompleted = 1;
    int maxPages = 1;
    for (int i = 0; i < 12; i++) {
      if (monthlyCompleted[i] > maxCompleted) maxCompleted = monthlyCompleted[i];
      if (monthlyPages[i] > maxPages) maxPages = monthlyPages[i];
    }

    List<Map<String, Object>> monthlyList = new ArrayList<>(12);
    for (int i = 0; i < 12; i++) {
      Map<String, Object> m = new HashMap<>();
      m.put("monthNumber", i + 1);
      m.put("monthName", monthNames[i]);
      m.put("completed", monthlyCompleted[i]);
      m.put("pages", monthlyPages[i]);
      m.put("added", monthlyAdded[i]);
      int compPct = Math.min(100, Math.round((monthlyCompleted[i] * 100.0f) / maxCompleted));
      int pagePct = Math.min(100, Math.round((monthlyPages[i] * 100.0f) / maxPages));
      m.put("completedBarPercent", compPct);
      m.put("pagesBarPercent", pagePct);
      m.put("completedBarHeight", compPct > 0 ? Math.max(compPct, 8) : 0);
      m.put("pagesBarHeight", pagePct > 0 ? Math.max(pagePct, 6) : 0);
      monthlyList.add(m);
    }

    Map<String, Object> rhythm = new HashMap<>();
    rhythm.put("year", selectedYear);
    rhythm.put("availableYears", new ArrayList<>(recordedYears));
    rhythm.put("months", monthlyList);
    rhythm.put("totalCompletedInYear", yearTotalCompleted);
    rhythm.put("totalPagesInYear", yearTotalPages);
    rhythm.put("hasActivity", yearTotalCompleted > 0 || yearTotalPages > 0);

    // 4. Compute Worlds You've Explored (Section 4: Genre distribution)
    Map<String, int[]> genreCounts = new HashMap<>(); // name -> [total, completed]
    for (Map<String, Object> row : collection) {
      String genre = String.valueOf(row.get("genre_name"));
      int[] counts = genreCounts.computeIfAbsent(genre, k -> new int[2]);
      counts[0]++; // collected
      if ("COMPLETED".equals(row.get("reading_status"))) {
        counts[1]++; // completed
      }
    }

    String[] genrePalette = {
      "#D4AF37", // Warm Gold
      "#E59866", // Burnt Amber
      "#C39BD3", // Amethyst
      "#5499C7", // Midnight Sapphire
      "#48C9B0", // Forest Jade
      "#F1948A", // Deep Rose
      "#A569BD", // Gothic Purple
      "#F8C471", // Antique Ochre
      "#73C6B6", // Verdigris
      "#EC7063", // Deep Crimson
      "#85929E", // Parchment Charcoal
      "#5DADE2"  // Celestial Blue
    };

    List<Map<String, Object>> genreStats = new ArrayList<>();
    int colorIdx = 0;
    int topGenreCount = 0;
    List<String> topGenreNames = new ArrayList<>();

    for (Map.Entry<String, int[]> entry : genreCounts.entrySet()) {
      int count = entry.getValue()[0];
      int comp = entry.getValue()[1];
      double pct = (totalCollected > 0) ? (count * 100.0 / totalCollected) : 0.0;

      if (count > topGenreCount) {
        topGenreCount = count;
        topGenreNames.clear();
        topGenreNames.add(entry.getKey());
      } else if (count == topGenreCount && count > 0) {
        topGenreNames.add(entry.getKey());
      }

      Map<String, Object> gs = new HashMap<>();
      gs.put("name", entry.getKey());
      gs.put("count", count);
      gs.put("completed", comp);
      gs.put("percentage", Math.round(pct * 10.0) / 10.0);
      gs.put("color", genrePalette[colorIdx % genrePalette.length]);
      genreStats.add(gs);
      colorIdx++;
    }

    // Sort genres descending by count
    genreStats.sort((a, b) -> Integer.compare((int) b.get("count"), (int) a.get("count")));

    double circ = 376.99;
    double accumPct = 0;
    for (Map<String, Object> gs : genreStats) {
      double pct = (Double) gs.get("percentage");
      double strokeLen = (pct * circ) / 100.0;
      double offset = -(accumPct * circ) / 100.0;
      gs.put("strokeDashArray", String.format(Locale.US, "%.2f %.2f", strokeLen, Math.max(0.0, circ - strokeLen)));
      gs.put("strokeDashOffset", String.format(Locale.US, "%.2f", offset));
      accumPct += pct;
    }

    String topGenreHighlight;
    if (totalCollected == 0) {
      topGenreHighlight = "Your shelves are currently open. Discover stories to begin mapping your literary world.";
    } else if (topGenreNames.size() == 1) {
      topGenreHighlight = "Your shelves lean toward " + topGenreNames.get(0) + ".";
    } else if (topGenreNames.size() == 2) {
      topGenreHighlight = "Your shelves are enchanted by " + topGenreNames.get(0) + " and " + topGenreNames.get(1) + ".";
    } else {
      topGenreHighlight = "You are an eclectic traveler with diverse tastes across " + topGenreNames.size() + " realms.";
    }

    Map<String, Object> genresData = new HashMap<>();
    genresData.put("genreList", genreStats);
    genresData.put("highlight", topGenreHighlight);
    genresData.put("totalGenres", genreStats.size());

    // 5. Compute Reading Quest (Section 7: Annual Goal)
    List<Map<String, Object>> goalRows =
        Sql.query(
            "SELECT target_books FROM user_reading_goals WHERE user_id = ? AND year = ?",
            userId,
            selectedYear);

    Integer goalTarget = null;
    if (!goalRows.isEmpty() && goalRows.getFirst().get("target_books") != null) {
      goalTarget = ((Number) goalRows.getFirst().get("target_books")).intValue();
    }

    int questCompleted = yearTotalCompleted;
    int questPercent = 0;
    String questSubtitle;

    if (goalTarget != null && goalTarget > 0) {
      questPercent = (int) Math.round((questCompleted * 100.0) / goalTarget);
      if (questCompleted >= goalTarget) {
        questSubtitle = questCompleted + " of " + goalTarget + " stories completed — Quest Fulfilled!";
      } else {
        questSubtitle = questCompleted + " of " + goalTarget + " stories completed (" + questPercent + "% of your quest fulfilled).";
      }
    } else {
      questSubtitle = "You have completed " + questCompleted + " stories in " + selectedYear + ". Set your annual quest below to begin tracking your target!";
    }

    Map<String, Object> quest = new HashMap<>();
    quest.put("year", selectedYear);
    quest.put("hasGoal", goalTarget != null);
    quest.put("target", goalTarget != null ? goalTarget : 12);
    quest.put("completed", questCompleted);
    quest.put("percent", questPercent);
    quest.put("visualPercent", Math.min(100, questPercent));
    quest.put("subtitle", questSubtitle);

    // 6. Compute Achievements (Section 6: Badges)
    int distinctGenres = genreCounts.size();
    long ratedBooksCount = collection.stream().filter(b -> b.get("rating") != null).count();

    List<Map<String, Object>> achievements = new ArrayList<>();
    achievements.add(createAchievement("first_chapter", "First Chapter", "Complete your first story.", "i-scroll", completedCount, 1));
    achievements.add(createAchievement("bookworm", "Bookworm", "Complete 5 stories in your reading journey.", "i-book", completedCount, 5));
    achievements.add(createAchievement("avid_reader", "Avid Reader", "Complete 10 stories in your reading journey.", "i-book-plus", completedCount, 10));
    achievements.add(createAchievement("master_of_stories", "Master of Stories", "Reach the milestone of 25 completed stories.", "i-star", completedCount, 25));
    achievements.add(createAchievement("keeper_of_knowledge", "Keeper of Knowledge", "Complete 50 stories in your grand library.", "i-award", completedCount, 50));
    achievements.add(createAchievement("genre_explorer", "Genre Explorer", "Collect books from at least 5 different genres.", "i-compass", distinctGenres, 5));
    achievements.add(createAchievement("the_collector", "The Collector", "Gather 20 stories onto your personal shelves.", "i-shelf", totalCollected, 20));
    achievements.add(createAchievement("sanctuary_scholar", "Sanctuary Scholar", "Read over 1,000 pages across your journeys.", "i-feather", (int) Math.min(Integer.MAX_VALUE, totalPagesRead), 1000));
    achievements.add(createAchievement("the_critic", "Thoughtful Scribe", "Rate at least 3 books in your collection.", "i-star", (int) ratedBooksCount, 3));

    int unlockedAchievements = (int) achievements.stream().filter(a -> (Boolean) a.get("unlocked")).count();

    // 7. Compute Chapters of Your Journey Timeline (Section 5)
    List<Map<String, Object>> timeline = buildTimeline(collection);

    // 8. Compute Recent Entries Activity Log (Section 8)
    List<Map<String, Object>> activities = getRecentActivities(userId, collection);

    // Assemble final response map
    Map<String, Object> data = new HashMap<>();
    data.put("stats", stats);
    data.put("rhythm", rhythm);
    data.put("genres", genresData);
    data.put("quest", quest);
    data.put("achievements", achievements);
    data.put("unlockedAchievements", unlockedAchievements);
    data.put("totalAchievements", achievements.size());
    data.put("timeline", timeline);
    data.put("activities", activities);

    return data;
  }

  private Map<String, Object> createAchievement(
      String id, String title, String description, String icon, int current, int target) {
    Map<String, Object> a = new HashMap<>();
    boolean unlocked = current >= target;
    int pct = (target > 0) ? Math.min(100, (int) Math.round((current * 100.0) / target)) : 0;
    a.put("id", id);
    a.put("title", title);
    a.put("description", description);
    a.put("icon", icon);
    a.put("unlocked", unlocked);
    a.put("current", current);
    a.put("target", target);
    a.put("percent", pct);
    return a;
  }

  private List<Map<String, Object>> buildTimeline(List<Map<String, Object>> collection) {
    List<Map<String, Object>> events = new ArrayList<>();

    // Sort collection chronologically by date_added ascending
    List<Map<String, Object>> chronological = new ArrayList<>(collection);
    chronological.sort(Comparator.comparing(a -> String.valueOf(a.get("date_added"))));

    // First book collected
    if (!chronological.isEmpty()) {
      Map<String, Object> first = chronological.getFirst();
      events.add(makeTimelineEvent(
          "First Tome Shelved",
          "You inscribed your first story into your personal sanctuary.",
          first.get("date_added"),
          "i-shelf",
          String.valueOf(first.get("title")),
          String.valueOf(first.get("author")),
          (String) first.get("cover_image_url")));
    }

    // First book started
    Optional<Map<String, Object>> firstStarted =
        collection.stream()
            .filter(b -> b.get("date_started") != null)
            .min(Comparator.comparing(b -> String.valueOf(b.get("date_started"))));

    firstStarted.ifPresent(b -> events.add(makeTimelineEvent(
        "First Journey Begun",
        "You opened the first chapter of your reading journey.",
        b.get("date_started"),
        "i-scroll",
        String.valueOf(b.get("title")),
        String.valueOf(b.get("author")),
        (String) b.get("cover_image_url"))));

    // Completed books sorted chronologically by date_completed
    List<Map<String, Object>> completed = collection.stream()
        .filter(b -> "COMPLETED".equals(b.get("reading_status")) && b.get("date_completed") != null)
        .sorted(Comparator.comparing(b -> String.valueOf(b.get("date_completed"))))
        .toList();

    if (!completed.isEmpty()) {
      Map<String, Object> firstComp = completed.getFirst();
      events.add(makeTimelineEvent(
          "First Journey Completed",
          "You closed the back cover of your first completed tome.",
          firstComp.get("date_completed"),
          "i-award",
          String.valueOf(firstComp.get("title")),
          String.valueOf(firstComp.get("author")),
          (String) firstComp.get("cover_image_url")));
    }

    if (completed.size() >= 5) {
      Map<String, Object> fifth = completed.get(4);
      events.add(makeTimelineEvent(
          "5th Story Conquered",
          "A true bibliophile emerges: 5 stories completed.",
          fifth.get("date_completed"),
          "i-star",
          String.valueOf(fifth.get("title")),
          String.valueOf(fifth.get("author")),
          (String) fifth.get("cover_image_url")));
    }

    if (completed.size() >= 10) {
      Map<String, Object> tenth = completed.get(9);
      events.add(makeTimelineEvent(
          "10th Story Conquered",
          "A grand chapter: 10 journeys brought to their conclusion.",
          tenth.get("date_completed"),
          "i-star",
          String.valueOf(tenth.get("title")),
          String.valueOf(tenth.get("author")),
          (String) tenth.get("cover_image_url")));
    }

    if (completed.size() >= 25) {
      Map<String, Object> twentyFifth = completed.get(24);
      events.add(makeTimelineEvent(
          "25th Story Conquered",
          "Master of Stories: 25 tales preserved in memory.",
          twentyFifth.get("date_completed"),
          "i-award",
          String.valueOf(twentyFifth.get("title")),
          String.valueOf(twentyFifth.get("author")),
          (String) twentyFifth.get("cover_image_url")));
    }

    // Collection count milestones
    if (chronological.size() >= 5) {
      Map<String, Object> c5 = chronological.get(4);
      events.add(makeTimelineEvent(
          "Collector's Foundation: 5 Books Shelved",
          "Your personal library grows to 5 treasured volumes.",
          c5.get("date_added"),
          "i-book",
          String.valueOf(c5.get("title")),
          String.valueOf(c5.get("author")),
          (String) c5.get("cover_image_url")));
    }

    if (chronological.size() >= 10) {
      Map<String, Object> c10 = chronological.get(9);
      events.add(makeTimelineEvent(
          "Archive of Stories: 10 Books Shelved",
          "A quiet sanctuary with 10 doorways into other worlds.",
          c10.get("date_added"),
          "i-book-plus",
          String.valueOf(c10.get("title")),
          String.valueOf(c10.get("author")),
          (String) c10.get("cover_image_url")));
    }

    // Sort all timeline events descending by date (most recent first)
    events.sort((a, b) -> String.valueOf(b.get("rawDate")).compareTo(String.valueOf(a.get("rawDate"))));
    return events;
  }

  private Map<String, Object> makeTimelineEvent(
      String title, String desc, Object dateObj, String icon, String bookTitle, String author, String coverUrl) {
    Map<String, Object> e = new HashMap<>();
    e.put("title", title);
    e.put("description", desc);
    String raw = (dateObj != null) ? dateObj.toString() : "";
    e.put("rawDate", raw);
    e.put("dateStr", formatDateStr(raw));
    e.put("icon", icon);
    e.put("bookTitle", bookTitle);
    e.put("bookAuthor", author);
    e.put("coverUrl", coverUrl);
    return e;
  }

  private List<Map<String, Object>> getRecentActivities(
      long userId, List<Map<String, Object>> collection) {
    List<Map<String, Object>> list = new ArrayList<>();
    try {
      var rows =
          Sql.query(
              "SELECT a.*, b.title, b.author, b.cover_image_url "
                  + "FROM reading_activity a "
                  + "LEFT JOIN books b ON a.book_id = b.id "
                  + "WHERE a.user_id = ? "
                  + "ORDER BY a.created_at DESC LIMIT 15",
              userId);

      for (var r : rows) {
        Map<String, Object> item = new HashMap<>(r);
        String raw = String.valueOf(r.get("created_at"));
        item.put("dateStr", formatDateStr(raw));
        String type = String.valueOf(r.get("activity_type"));
        item.put("typeBadge", getActivityBadge(type));
        list.add(item);
      }
    } catch (Exception e) {
      System.err.println("Could not load reading activities: " + e.getMessage());
    }

    // If activity table has very few records (e.g. initial setup for existing users),
    // synthesize activity entries from existing user_library records
    if (list.size() < 3 && !collection.isEmpty()) {
      for (Map<String, Object> b : collection) {
        if (list.size() >= 10) break;
        String status = String.valueOf(b.get("reading_status"));
        String title = String.valueOf(b.get("title"));
        String cover = (String) b.get("cover_image_url");
        Long bookId = ((Number) b.get("book_id")).longValue();

        if ("COMPLETED".equals(status) && b.get("date_completed") != null) {
          Map<String, Object> comp = new HashMap<>();
          comp.put("activity_type", "COMPLETED");
          comp.put("typeBadge", "Completed");
          comp.put("description", "Finished reading \"" + title + "\"");
          comp.put("title", title);
          comp.put("book_id", bookId);
          comp.put("cover_image_url", cover);
          comp.put("dateStr", formatDateStr(String.valueOf(b.get("date_completed"))));
          list.add(comp);
        } else if ("CURRENTLY_READING".equals(status)) {
          Map<String, Object> cur = new HashMap<>();
          cur.put("activity_type", "STARTED");
          cur.put("typeBadge", "In Progress");
          cur.put("description", "Currently reading \"" + title + "\"");
          cur.put("title", title);
          cur.put("book_id", bookId);
          cur.put("cover_image_url", cover);
          cur.put("dateStr", formatDateStr(String.valueOf(b.get("updated_at") != null ? b.get("updated_at") : b.get("date_added"))));
          list.add(cur);
        } else {
          Map<String, Object> shelv = new HashMap<>();
          shelv.put("activity_type", "SHELVED");
          shelv.put("typeBadge", "Shelved");
          shelv.put("description", "Placed \"" + title + "\" on personal shelf");
          shelv.put("title", title);
          shelv.put("book_id", bookId);
          shelv.put("cover_image_url", cover);
          shelv.put("dateStr", formatDateStr(String.valueOf(b.get("date_added"))));
          list.add(shelv);
        }
      }
    }

    return list;
  }

  private String getActivityBadge(String type) {
    if (type == null) return "Activity";
    return switch (type.toUpperCase()) {
      case "SHELVED" -> "Shelved";
      case "STARTED" -> "Began Journey";
      case "COMPLETED" -> "Finished";
      case "PROGRESS" -> "Progress";
      case "RATED" -> "Rated";
      case "GOAL" -> "Quest Updated";
      default -> type;
    };
  }

  private String formatDateStr(String raw) {
    if (raw == null || raw.isBlank()) return "";
    try {
      if (raw.length() >= 10) {
        String ymd = raw.substring(0, 10);
        LocalDate ld = LocalDate.parse(ymd);
        return ld.format(DATE_FMT);
      }
    } catch (Exception ignored) {}
    return raw;
  }
}
