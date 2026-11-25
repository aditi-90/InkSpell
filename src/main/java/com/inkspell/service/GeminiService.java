package com.inkspell.service;

import com.google.gson.*;
import com.inkspell.config.AppConfig;
import com.inkspell.dao.BookCatalogue;
import com.inkspell.model.BookItem;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * Service for the Enchanted Librarian AI assistant.
 * Communicates with Google Gemini API when a GEMINI_API_KEY is configured.
 * When an API key is not present or when the API is unreachable, it seamlessly
 * activates the built-in Sanctuary Intelligence, providing rich, atmospheric,
 * catalogue-grounded book recommendations across all 108 books and 12 genres.
 */
public class GeminiService {
  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();

  public String reply(
      List<Map<String, String>> history, List<Map<String, Object>> catalog, List<String> genres)
      throws Exception {
    String key = AppConfig.get("GEMINI_API_KEY", "").trim();

    // If a valid Gemini API key is configured, query Google Gemini
    if (!key.isBlank() && !key.equalsIgnoreCase("your_api_key_here") && !key.startsWith("PASTE_")) {
      try {
        return callGemini(key, history, catalog, genres);
      } catch (Exception e) {
        // Fall back gracefully to Sanctuary Intelligence if Gemini call fails
        return sanctuaryReply(history, genres);
      }
    }

    // Built-in Enchanted Librarian Sanctuary Intelligence (free & offline)
    return sanctuaryReply(history, genres);
  }

  private String callGemini(
      String key,
      List<Map<String, String>> history,
      List<Map<String, Object>> catalog,
      List<String> genres)
      throws Exception {
    String model = AppConfig.get("GEMINI_MODEL", "gemini-2.5-flash").trim();
    if (!model.matches("[a-zA-Z0-9.\\-]+")) {
      model = "gemini-2.5-flash";
    }

    StringBuilder grounding =
        new StringBuilder(
            "You are the Enchanted Librarian of InkSpell, a warm, poetic, and concise literary guide."
                + " Help travelers with book recommendations, genres, authors, and reading choices."
                + " Ground recommendations in the sanctuary's CATALOG first. Decline unrelated tasks gently."
                + " Keep responses atmospheric and plain text (no markdown headings). Preferred genres: "
                + String.join(", ", genres)
                + "\nCATALOG EXCERPT:\n");

    for (var book : catalog) {
      grounding
          .append(book.get("title"))
          .append(" | ")
          .append(book.get("author"))
          .append(" | ")
          .append(book.get("genre"))
          .append(" | ")
          .append(book.get("description"))
          .append('\n');
    }

    JsonObject request = new JsonObject();
    JsonObject system = new JsonObject();
    JsonArray parts = new JsonArray();
    JsonObject systemText = new JsonObject();
    systemText.addProperty("text", grounding.toString());
    parts.add(systemText);
    system.add("parts", parts);
    request.add("system_instruction", system);

    JsonArray contents = new JsonArray();
    for (var message : history) {
      JsonObject content = new JsonObject();
      content.addProperty("role", message.get("role"));
      JsonArray p = new JsonArray();
      JsonObject text = new JsonObject();
      text.addProperty("text", message.get("text"));
      p.add(text);
      content.add("parts", p);
      contents.add(content);
    }
    request.add("contents", contents);

    JsonObject generation = new JsonObject();
    generation.addProperty("maxOutputTokens", 1200);
    request.add("generationConfig", generation);

    HttpRequest http =
        HttpRequest.newBuilder(
                URI.create(
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model
                        + ":generateContent"))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .header("x-goog-api-key", key)
            .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
            .build();

    HttpResponse<String> response = client.send(http, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      JsonArray replyParts =
          JsonParser.parseString(response.body())
              .getAsJsonObject()
              .getAsJsonArray("candidates")
              .get(0)
              .getAsJsonObject()
              .getAsJsonObject("content")
              .getAsJsonArray("parts");
      StringBuilder answer = new StringBuilder();
      for (var p : replyParts) {
        if (p.getAsJsonObject().has("text")) {
          answer.append(p.getAsJsonObject().get("text").getAsString());
        }
      }
      if (!answer.isEmpty()) {
        return answer.toString().trim();
      }
    }
    throw new IllegalStateException("Gemini returned status " + response.statusCode());
  }

