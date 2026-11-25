package com.inkspell.util;

import com.inkspell.config.AppConfig;
import java.net.URI;
import java.sql.*;

public final class Database {
  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private Database() {}

  public static Connection open() throws SQLException {
    String rawUrl = AppConfig.get("DATABASE_URL", "");
    if (rawUrl.isBlank()) {
      rawUrl = AppConfig.get("DB_URL", "");
    }

    String user = AppConfig.get("DB_USER", "");
    String password = AppConfig.get("DB_PASSWORD", "");
    String jdbcUrl;

    // Handle URI format (e.g., mysql://user:pass@host:port/dbname from cloud providers like Railway/Render/Aiven)
    if (rawUrl.startsWith("mysql://") || rawUrl.startsWith("mysqls://")) {
      try {
        URI uri = URI.create(rawUrl);
        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 3306;
        String path = uri.getPath();
        String dbName = (path != null && path.length() > 1) ? path.substring(1) : "inkspell_db";
        String userInfo = uri.getUserInfo();
        if (userInfo != null && user.isBlank()) {
          String[] parts = userInfo.split(":", 2);
          user = parts[0];
          if (parts.length > 1 && password.isBlank()) {
            password = parts[1];
          }
        }
        boolean isSsl = rawUrl.startsWith("mysqls://");
        jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8%s",
            host, port, dbName, isSsl ? "&sslMode=REQUIRED" : "&sslMode=PREFERRED");
      } catch (Exception e) {
        jdbcUrl = "jdbc:" + rawUrl;
      }
    } else if (!rawUrl.isBlank()) {
      jdbcUrl = rawUrl.startsWith("jdbc:") ? rawUrl : "jdbc:" + rawUrl;
    } else {
      String host = AppConfig.get("DB_HOST", "localhost");
      String port = AppConfig.get("DB_PORT", "3306");
      String db = AppConfig.get("DB_NAME", "inkspell_db");
      jdbcUrl = String.format(
          "jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&sslMode=PREFERRED",
          host, port, db);
    }

    if (user.isBlank()) {
      throw new SQLException("Database username is required. Configure DB_USER (or DATABASE_URL) in environment.");
    }

    Connection connection = DriverManager.getConnection(jdbcUrl, user, password);

    // Non-fatal UTC timezone synchronization (gracefully handles restricted cloud MySQL environments)
    try (PreparedStatement statement = connection.prepareStatement("SET time_zone = ?")) {
      statement.setString(1, "+00:00");
      statement.execute();
    } catch (SQLException ignored) {
      // serverTimezone=UTC is already enforced via the JDBC URL parameters
    }

    return connection;
  }
}
