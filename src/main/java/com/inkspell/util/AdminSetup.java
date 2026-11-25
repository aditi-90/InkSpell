package com.inkspell.util;

import com.inkspell.dao.Sql;
import com.inkspell.service.Validation;

public final class AdminSetup {
  public static void main(String[] args) throws Exception {
    var console = System.console();
    String email = System.getenv("ADMIN_EMAIL"), password = System.getenv("ADMIN_PASSWORD");
    if (email == null && console != null) email = console.readLine("Admin email: ");
    if (password == null && console != null)
      password = new String(console.readPassword("Admin password: "));
    email = Validation.email(email);
    Validation.password(password);
    Sql.update(
        "INSERT INTO users(full_name,email,password_hash,role) VALUES(?,?,?,'ADMIN') ON DUPLICATE"
            + " KEY UPDATE role='ADMIN',password_hash=VALUES(password_hash)",
        "Librarian",
        email,
        Passwords.hash(password));
    System.out.println(
        "Admin account created or promoted. Existing account password was replaced with the"
            + " supplied password.");
  }
}