  /**
   * Built-in Enchanted Librarian Sanctuary Intelligence.
   * Leverages InkSpell's 108 curated books across 12 genres to answer queries
   * with cozy, atmospheric, and characterful recommendations.
   */
  public String sanctuaryReply(List<Map<String, String>> history, List<String> userPreferredGenres) {
    String userQuery = "";
    if (history != null && !history.isEmpty()) {
      for (int i = history.size() - 1; i >= 0; i--) {
        if ("user".equalsIgnoreCase(history.get(i).get("role"))) {
          userQuery = history.get(i).getOrDefault("text", "").trim();
          break;
        }
      }
    }

    String lower = userQuery.toLowerCase(Locale.ROOT);

    // 1. Handling Greetings
    if (isGreeting(lower)) {
      return "Greetings, traveler of the written word. I am the Enchanted Librarian of InkSpell. "
          + "Within this sanctuary rest 108 carefully bound chronicles across twelve distinct realms—from epic High Fantasy "
          + "and Gothic mysteries to dark romances, mind-bending thrillers, and distant galaxies. "
          + "Tell me, what feeling or mood does your spirit long to explore today?";
    }

    // 2. Genre detection
    String matchedGenre = detectGenre(lower);
    if (matchedGenre != null) {
      List<BookItem> genreBooks = BookCatalogue.getByGenre(matchedGenre);
      if (!genreBooks.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ah, ").append(matchedGenre).append("—a splendid choice. The shelves of this realm hold wonders and perils alike. Here are chronicles that beckon:\n\n");
        int count = 0;
        for (BookItem b : genreBooks) {
          sb.append("✧ \"").append(b.getTitle()).append("\" by ").append(b.getAuthor())
            .append(" (★ ").append(String.format(Locale.US, "%.1f", b.getRating())).append(")\n")
            .append("   ").append(b.getShortDescription()).append("\n")
            .append("   Tags: ").append(String.join(", ", b.getTags())).append("\n\n");
          if (++count >= 3) break;
        }
        sb.append("You may discover these in the 'Seek a Story' catalogue and place them onto your shelf. Would you like to hear more of one of these, or seek another tale?");
        return sb.toString();
      }
    }

    // 3. Search catalogue by keyword (author, title, theme, tags)
    List<BookItem> matches = BookCatalogue.search(userQuery, "", "rating", 1, 4);
    if (!matches.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("The parchment rustles, and the shelves present these stories suited to your inquiry:\n\n");
      int count = 0;
      for (BookItem b : matches) {
        sb.append("✦ \"").append(b.getTitle()).append("\" by ").append(b.getAuthor())
          .append(" [").append(b.getGenre()).append(" • ★ ").append(String.format(Locale.US, "%.1f", b.getRating())).append("]\n")
          .append("   ").append(b.getShortDescription()).append("\n\n");
        if (++count >= 3) break;
      }
      sb.append("Every title is waiting in Seek a Story for you to open or add to your chronicle. What other paths shall we walk?");
      return sb.toString();
    }

    // 4. Mood/theme matching (cozy, scary, dark, sad, magical, romantic)
    if (lower.contains("magic") || lower.contains("dragon") || lower.contains("sword") || lower.contains("wizard")) {
      return recommendByGenre("Fantasy", "Stories woven with ancient sorcery and forgotten realms");
    } else if (lower.contains("spooky") || lower.contains("ghost") || lower.contains("haunt") || lower.contains("creepy")) {
      return recommendByGenre("Gothic Fiction", "Haunting corridors, ancestral curses, and eerie shadows");
    } else if (lower.contains("murder") || lower.contains("detective") || lower.contains("clue") || lower.contains("investigat")) {
      return recommendByGenre("Mystery", "Intricate puzzles and master sleuths unraveling dark secrets");
    } else if (lower.contains("love") || lower.contains("heart") || lower.contains("romance") || lower.contains("passion")) {
      return recommendByGenre("Romance", "Tender devotion, witty banter, and enduring emotional bonds");
    } else if (lower.contains("space") || lower.contains("future") || lower.contains("alien") || lower.contains("ai") || lower.contains("tech")) {
      return recommendByGenre("Science Fiction", "Visions of distant stars, cybernetic futures, and cosmic discovery");
    } else if (lower.contains("dark") || lower.contains("morally grey") || lower.contains("villain")) {
      return recommendByGenre("Dark Romance", "Passionate, forbidden attractions laced with danger and moral complexity");
    } else if (lower.contains("scare") || lower.contains("horror") || lower.contains("terror") || lower.contains("monster")) {
      return recommendByGenre("Horror", "Chilling tales that stir dread and test the limits of courage");
    }

    // 5. Default warm recommendation from featured/top books
    List<BookItem> featured = BookCatalogue.getFeatured();
    StringBuilder sb = new StringBuilder();
    sb.append("The sanctuary is quiet today, traveler. If you are seeking inspiration, these cherished chronicles are beloved by our visitors:\n\n");
    int count = 0;
    for (BookItem b : featured) {
      sb.append("✦ \"").append(b.getTitle()).append("\" by ").append(b.getAuthor())
        .append(" (").append(b.getGenre()).append(" • ★ ").append(String.format(Locale.US, "%.1f", b.getRating())).append(")\n")
        .append("   ").append(b.getShortDescription()).append("\n\n");
      if (++count >= 3) break;
    }
    sb.append("You may search any author, genre, or keyword in Seek a Story, or ask me for a specific realm!");
    return sb.toString();
  }

