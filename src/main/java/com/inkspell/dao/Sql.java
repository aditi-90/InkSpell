package com.inkspell.dao;

import com.inkspell.util.Database;
import java.sql.*;
import java.util.*;

/** Small JDBC helper. All caller-supplied values are bound parameters. */
public final class Sql {
  private Sql() {}

  public static List<Map<String, Object>> query(String sql, Object... args) throws SQLException {
    try (Connection c = Database.open()) {
      return query(c, sql, args);
    }
  }

  public static List<Map<String, Object>> query(Connection c, String sql, Object... args)
      throws SQLException {
    try (PreparedStatement s = c.prepareStatement(sql)) {
      bind(s, args);
      try (ResultSet rs = s.executeQuery()) {
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
            row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
          rows.add(row);
        }
        return rows;
      }
    }
  }

  public static int update(String sql, Object... args) throws SQLException {
    try (Connection c = Database.open()) {
      return update(c, sql, args);
    }
  }

  public static int update(Connection c, String sql, Object... args) throws SQLException {
    try (PreparedStatement s = c.prepareStatement(sql)) {
      bind(s, args);
      return s.executeUpdate();
    }
  }

  private static void bind(PreparedStatement s, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) s.setObject(i + 1, args[i]);
  }
}
