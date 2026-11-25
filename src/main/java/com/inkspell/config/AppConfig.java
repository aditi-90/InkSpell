package com.inkspell.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class AppConfig {
  public static final String APP_NAME = "InkSpell";
  public static final String GREETING =
      "Greetings, traveler. Tell me what kind of story your heart is searching for.";
  private static final Properties LOCAL = new Properties();

  static {
    Path path = Path.of(System.getProperty("inkspell.config", "config/inkspell.properties"));
    if (Files.exists(path))
      try (InputStream in = Files.newInputStream(path)) {
        LOCAL.load(in);
      } catch (IOException e) {
        throw new IllegalStateException("Cannot read local configuration", e);
      }
  }

  private AppConfig() {}

  public static String get(String name, String fallback) {
    String env = System.getenv(name);
    return env != null ? env : LOCAL.getProperty(name, fallback);
  }
}