  private boolean isGreeting(String text) {
    return text.matches("^(hi|hello|hey|greetings|good day|good morning|good evening|salutations)[!.,? ]*.*")
        || text.equals("hello")
        || text.equals("hi")
        || text.equals("hey");
  }

  private String detectGenre(String text) {
    if (text.contains("dark romance")) return "Dark Romance";
    if (text.contains("psychological thriller") || text.contains("psychological")) return "Psychological Thriller";
    if (text.contains("gothic")) return "Gothic Fiction";
    if (text.contains("historical")) return "Historical Fiction";
    if (text.contains("young adult") || text.contains(" ya")) return "Young Adult";
    if (text.contains("contemporary")) return "Contemporary Fiction";
    if (text.contains("science fiction") || text.contains("sci-fi") || text.contains("scifi")) return "Science Fiction";
    if (text.contains("fantasy")) return "Fantasy";
    if (text.contains("mystery")) return "Mystery";
    if (text.contains("thriller")) return "Thriller";
    if (text.contains("horror")) return "Horror";
    if (text.contains("romance")) return "Romance";
    return null;
  }

  private String recommendByGenre(String genre, String subtitle) {
    List<BookItem> books = BookCatalogue.getByGenre(genre);
    StringBuilder sb = new StringBuilder();
    sb.append("For ").append(subtitle).append(", I recommend turning to our ").append(genre).append(" collection:\n\n");
    int count = 0;
    for (BookItem b : books) {
      sb.append("✧ \"").append(b.getTitle()).append("\" by ").append(b.getAuthor())
        .append(" (★ ").append(String.format(Locale.US, "%.1f", b.getRating())).append(")\n")
        .append("   ").append(b.getShortDescription()).append("\n\n");
      if (++count >= 2) break;
    }
    sb.append("Would you like to explore more in this genre, or seek a different path?");
    return sb.toString();
  }
}
