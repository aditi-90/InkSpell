package com.inkspell.util;

import com.inkspell.config.AppConfig;
import com.inkspell.dao.BookCatalogue;
import java.nio.file.*;
import java.sql.*;

/**
 * Explicit setup utility to initialize schema, seed site content,
 * and synchronize the 108 hardcoded books into MySQL without altering user data.
 */
public final class DatabaseSetup {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    String user = AppConfig.get("DB_USER", "");
    String password = AppConfig.get("DB_PASSWORD", "");
    String setupUrl = AppConfig.get("DB_SETUP_URL", "");
    
    // Connect either via explicit DB_SETUP_URL or fallback through Database.open()
    Connection connection = null;
    if (!setupUrl.isBlank()) {
      connection = DriverManager.getConnection(setupUrl, user, password);
    } else {
      connection = Database.open();
    }

    try (Connection c = connection) {
      for (String file : new String[] {"schema.sql", "seed_books.sql", "seed_site_content.sql"}) {
        String sql = Files.readString(Path.of("db", file)).replace("\ufeff", "");
        // The committed scripts contain no semicolons within string values.
        for (String statement : sql.split(";")) {
          String trimmed = statement.trim();
          if (!trimmed.isBlank()) {
            try (PreparedStatement ps = c.prepareStatement(trimmed)) {
              ps.execute();
            } catch (SQLException ex) {
              // Gracefully handle statements like CREATE DATABASE or USE when restricted on cloud instances
              if (trimmed.toUpperCase().startsWith("CREATE DATABASE") || trimmed.toUpperCase().startsWith("USE ")) {
                System.out.println("Notice: Database creation/selection managed by cloud host (" + ex.getMessage() + ")");
              } else {
                throw ex;
              }
            }
          }
        }
        if (file.equals("schema.sql")) {
          try (ResultSet columns =
              c.getMetaData()
                  .getColumns(null, null, "user_library", "total_pages_override")) {
            if (!columns.next())
              try (PreparedStatement migration =
                  c.prepareStatement(
                      "ALTER TABLE user_library ADD COLUMN total_pages_override INT NULL"
                          + " CHECK(total_pages_override IS NULL OR total_pages_override>0)")) {
                migration.execute();
              }
          }
        }
        System.out.println("Applied " + file);
      }

      // Synchronize all 108 books across 12 genres into MySQL
      try (PreparedStatement useDb = c.prepareStatement("USE inkspell_db")) {
        useDb.execute();
      } catch (SQLException ignored) {
        // Active database already selected via connection
      }
      BookCatalogue.syncToDatabase(c);
      System.out.println("Synchronized 108 hardcoded books into inkspell_db.");
    } finally {
      // Shutdown MySQL cleanup thread so CLI invocations exit immediately
      try {
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
      } catch (Throwable ignored) {
      }
    }
  }
}
