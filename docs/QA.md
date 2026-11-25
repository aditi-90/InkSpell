# Verification performed — 21 October 2026

Environment: Temurin Java 21.0.12.1, Maven 3.9.9, Apache Tomcat 10.1.61, actual MySQL Community Server 8.0.44. The app runs as ROOT at http://localhost:8080/. The isolated project database uses port 3307; the pre-existing MySQL80 Windows service was not changed.

## Automated checks

- Maven WAR build passed. Ten JUnit service tests passed: progress arithmetic, unknown totals, invalid progress, email/password validation, BCrypt verification, recommender scoring and cover URL restrictions.
- Real HTTP/MySQL integration suite passed repeatedly. It uses two separate cookie sessions and disposable accounts. Checked registration/duplicate/invalid data, login failure/success, protected redirects, CSRF rejection, catalog search/filter/six sorts/pagination/empty results, book details, all private pages, unique library saves, reading transitions, private notes escaping, cross-user isolation, edition-specific progress and completion, rating bounds/updates/averages, bookmark toggle, preferences, recommendation exclusions, profile/password/account deletion, logout, non-admin denial and admin book/testimonial CRUD/featured invariants.
- Missing-key chat setup message, empty chat rejection and conversation reset passed. No model answer is fabricated.
- PowerShell smoke script: 19 endpoint/protection/CSRF checks passed, zero failures.
- Explicit demo-cleanup integration test passed separately: unsaved demo books and placeholder testimonials were removed; saved books remained. Seed catalog and fictional testimonials were then restored.
- Local secret values and configuration exclusions are checked before commits/push; real config, generated MySQL initialization SQL, data and WAR outputs are ignored.

Run `mvn test` for unit tests; `mvn test -Dinkspell.integration=true` with the server running for HTTP tests. The cleanup test is deliberately separate: `mvn test -Dtest=DemoCleanupTest -Dinkspell.cleanupTest=true`, only against a disposable development catalog. See README for configuration.

## Actual browser checks

Used real navigation, mouse clicks and input against the running WAR:

- Registration and login, inside-page navigation, adding to collection, heart toggle and state feedback.
- Book status, personal edition page count, private notes, rating and persistence after reload.
- Dashboard 25% progress, then quick-update to 30%, verified on both reading cards.
- Preferred genres persisted after reload; profile name edit persisted.
- Librarian missing-key message and new-conversation reset.
- Landing anchor scrolling, shelf Next arrow (scroll position changed), Soundscape ON/OFF state.
- All inside pages opened at 390px: Library, Bookmarks, Preferences, Account, Discover, plus Assistant. No document horizontal overflow in measured pages.
- Landing checked at 390, 768, 1280, 1440 and 1860px. Navigation collapses at tablet/phone widths and stays on one line at desktop widths. Measured scroll widths did not exceed viewport widths after fixes.
- Browser console had no JavaScript errors in the inspected flows. Screenshot evidence is saved locally in ignored `test-results/dashboard.png`.

## Issues found and fixed

1. Default servlet routing initially intercepted CSS/JS. Static assets now forward to Tomcat's default resource servlet.
2. A web.xml cookie-attribute declaration prevented deployment. SameSite=Lax is now set through the Servlet 6 session-cookie API; HttpOnly remains in web.xml.
3. JSP fragments initially decoded decorative characters incorrectly. Every view/include now explicitly declares UTF-8.
4. The first API-only tests did not catch a browser form problem: FormData sent multipart bodies, which the plain servlet was not parsing. Shared AJAX forms now submit URL-encoded fields. Actual UI saves were retested after this fix.
5. Desktop navigation overflow near 1440px was fixed with the compact-navigation breakpoint. Decorative layers cannot intercept mouse events.
6. Bookmark and shelf states now initialize from each reader's real database rows.
7. Dashboard Continue Reading now selects currently reading records, with a separate recently added section and quick page updates.
8. Readers can supply the page count of their own edition without altering the global catalog; progress validation uses that private total.
9. JDBC sessions use UTC consistently, fixing a timezone offset in displayed timestamps.
10. Recommendation fallbacks now make a first pass of up to two books per genre, then fill remaining slots when choices are limited.

## Verification limits

No reference images were supplied, so pixel matching is unverified. Remote fonts and covers depend on availability; missing covers fall back safely. Live Gemini success, real upstream quota/network failures, and model-quality evaluation need an API key and were not tested. Browser password changes and account deletion were tested through HTTP rather than UI; their actual server flows passed. Optional CSV import is not implemented. This is a student application, not an exhaustive production security audit or a guarantee against every possible bug.
