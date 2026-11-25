package com.inkspell.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class Passwords {
  private Passwords() {}

  public static String hash(String password) {
    return BCrypt.withDefaults().hashToString(12, password.toCharArray());
  }

  public static boolean verify(String password, String hash) {
    return password != null
        && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72
        && BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
  }
}
