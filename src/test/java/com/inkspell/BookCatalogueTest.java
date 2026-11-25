package com.inkspell;

import static org.junit.jupiter.api.Assertions.*;

import com.inkspell.dao.BookCatalogue;
import com.inkspell.model.BookItem;
import com.inkspell.model.ReviewItem;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookCatalogueTest {

  @Test
  @DisplayName("Catalogue contains at least 100 unique books")
  void catalogueHasAtLeast100Books() {
    List<BookItem> books = BookCatalogue.getAll();
    assertTrue(books.size() >= 100, "Catalogue must contain at least 100 books. Actual: " + books.size());
    assertEquals(108, books.size(), "Catalogue should contain exactly 108 books.");

    Set<Long> ids = new HashSet<>();
    Set<String> titles = new HashSet<>();
    for (BookItem b : books) {
      assertTrue(ids.add(b.getId()), "Duplicate book ID found: " + b.getId());
      assertTrue(titles.add(b.getTitle().toLowerCase()), "Duplicate title found: " + b.getTitle());
    }
  }

  @Test
  @DisplayName("All 12 required genres are represented with 8-10 books each")
  void all12GenresAreRepresented() {
    List<String> requiredGenres =
        List.of(
            "Fantasy",
            "Mystery",
            "Thriller",
            "Horror",
            "Romance",
            "Dark Romance",
            "Science Fiction",
            "Contemporary Fiction",
            "Historical Fiction",
            "Young Adult",
            "Gothic Fiction",
            "Psychological Thriller");

    Map<String, Integer> genreCounts = new HashMap<>();
    for (BookItem b : BookCatalogue.getAll()) {
      genreCounts.merge(b.getGenre(), 1, Integer::sum);
    }

    for (String genre : requiredGenres) {
      Integer count = genreCounts.get(genre);
      assertNotNull(count, "Genre missing from catalogue: " + genre);
      assertTrue(
          count >= 8 && count <= 10,
          "Genre '" + genre + "' should have 8-10 books, but has: " + count);
      assertEquals(9, count.intValue(), "Genre '" + genre + "' has exactly 9 books.");
    }
  }

  @Test
  @DisplayName("Every book contains all required non-empty fields and sample reviews")
  void everyBookHasRequiredFields() {
    Set<Double> distinctRatings = new HashSet<>();
    Set<Integer> distinctReviewCounts = new HashSet<>();

    for (BookItem b : BookCatalogue.getAll()) {
      assertTrue(b.getId() > 0, "Book ID must be positive: " + b.getId());
      assertNotNull(b.getTitle(), "Title required for id " + b.getId());
      assertFalse(b.getTitle().isBlank(), "Title cannot be blank for id " + b.getId());

      assertNotNull(b.getAuthor(), "Author required for " + b.getTitle());
      assertFalse(b.getAuthor().isBlank(), "Author cannot be blank for " + b.getTitle());

      assertNotNull(b.getGenre(), "Genre required for " + b.getTitle());
      assertFalse(b.getGenre().isBlank(), "Genre cannot be blank for " + b.getTitle());

      assertNotNull(b.getDescription(), "Description required for " + b.getTitle());
      assertTrue(b.getDescription().length() >= 80, "Description should be detailed for " + b.getTitle());
      assertFalse(b.getShortDescription().isBlank(), "Short description preview required for " + b.getTitle());

      assertNotNull(b.getCoverImage(), "Cover image required for " + b.getTitle());
      assertTrue(b.getCoverImage().startsWith("https://covers.openlibrary.org/"), "Cover image URL format invalid for " + b.getTitle());

      assertTrue(b.getRating() >= 1.0 && b.getRating() <= 5.0, "Rating out of bounds: " + b.getRating());
      assertTrue(b.getReviewCount() > 0, "Review count must be positive for " + b.getTitle());
      assertTrue(b.getPublicationYear() >= 1700 && b.getPublicationYear() <= 2026, "Publication year out of bounds for " + b.getTitle());
      assertTrue(b.getPageCount() >= 50, "Page count must be realistic for " + b.getTitle());

      assertNotNull(b.getLanguage(), "Language required for " + b.getTitle());
      assertFalse(b.getLanguage().isBlank(), "Language cannot be blank for " + b.getTitle());

      assertNotNull(b.getTags(), "Tags required for " + b.getTitle());
      assertTrue(b.getTags().size() >= 2 && b.getTags().size() <= 5, "Tags count must be 2-5 for " + b.getTitle());

      assertTrue(b.getPopularity() >= 0 && b.getPopularity() <= 100, "Popularity must be 0-100 for " + b.getTitle());

      assertNotNull(b.getReviews(), "Reviews required for " + b.getTitle());
      assertTrue(b.getReviews().size() >= 2, "Must have at least 2 sample reviews for " + b.getTitle());
      for (ReviewItem r : b.getReviews()) {
        assertNotNull(r.getReviewerName(), "Reviewer name required");
        assertFalse(r.getReviewerName().isBlank(), "Reviewer name cannot be blank");
        assertNotNull(r.getReviewDate(), "Review date required");
        assertTrue(r.getRating() >= 1 && r.getRating() <= 5, "Review rating out of bounds: " + r.getRating());
        assertNotNull(r.getComment(), "Review comment required");
        assertFalse(r.getComment().isBlank(), "Review comment cannot be blank");
        assertTrue(r.isDemo(), "Sample review must be identified as demo data");
      }

      distinctRatings.add(b.getRating());
      distinctReviewCounts.add(b.getReviewCount());

      Map<String, Object> map = b.toMap();
      assertEquals(b.getId(), map.get("id"));
      assertEquals(b.getTitle(), map.get("title"));
      assertEquals(b.getAuthor(), map.get("author"));
      assertEquals(b.getGenre(), map.get("genre"));
      assertEquals(b.getCoverImage(), map.get("cover_image_url"));
    }

    assertTrue(distinctRatings.size() >= 5, "Ratings must be realistic and varied");
    assertTrue(distinctReviewCounts.size() >= 10, "Review counts must be varied");
  }

  @Test
  @DisplayName("Search by title, author, genre, and tags returns expected results")
  void searchFilteringWorks() {
    // Title search
    List<BookItem> hobbit = BookCatalogue.search("The Hobbit", "", "recommended", 1, 10);
    assertFalse(hobbit.isEmpty());
    assertEquals("The Hobbit", hobbit.get(0).getTitle());

    // Author search
    List<BookItem> tolkien = BookCatalogue.search("Tolkien", "", "recommended", 1, 10);
    assertEquals(2, tolkien.size());

    // Genre search
    List<BookItem> horror = BookCatalogue.search("", "Horror", "recommended", 1, 20);
    assertEquals(9, horror.size());
    for (BookItem b : horror) {
      assertEquals("Horror", b.getGenre());
    }

    // Tag search
    List<BookItem> dragons = BookCatalogue.search("Dragons", "", "recommended", 1, 10);
    assertFalse(dragons.isEmpty());

    // Combined search and genre
    List<BookItem> stephenKingHorror = BookCatalogue.search("Stephen King", "Horror", "recommended", 1, 10);
    assertEquals(2, stephenKingHorror.size()); // The Shining and Pet Sematary

    List<BookItem> stephenKingPsychological = BookCatalogue.search("Stephen King", "Psychological Thriller", "recommended", 1, 10);
    assertEquals(1, stephenKingPsychological.size()); // Misery
  }

  @Test
  @DisplayName("Sorting orders results correctly")
  void sortingWorksCorrectly() {
    // Highest rated
    List<BookItem> topRated = BookCatalogue.search("", "", "rating", 1, 10);
    for (int i = 0; i < topRated.size() - 1; i++) {
      assertTrue(topRated.get(i).getRating() >= topRated.get(i + 1).getRating());
    }

    // Alphabetical title
    List<BookItem> titleSorted = BookCatalogue.search("", "", "title", 1, 10);
    for (int i = 0; i < titleSorted.size() - 1; i++) {
      assertTrue(titleSorted.get(i).getTitle().compareToIgnoreCase(titleSorted.get(i + 1).getTitle()) <= 0);
    }

    // Newest year
    List<BookItem> yearSorted = BookCatalogue.search("", "", "year", 1, 10);
    for (int i = 0; i < yearSorted.size() - 1; i++) {
      assertTrue(yearSorted.get(i).getPublicationYear() >= yearSorted.get(i + 1).getPublicationYear());
    }
  }

  @Test
  @DisplayName("Count matches accurately computes matching items")
  void countMatchesWorks() {
    assertEquals(108, BookCatalogue.countMatches("", ""));
    assertEquals(9, BookCatalogue.countMatches("", "Gothic Fiction"));
    assertEquals(9, BookCatalogue.countMatches("", "Dark Romance"));
    assertEquals(0, BookCatalogue.countMatches("NonexistentBookXYZ123", ""));
  }
}
