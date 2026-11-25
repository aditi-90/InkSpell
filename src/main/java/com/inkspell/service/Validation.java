package com.inkspell.service;

public final class Validation {
  private Validation() {}

  public static String text(String value, int max, String label) {
    if (value == null || value.isBlank() || value.trim().length() > max)
      throw new IllegalArgumentException(label + " is required (maximum " + max + " characters).");
    return value.trim();
  }

  public static String email(String value) {
    String email = text(value, 254, "Email").toLowerCase(java.util.Locale.ROOT);
    if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
      throw new IllegalArgumentException("Enter a valid email address.");
    return email;
  }

  public static String password(String value) {
    if (value == null
        || value.length() < 8
        || value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
        || !value.matches(".*[A-Za-z].*")
        || !value.matches(".*[0-9].*"))
      throw new IllegalArgumentException(
          "Password must have at least 8 characters, a letter and a number; maximum 72 UTF-8"
              + " bytes.");
    return value;
  }

  public static int integer(String value, int min, int max, String label) {
    try {
      int n = Integer.parseInt(value);
      if (n < min || n > max) throw new NumberFormatException();
      return n;
    } catch (Exception e) {
      throw new IllegalArgumentException(label + " must be between " + min + " and " + max + ".");
    }
  }

  public static int progress(int page, Integer total) {
    if (page < 0 || (total != null && total > 0 && page > total))
      throw new IllegalArgumentException(
          "Current page must be between zero and the total page count.");
    return total == null || total <= 0 ? 0 : (int) Math.round(100.0 * page / total);
  }

  public static String cover(String value) {
    if (value == null || value.isBlank()) return null;
    value = text(value, 1000, "Cover URL");
    if (!value.matches("https://covers\\.openlibrary\\.org/[^\\s]+"))
      throw new IllegalArgumentException(
          "Use an HTTPS covers.openlibrary.org cover URL, or leave blank for a generated cover.");
    return value;
  }
}
