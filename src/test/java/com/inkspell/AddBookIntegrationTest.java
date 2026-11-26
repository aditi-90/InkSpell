package com.inkspell;

import static org.junit.jupiter.api.Assertions.*;

import com.inkspell.dao.BookCatalogue;
import com.inkspell.dao.CatalogDao;
import com.inkspell.dao.Sql;
import com.inkspell.model.ReadingStatus;
import com.inkspell.service.BookService;
import com.inkspell.service.BookService.BookResult;
import com.inkspell.service.BookService.ResultStatus;
import java.util.*;
import org.junit.jupiter.api.*;

class AddBookIntegrationTest {

  private final BookService bookService = new BookService();
  private final CatalogDao catalogDao = new CatalogDao();

  @Test
  void testAddBookWorkflowAndPersistence() throws Exception {
    var userRows = Sql.query("SELECT id FROM users ORDER BY id LIMIT 2");
    assertTrue(userRows.size() >= 2, "Test requires at least 2 users in the database");
    long user1 = ((Number) userRows.get(0).get("id")).longValue();
    long user2 = ((Number) userRows.get(1).get("id")).longValue();

    long timestamp = System.currentTimeMillis();
    String uniqueTitle = "Chronicles of the Starforged " + timestamp;
    String author = "Archivist Vane " + timestamp;
    String genre = "Fantasy";
    String description = "A legendary journey across the celestial expanse, weaving shadows and starfire.";
    String coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c";
    String isbn = String.format("978%010d", Math.abs(new Random().nextLong() % 10000000000L));
    String year = "2024";
    String pages = "450";
    String readingStatus = "CURRENTLY_READING";

    long newBookId = 0;
    try {
      // 1. Add Book as User 1 with CURRENTLY_READING status
      BookResult result =
          bookService.addBook(
              user1,
              uniqueTitle,
              author,
              genre,
              description,
              coverUrl,
              isbn,
              year,
              pages,
              readingStatus,
              true);

      assertTrue(result.isSuccess(), "Adding book should succeed: " + result.getMessage());
      assertEquals(ResultStatus.CREATED_IN_COLLECTION, result.getStatus());
      newBookId = result.getBookId();
      final long createdBookId = newBookId;
      assertTrue(createdBookId > 0, "Book ID must be greater than 0");

      // 2. Verify book exists in MySQL 'books' table
      var bookRows = Sql.query("SELECT * FROM books WHERE id = ?", createdBookId);
      assertFalse(bookRows.isEmpty(), "Book must be saved in MySQL books table");
      Map<String, Object> savedBook = bookRows.getFirst();
      assertEquals(uniqueTitle, savedBook.get("title"));
      assertEquals(author, savedBook.get("author"));
      assertEquals(description, savedBook.get("description"));
      assertEquals(isbn, savedBook.get("isbn"));
      assertEquals(450, ((Number) savedBook.get("total_pages")).intValue());
      assertEquals(2024, ((Number) savedBook.get("publication_year")).intValue());

      // 3. Verify user-book relationship in 'user_library' table
      var libRows =
          Sql.query(
              "SELECT * FROM user_library WHERE user_id = ? AND book_id = ?", user1, createdBookId);
      assertFalse(libRows.isEmpty(), "User-book relationship must be saved in MySQL user_library table");
      Map<String, Object> savedLib = libRows.getFirst();
      assertEquals("CURRENTLY_READING", savedLib.get("reading_status"));
      assertNotNull(savedLib.get("date_started"), "date_started must be set for currently reading book");

      // 4. Verify book appears in User 1's collection via CatalogDao
      List<Map<String, Object>> user1Library = catalogDao.library(user1, "", false);
      boolean foundInUser1 =
          user1Library.stream().anyMatch(b -> ((Number) b.get("id")).longValue() == createdBookId);
      assertTrue(foundInUser1, "Newly added book must appear in User 1's My Collection");

      // Also verify filtering by status
      List<Map<String, Object>> user1Reading = catalogDao.library(user1, "CURRENTLY_READING", false);
      boolean foundInReading =
          user1Reading.stream().anyMatch(b -> ((Number) b.get("id")).longValue() == createdBookId);
      assertTrue(foundInReading, "Book must appear under CURRENTLY_READING status tab");

      // 5. Verify User 2's collection does NOT contain this book automatically
      List<Map<String, Object>> user2Library = catalogDao.library(user2, "", false);
      boolean foundInUser2 =
          user2Library.stream().anyMatch(b -> ((Number) b.get("id")).longValue() == createdBookId);
      assertFalse(foundInUser2, "Book must NOT appear in User 2's collection automatically");

      // 6. Verify duplicate prevention for the same user
      BookResult duplicateResult =
          bookService.addBook(
              user1,
              uniqueTitle,
              author,
              genre,
              description,
              coverUrl,
              isbn,
              year,
              pages,
              readingStatus,
              true);
      assertTrue(duplicateResult.isDuplicateNotice(), "Duplicate addition by same user must be prevented");
      assertEquals(ResultStatus.ALREADY_IN_COLLECTION, duplicateResult.getStatus());

      // Verify books table still has exactly 1 record for this book (no duplicate rows)
      var countRows =
          Sql.query("SELECT COUNT(*) AS c FROM books WHERE title = ? AND author = ?", uniqueTitle, author);
      assertEquals(1, ((Number) countRows.getFirst().get("c")).intValue(), "No duplicate row in books table");

      // 7. Verify User 2 can add this existing book to their collection without duplicating the books table
      BookResult user2Result =
          bookService.addBook(
              user2,
              uniqueTitle,
              author,
              genre,
              description,
              coverUrl,
              isbn,
              year,
              pages,
              "WANT_TO_READ",
              true);
      assertTrue(user2Result.isSuccess(), "User 2 adding the existing tome must succeed");
      assertEquals(ResultStatus.EXISTING_ADDED_TO_COLLECTION, user2Result.getStatus());
      assertEquals(createdBookId, user2Result.getBookId(), "Same book ID must be referenced");

      // Verify User 2 now has the book in their collection
      List<Map<String, Object>> user2UpdatedLib = catalogDao.library(user2, "", false);
      boolean user2NowHasBook =
          user2UpdatedLib.stream().anyMatch(b -> ((Number) b.get("id")).longValue() == createdBookId);
      assertTrue(user2NowHasBook, "User 2 must now have the book in their collection");

      // Verify book count in books table is still exactly 1
      var countAfterUser2 =
          Sql.query("SELECT COUNT(*) AS c FROM books WHERE title = ? AND author = ?", uniqueTitle, author);
      assertEquals(1, ((Number) countAfterUser2.getFirst().get("c")).intValue(), "Books table remains deduplicated");

      // 8. Verify BookCatalogue memory synchronization
      Optional<com.inkspell.model.BookItem> item = BookCatalogue.getById(createdBookId);
      assertTrue(item.isPresent(), "Book must be accessible from BookCatalogue");
      assertEquals(uniqueTitle, item.get().getTitle());

      // Verify search in catalogue finds this book
      List<Map<String, Object>> searchResults = catalogDao.search(uniqueTitle, "", "title", 1, 10);
      boolean foundInSearch =
          searchResults.stream().anyMatch(b -> ((Number) b.get("id")).longValue() == createdBookId);
      assertTrue(foundInSearch, "Book must be found in CatalogDao.search");
    } finally {
      if (newBookId > 0) {
        Sql.update("DELETE FROM user_library WHERE book_id = ?", newBookId);
        Sql.update("DELETE FROM reading_activity WHERE book_id = ?", newBookId);
        Sql.update("DELETE FROM books WHERE id = ?", newBookId);
        BookCatalogue.removeCustomBook(newBookId);
      }
    }
  }

