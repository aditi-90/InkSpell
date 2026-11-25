package com.inkspell.util;

import java.nio.file.*;
import java.sql.*;

public final class ClearDemo {
  public static void main(String[] args) throws Exception {
    try (Connection c = Database.open()) {
      for (String sql : Files.readString(Path.of("db/clear_demo_data.sql")).split(";"))
        if (!sql.isBlank())
          try (PreparedStatement s = c.prepareStatement(sql)) {
            s.execute();
          }
    }
    System.out.println(
        "Removed unsaved demo books and placeholder testimonials. Saved books were preserved.");
  }
}
