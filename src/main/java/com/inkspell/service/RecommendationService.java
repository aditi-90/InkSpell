package com.inkspell.service;

import com.inkspell.dao.*;
import java.sql.SQLException;
import java.util.*;

public class RecommendationService {
  public static double score(double genreWeight, double rating, long popularity) {
    return genreWeight + rating * 0.15 + Math.min(popularity, 20) * 0.02;
  }

  public List<Map<String, Object>> recommend(long user) throws SQLException {
    Map<Integer, Double> weights = new HashMap<>();
    for (var row : Sql.query("SELECT genre_id FROM user_preferred_genres WHERE user_id=?", user))
      weights.merge(((Number) row.get("genre_id")).intValue(), 3.0, Double::sum);
    for (var row :
        Sql.query(
            "SELECT b.genre_id,l.reading_status,COALESCE(r.rating,0) rating FROM user_library l"
                + " JOIN books b ON b.id=l.book_id LEFT JOIN ratings r ON r.book_id=b.id AND"
                + " r.user_id=l.user_id WHERE l.user_id=?",
            user)) {
      double weight =
          switch (row.get("reading_status").toString()) {
            case "COMPLETED" -> 3;
            case "CURRENTLY_READING" -> 2;
            default -> 0;
          };
      if (((Number) row.get("rating")).intValue() >= 4) weight += 4;
      weights.merge(((Number) row.get("genre_id")).intValue(), weight, Double::sum);
    }
    var candidates =
        Sql.query(
            CatalogDao.SELECT
                + " WHERE NOT EXISTS (SELECT 1 FROM user_library l WHERE l.user_id=? AND"
                + " l.book_id=b.id) ORDER BY b.created_at DESC,b.id DESC",
            user);
    boolean hasWeights = weights.values().stream().anyMatch(w -> w > 0);
    candidates.removeIf(
        b -> hasWeights && weights.getOrDefault(((Number) b.get("genre_id")).intValue(), 0.0) <= 0);
    candidates.sort(
        Comparator.<Map<String, Object>>comparingDouble(
                b ->
                    score(
                            weights.getOrDefault(((Number) b.get("genre_id")).intValue(), 0.0),
                            ((Number) b.get("avg_rating")).doubleValue(),
                            ((Number) b.get("popularity")).longValue())
                        + (!hasWeights && Boolean.TRUE.equals(b.get("is_featured")) ? 1 : 0))
            .reversed());
    List<Map<String, Object>> varied = new ArrayList<>();
    Map<Integer, Integer> perGenre = new HashMap<>();
    for (var candidate : candidates) {
      int genre = ((Number) candidate.get("genre_id")).intValue();
      if (perGenre.getOrDefault(genre, 0) < 2) {
        varied.add(candidate);
        perGenre.merge(genre, 1, Integer::sum);
      }
      if (varied.size() == 8) break;
    }
    for (var candidate : candidates) {
      if (varied.size() == 8) break;
      if (!varied.contains(candidate)) varied.add(candidate);
    }
    return varied;
  }
}