  @Test
  void testCompletedReadingStatusSetsDatesAndPages() throws Exception {
    var userRows = Sql.query("SELECT id FROM users ORDER BY id LIMIT 1");
    long user = ((Number) userRows.getFirst().get("id")).longValue();

    long timestamp = System.currentTimeMillis();
    String title = "Completed Masterpiece " + timestamp;
    String author = "Scholar Thorne " + timestamp;
    String genre = "Science Fiction";
    String desc = "A breathtaking voyage through quantum gateways and distant stellar empires.";
    String pages = "320";

    long bookId = 0;
    try {
      BookResult res =
          bookService.addBook(
              user,
              title,
              author,
              genre,
              desc,
              null,
              null,
              "2023",
              pages,
              "COMPLETED",
              true);

      assertTrue(res.isSuccess());
      bookId = res.getBookId();

      var libRow = Sql.query("SELECT * FROM user_library WHERE user_id=? AND book_id=?", user, bookId).getFirst();
      assertEquals("COMPLETED", libRow.get("reading_status"));
      assertEquals(320, ((Number) libRow.get("current_page")).intValue(), "Current page must equal total pages for completed book");
      assertNotNull(libRow.get("date_completed"), "date_completed must be set");
    } finally {
      if (bookId > 0) {
        Sql.update("DELETE FROM user_library WHERE book_id = ?", bookId);
        Sql.update("DELETE FROM reading_activity WHERE book_id = ?", bookId);
        Sql.update("DELETE FROM books WHERE id = ?", bookId);
        BookCatalogue.removeCustomBook(bookId);
      }
    }
  }

