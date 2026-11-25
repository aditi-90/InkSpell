package com.inkspell;

import static org.junit.jupiter.api.Assertions.*;

import com.inkspell.service.*;
import com.inkspell.util.Passwords;
import java.util.*;
import org.junit.jupiter.api.Test;

class ServicesTest {
  @Test
  void progressIsAccurate() {
    assertEquals(25, Validation.progress(50, 200));
    assertEquals(100, Validation.progress(200, 200));
    assertEquals(0, Validation.progress(0, 200));
  }

  @Test
  void unknownTotalsAreSafe() {
    assertEquals(0, Validation.progress(34, null));
    assertEquals(0, Validation.progress(0, 0));
  }

  @Test
  void impossibleProgressIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> Validation.progress(-1, 100));
    assertThrows(IllegalArgumentException.class, () -> Validation.progress(101, 100));
  }

  @Test
  void validEmailIsNormalized() {
    assertEquals("reader@example.com", Validation.email(" Reader@Example.com "));
  }

  @Test
  void invalidEmailIsRejected() {
    for (String s : new String[] {"bad", "a@b", "a b@c.com", ""})
      assertThrows(IllegalArgumentException.class, () -> Validation.email(s));
  }

  @Test
  void passwordRulesAreEnforced() {
    assertEquals("Chapter123", Validation.password("Chapter123"));
    for (String s : new String[] {"short1", "abcdefgh", "12345678", ""})
      assertThrows(IllegalArgumentException.class, () -> Validation.password(s));
  }

  @Test
  void bcryptRejectsWrongPasswords() {
    String hash = Passwords.hash("Chapter123");
    assertTrue(Passwords.verify("Chapter123", hash));
    assertFalse(Passwords.verify("wrong123", hash));
    assertFalse(hash.contains("Chapter123"));
  }

  @Test
  void recommendationPrioritizesTaste() {
    assertTrue(RecommendationService.score(3, 0, 0) > RecommendationService.score(0, 5, 1000));
  }

  @Test
  void recommendationUsesRatingAndCapsPopularity() {
    assertTrue(RecommendationService.score(3, 5, 2) > RecommendationService.score(3, 2, 0));
    assertEquals(RecommendationService.score(3, 5, 20), RecommendationService.score(3, 5, 2000));
  }

  @Test
  void coverUrlsAreRestricted() {
    assertNull(Validation.cover(""));
    assertThrows(IllegalArgumentException.class, () -> Validation.cover("javascript:alert(1)"));
    assertThrows(
        IllegalArgumentException.class, () -> Validation.cover("https://evil.example/a.jpg"));
  }

  @Test
  void enchantedLibrarianSanctuaryIntelligenceRepliesWarmly() throws Exception {
    GeminiService service = new GeminiService();

    // 1. Greeting
    String greetingReply =
        service.reply(List.of(Map.of("role", "user", "text", "hello")), List.of(), List.of());
    assertNotNull(greetingReply);
    assertTrue(greetingReply.contains("Enchanted Librarian"));
    assertTrue(greetingReply.contains("108"));

    // 2. Genre query ('fantasy' - exactly what was asked in user's screenshot)
    String fantasyReply =
        service.reply(List.of(Map.of("role", "user", "text", "fantasy")), List.of(), List.of());
    assertNotNull(fantasyReply);
    assertTrue(fantasyReply.contains("Fantasy"));
    assertTrue(fantasyReply.contains("Seek a Story"));

    // 3. Spooky / Gothic query
    String gothicReply =
        service.reply(
            List.of(Map.of("role", "user", "text", "tell me spooky gothic haunted house stories")),
            List.of(),
            List.of());
    assertNotNull(gothicReply);
    assertTrue(gothicReply.contains("Gothic") || gothicReply.contains("Haunting"));
  }
}
