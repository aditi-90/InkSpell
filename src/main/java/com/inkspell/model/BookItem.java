package com.inkspell.model;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a hardcoded published book in InkSpell's curated catalogue.
 * Includes full literary metadata, sample reviews, and tags.
 */
public class BookItem implements Serializable {
  private static final long serialVersionUID = 1L;

  private final long id;
  private final String title;
  private final String author;
  private final String genre;
  private final String description;
  private final String coverImage;
  private final String isbn;
  private final double rating;
  private final int reviewCount;
  private final int publicationYear;
  private final int pageCount;
  private final String language;
  private final List<String> tags;
  private final boolean featured;
  private final int popularity;
  private final List<ReviewItem> reviews;

  public BookItem(
      long id,
      String title,
      String author,
      String genre,
      String description,
      String coverImage,
      String isbn,
      double rating,
      int reviewCount,
      int publicationYear,
      int pageCount,
      String language,
      List<String> tags,
      boolean featured,
      int popularity,
      List<ReviewItem> reviews) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.genre = genre;
    this.description = description;
    this.coverImage = coverImage;
    this.isbn = isbn;
    this.rating = rating;
    this.reviewCount = reviewCount;
    this.publicationYear = publicationYear;
    this.pageCount = pageCount;
    this.language = language;
    this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
    this.featured = featured;
    this.popularity = popularity;
    this.reviews = Collections.unmodifiableList(new ArrayList<>(reviews));
  }

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public String getGenre() {
    return genre;
  }

  public String getDescription() {
    return description;
  }

  public String getCoverImage() {
    return coverImage;
  }

  public String getIsbn() {
    return isbn;
  }

  public double getRating() {
    return rating;
  }

  public int getReviewCount() {
    return reviewCount;
  }

  public int getPublicationYear() {
    return publicationYear;
  }

  public int getPageCount() {
    return pageCount;
  }

  public String getLanguage() {
    return language;
  }

  public List<String> getTags() {
    return tags;
  }

  public boolean isFeatured() {
    return featured;
  }

  public int getPopularity() {
    return popularity;
  }

  public List<ReviewItem> getReviews() {
    return reviews;
  }

  /**
   * Returns a 1-2 sentence preview for display on cards.
   */
  public String getShortDescription() {
    if (description == null || description.isBlank()) return "";
    int firstPeriod = description.indexOf(". ");
    if (firstPeriod != -1) {
      int secondPeriod = description.indexOf(". ", firstPeriod + 2);
      if (secondPeriod != -1) {
        return description.substring(0, secondPeriod + 1);
      }
      return description.substring(0, firstPeriod + 1);
    }
    return description;
  }

  /**
   * Converts the book item into a Map matching existing database record keys
   * used across InkSpell JSPs.
   */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", id);
    map.put("title", title);
    map.put("author", author);
    map.put("genre", genre);
    map.put("description", description);
    map.put("short_description", getShortDescription());
    map.put("cover_image_url", coverImage);
    map.put("isbn", isbn);
    map.put("avg_rating", rating);
    map.put("rating_count", reviewCount);
    map.put("publication_year", publicationYear);
    map.put("total_pages", pageCount);
    map.put("language", language);
    map.put("tags", tags);
    map.put("is_featured", featured ? 1 : 0);
    map.put("popularity", popularity);
    List<Map<String, Object>> reviewMaps = new ArrayList<>();
    for (ReviewItem r : reviews) {
      reviewMaps.add(r.toMap());
    }
    map.put("reviews", reviewMaps);
    return map;
  }
}
