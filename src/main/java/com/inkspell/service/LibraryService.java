package com.inkspell.service;

import com.inkspell.dao.*;
import com.inkspell.model.ReadingStatus;
import java.sql.SQLException;

public class LibraryService {
  public void save(
      long user, long book, String status, String page, String notes, String editionPages)
      throws SQLException {
    ReadingStatus state;
    try {
      state = ReadingStatus.valueOf(status);
    } catch (Exception e) {
      throw new IllegalArgumentException("Choose a valid reading status.");
    }
    var b = new CatalogDao().book(book);
    Integer total =
        b.get("total_pages") == null ? null : ((Number) b.get("total_pages")).intValue();
    Integer override = null;
    if (editionPages != null && !editionPages.isBlank())
      override = Validation.integer(editionPages, 1, 100000, "Your edition's page count");
    if (editionPages == null) {
      var existing =
          Sql.query(
              "SELECT total_pages_override FROM user_library WHERE user_id=? AND book_id=?",
              user,
              book);
      if (!existing.isEmpty() && existing.getFirst().get("total_pages_override") != null)
        override = ((Number) existing.getFirst().get("total_pages_override")).intValue();
    }
    if (override != null) total = override;
    int current = Validation.integer(page, 0, Integer.MAX_VALUE, "Current page");
    Validation.progress(current, total);
    if (state == ReadingStatus.COMPLETED && total != null) current = total;
    if (notes != null && notes.length() > 10000)
      throw new IllegalArgumentException("Notes cannot exceed 10,000 characters.");
    var existingRecord =
        Sql.query(
            "SELECT reading_status, current_page FROM user_library WHERE user_id=? AND book_id=?",
            user,
            book);
    String oldStatus = existingRecord.isEmpty() ? null : String.valueOf(existingRecord.getFirst().get("reading_status"));
    int oldPage = (existingRecord.isEmpty() || existingRecord.getFirst().get("current_page") == null)
        ? 0
        : ((Number) existingRecord.getFirst().get("current_page")).intValue();

    Sql.update(
        "INSERT INTO"
            + " user_library(user_id,book_id,reading_status,current_page,date_started,date_completed,personal_notes,total_pages_override)"
            + " VALUES(?,?,?,?,IF(?='CURRENTLY_READING',CURRENT_DATE,NULL),IF(?='COMPLETED',CURRENT_DATE,NULL),?,?)"
            + " ON DUPLICATE KEY UPDATE"
            + " reading_status=VALUES(reading_status),current_page=VALUES(current_page),date_started=IF(VALUES(reading_status)='CURRENTLY_READING',COALESCE(date_started,CURRENT_DATE),date_started),date_completed=IF(VALUES(reading_status)='COMPLETED',COALESCE(date_completed,CURRENT_DATE),NULL),personal_notes=COALESCE(VALUES(personal_notes),personal_notes),total_pages_override=VALUES(total_pages_override)",
        user,
        book,
        state.name(),
        current,
        state.name(),
        state.name(),
        notes,
        override);

    try {
      String title = b != null ? String.valueOf(b.get("title")) : "a book";
      if (state == ReadingStatus.COMPLETED && !"COMPLETED".equals(oldStatus)) {
        ReadingDashboardService.recordActivity(user, book, "COMPLETED", "Finished reading \"" + title + "\"");
      } else if (state == ReadingStatus.CURRENTLY_READING && !"CURRENTLY_READING".equals(oldStatus)) {
        ReadingDashboardService.recordActivity(user, book, "STARTED", "Began reading \"" + title + "\"");
      } else if (current > oldPage) {
        ReadingDashboardService.recordActivity(user, book, "PROGRESS", "Advanced to page " + current + " of \"" + title + "\"");
      }
    } catch (Exception ignored) {}
  }
}
