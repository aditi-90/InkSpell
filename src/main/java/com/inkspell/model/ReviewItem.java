package com.inkspell.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a fictional sample review for a book in the InkSpell catalogue.
 * Clearly identified as community preview / demo data.
 */
public class ReviewItem implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String reviewerName;
  private final String reviewDate;
  private final int rating;
  private final String comment;
  private final boolean isDemo;

  public ReviewItem(String reviewerName, String reviewDate, int rating, String comment) {
    this.reviewerName = reviewerName;
    this.reviewDate = reviewDate;
    this.rating = rating;
    this.comment = comment;
    this.isDemo = true;
  }

  public String getReviewerName() {
    return reviewerName;
  }

  public String getReviewDate() {
    return reviewDate;
  }

  public int getRating() {
    return rating;
  }

  public String getComment() {
    return comment;
  }

  public boolean isDemo() {
    return isDemo;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("reviewer_name", reviewerName);
    map.put("review_date", reviewDate);
    map.put("rating", rating);
    map.put("comment", comment);
    map.put("is_demo", isDemo);
    return map;
  }
}