  @Test
  void testMandatoryUserTestCases() throws Exception {
    long uniqueSuffix = System.currentTimeMillis();
    String testEmail = "tester_" + uniqueSuffix + "@test.com";
    Sql.update(
        "INSERT INTO users(full_name, email, password_hash) VALUES(?, ?, ?)",
        "Mandatory Tester",
        testEmail,
        "dummyhash");
    var userRows = Sql.query("SELECT id FROM users WHERE email = ?", testEmail);
    assertFalse(userRows.isEmpty(), "Test requires a valid newly created user");
    long userId = ((Number) userRows.getFirst().get("id")).longValue();
    assertTrue(userId > 0, "Test requires a valid newly created user");

    long book1Id = 0;
    long book2Id = 0;
    long book3Id = 0;

    try {
      // TEST 1: The Moonlit Kingdom
      BookResult res1 =
          bookService.addBook(
              userId,
              "The Moonlit Kingdom",
              "Aditi Singh",
              "Fantasy",
              "A completely fictional fantasy story.",
              null,
              null,
              null,
              null,
              "WANT_TO_READ",
              true);
      assertTrue(res1.isSuccess(), "Test 1: The Moonlit Kingdom must be successfully added: " + res1.getMessage());
      assertTrue(res1.getStatus() == ResultStatus.CREATED_IN_COLLECTION || res1.getStatus() == ResultStatus.EXISTING_ADDED_TO_COLLECTION);
      book1Id = res1.getBookId();
      assertTrue(book1Id > 0);

      // Verify Test 1 saved in MySQL books table
      var b1 = Sql.query("SELECT * FROM books WHERE id = ?", book1Id);
      assertFalse(b1.isEmpty(), "Test 1: Book must exist in MySQL books table");
      assertEquals("The Moonlit Kingdom", b1.getFirst().get("title"));
      assertEquals("Aditi Singh", b1.getFirst().get("author"));

      // Verify Test 1 in user_library
      var l1 = Sql.query("SELECT * FROM user_library WHERE user_id = ? AND book_id = ?", userId, book1Id);
      assertFalse(l1.isEmpty(), "Test 1: Book must be linked in user_library");

      // TEST 2: The Last Clockmaker of Ashbourne
      BookResult res2 =
          bookService.addBook(
              userId,
              "The Last Clockmaker of Ashbourne",
              "Aditi Singh",
              "Mystery",
              "An intricate mystery of ticking gears and missing hours.",
              null,
              null,
              null,
              null,
              "CURRENTLY_READING",
              true);
      assertTrue(res2.isSuccess(), "Test 2: The Last Clockmaker of Ashbourne must be successfully added: " + res2.getMessage());
      assertTrue(res2.getStatus() == ResultStatus.CREATED_IN_COLLECTION || res2.getStatus() == ResultStatus.EXISTING_ADDED_TO_COLLECTION);
      book2Id = res2.getBookId();
      assertTrue(book2Id > 0);
      assertNotEquals(book1Id, book2Id, "Test 2 must be inserted as a separate book");

      // TEST 3: Whispers Beneath the Crimson Sea
      BookResult res3 =
          bookService.addBook(
              userId,
              "Whispers Beneath the Crimson Sea",
              "Aditi Singh",
              "Horror",
              "An oceanic descent into eldritch abysses.",
              null,
              null,
              null,
              null,
              "COMPLETED",
              true);
      assertTrue(res3.isSuccess(), "Test 3: Whispers Beneath the Crimson Sea must be successfully added: " + res3.getMessage());
      assertTrue(res3.getStatus() == ResultStatus.CREATED_IN_COLLECTION || res3.getStatus() == ResultStatus.EXISTING_ADDED_TO_COLLECTION);
      book3Id = res3.getBookId();
      assertTrue(book3Id > 0);
      assertNotEquals(book1Id, book3Id, "Test 3 must be inserted as a separate book");
      assertNotEquals(book2Id, book3Id, "Test 3 must be separate from Test 2");

      // TEST 4: Add The Moonlit Kingdom again for the same user
      BookResult res4 =
          bookService.addBook(
              userId,
              "The Moonlit Kingdom",
              "Aditi Singh",
              "Fantasy",
              "A completely fictional fantasy story.",
              null,
              null,
              null,
              null,
              "WANT_TO_READ",
              true);
      assertTrue(res4.isDuplicateNotice(), "Test 4: Adding The Moonlit Kingdom again must trigger duplicate notice");
      assertEquals(ResultStatus.ALREADY_IN_COLLECTION, res4.getStatus());
      assertEquals(book1Id, res4.getBookId(), "Duplicate must point to The Moonlit Kingdom ID, not The Hobbit");
      assertTrue(res4.getMessage().contains("The Moonlit Kingdom"),
          "Duplicate warning message must mention The Moonlit Kingdom, not an unrelated book: " + res4.getMessage());
      assertFalse(res4.getMessage().contains("Hobbit"),
          "Duplicate warning message must NOT mention The Hobbit: " + res4.getMessage());

      // Verify books table has exactly 1 row for each of the 3 books
      assertEquals(1, ((Number) Sql.query("SELECT COUNT(*) AS c FROM books WHERE title = 'The Moonlit Kingdom'").getFirst().get("c")).intValue());
      assertEquals(1, ((Number) Sql.query("SELECT COUNT(*) AS c FROM books WHERE title = 'The Last Clockmaker of Ashbourne'").getFirst().get("c")).intValue());
      assertEquals(1, ((Number) Sql.query("SELECT COUNT(*) AS c FROM books WHERE title = 'Whispers Beneath the Crimson Sea'").getFirst().get("c")).intValue());

      // Verify My Collection contains all 3 books
      final long f1 = book1Id;
      final long f2 = book2Id;
      final long f3 = book3Id;
      List<Map<String, Object>> myCollection = catalogDao.library(userId, "", false);
      assertTrue(myCollection.stream().anyMatch(b -> ((Number) b.get("id")).longValue() == f1), "Book 1 must be in My Collection");
      assertTrue(myCollection.stream().anyMatch(b -> ((Number) b.get("id")).longValue() == f2), "Book 2 must be in My Collection");
      assertTrue(myCollection.stream().anyMatch(b -> ((Number) b.get("id")).longValue() == f3), "Book 3 must be in My Collection");

    } finally {
      for (long id : List.of(book1Id, book2Id, book3Id)) {
        if (id > 0) {
          Sql.update("DELETE FROM user_library WHERE book_id = ?", id);
          Sql.update("DELETE FROM reading_activity WHERE book_id = ?", id);
          Sql.update("DELETE FROM books WHERE id = ?", id);
          BookCatalogue.removeCustomBook(id);
        }
      }
    }
  }

  @Test
  void testReadingStatusParsing() {
    assertEquals(ReadingStatus.WANT_TO_READ, BookService.parseReadingStatus(null));
    assertEquals(ReadingStatus.WANT_TO_READ, BookService.parseReadingStatus(""));
    assertEquals(ReadingStatus.WANT_TO_READ, BookService.parseReadingStatus("WANT_TO_READ"));
    assertEquals(ReadingStatus.WANT_TO_READ, BookService.parseReadingStatus("To-Read"));
    assertEquals(ReadingStatus.WANT_TO_READ, BookService.parseReadingStatus("want to read"));

    assertEquals(ReadingStatus.CURRENTLY_READING, BookService.parseReadingStatus("CURRENTLY_READING"));
    assertEquals(ReadingStatus.CURRENTLY_READING, BookService.parseReadingStatus("Currently Reading"));
    assertEquals(ReadingStatus.CURRENTLY_READING, BookService.parseReadingStatus("reading"));

    assertEquals(ReadingStatus.COMPLETED, BookService.parseReadingStatus("COMPLETED"));
    assertEquals(ReadingStatus.COMPLETED, BookService.parseReadingStatus("Completed"));
    assertEquals(ReadingStatus.COMPLETED, BookService.parseReadingStatus("finished"));
  }
}
