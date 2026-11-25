# InkSpell

A personal reading sanctuary built with Java 21, JSP, Jakarta Servlets, JDBC and MySQL. The frontend uses HTML, CSS and vanilla JavaScript. Maven packages `target/ROOT.war`; Cargo runs it on a real Apache Tomcat 10.1.61 server at **http://localhost:8080/**.

The product display name is defined once in `AppConfig.APP_NAME`.

## What is included

- Dark literary landing page, featured database book, shelf carousel, responsive navigation, particles, generated cover fallbacks and opt-in synthesized soundscape.
- BCrypt registration, session login/logout, throttling, CSRF, private per-reader collections, progress/statuses, personal edition page counts, notes, bookmarks, ratings, preferences and account management.
- Catalog search by title/author/genre, genre chips, six sort options, pagination, book details and reading dashboard.
- Explainable recommendations and a server-only Gemini assistant with catalog context, bounded conversation history and rate limits.
- Admin catalog CRUD, featured selection, editable landing text and testimonial CRUD. Saved catalog books cannot be deleted.
- 65 real seed books across 13 genres, three visibly fictional testimonials, schema and cleanup scripts.
- JUnit service tests, opt-in real HTTP/MySQL integration tests and PowerShell endpoint smoke tests.

## Prerequisites and local setup

JDK 21, MySQL 8.0.16+ (CHECK constraints enforced), and outbound HTTPS for Maven dependencies, Tomcat and optional covers/fonts/Gemini. Maven 3.9+ is recommended; `scripts/run-local.ps1` and `.sh` download Maven 3.9.9 into ignored `.tools/` if it is missing. Tomcat is downloaded automatically by Cargo. Tomcat 10.1 uses **jakarta.servlet**, not javax.

### Database option A: existing MySQL

Copy `config/inkspell.properties.example` to `config/inkspell.properties`. Set `DB_USER` and `DB_PASSWORD` to credentials you supply. `DB_URL` targets your application database. Environment variables of the same names override the file. No default database password is supplied.

Run, from the repository root (replace `mvn` with `.\.tools\apache-maven-3.9.9\bin\mvn.cmd` if needed):

```powershell
mvn compile exec:java '-Dexec.mainClass=com.inkspell.util.DatabaseSetup'
```

The setup utility applies `db/schema.sql`, `db/seed_books.sql`, then `db/seed_site_content.sql`. It uses `DB_SETUP_URL` (default `jdbc:mysql://localhost:3306/?serverTimezone=UTC`); set that separately for a custom server/port. The setup user needs permission to create/use `inkspell_db`. For everyday use, configure a dedicated account limited to this database.

Alternatively use the MySQL client interactively:

```text
mysql -u YOUR_USER -p
source db/schema.sql
source db/seed_books.sql
source db/seed_site_content.sql
```

Setup is safe to rerun for ordinary seeding: genres/content/ISBN books and named placeholders are not duplicated. Do not rerun seeds after intentionally clearing demo content unless you want it restored.

### Database option B: isolated project instance on Windows

The inspected computer has MySQL 8.0.44 installed at the default Program Files path. To use a separate project database without changing the existing Windows service:

```powershell
pwsh -File scripts/start-project-db.ps1
.\.tools\apache-maven-3.9.9\bin\mvn.cmd compile exec:java '-Dexec.mainClass=com.inkspell.util.DatabaseSetup'
```

On first initialization, this creates `.tools/mysql-data`, generates random credentials, writes the ignored local config, and starts MySQL on **127.0.0.1:3307**. It refuses to overwrite an existing configuration. Supply `-MySqlBinary` if mysqld.exe is i0nstalled elsewhere. Keep `.tools/mysql-data` to retain your books/accounts; it is not a backup and is not committed. Subsequent runs start the same database. Logs and the process ID are in `.tools/`. The initial SQL containing random credentials is ignored; it can be deleted once first startup succeeds. Do not point any cleanup command at the existing MySQL service's data directory.

### Start or restart the app

```powershell
pwsh -File scripts/run-local.ps1
```

Linux/macOS: `sh scripts/run-local.sh` after configuring a real MySQL server.

Open ****. Stop Tomcat with Ctrl+C in its terminal, or `mvn cargo:stop`, then rerun the script to restart. The project database runs independently; start it first after a computer restart. For manual deployment, copy `target/ROOT.war` to Tomcat 10.1's webapps directory. Set JVM property `-Dinkspell.config=ABSOLUTE_PATH_TO_CONFIG` or use environment variables. Deploying as another WAR name changes the context path; all application URLs use that context automatically.

## Gemini & Enchanted Librarian configuration

InkSpell features a dual-mode AI reading companion:
1. **Sanctuary Intelligence (Built-in & Free)**: By default, the Enchanted Librarian operates autonomously using InkSpell's curated 108-tome catalogue across all 12 magical realms. It provides atmospheric recommendations, answers questions about authors, titles, themes, and moods, and requires no external API key, internet connection, or quota.
2. **Google Gemini Live Integration**: To enable open-ended generative conversation with Google's Gemini models, obtain a free API key from [Google AI Studio](https://aistudio.google.com/app/apikey) and add it to `config/inkspell.properties`:
   ```properties
   GEMINI_API_KEY=AIzaSy...Your_Key_Here
   ```
   Or set the environment variable `$env:GEMINI_API_KEY = "your_key"` before launching Tomcat. `GEMINI_MODEL` defaults to `gemini-2.5-flash` (or `gemini-1.5-flash`) and can be adjusted in `config/inkspell.properties`.

The implementation follows Google's [model documentation](https://ai.google.dev/gemini-api/docs/models/gemini-2.5-flash) and [generateContent reference](https://ai.google.dev/api/generate-content). Requests use `HttpClient`, `x-goog-api-key`, system instructions, and user/model content parts. No API key is ever exposed to JSP or client-side JavaScript. If the Gemini API call exceeds quota or loses network connection, it smoothly falls back to the Sanctuary Intelligence so your live project demonstration is never interrupted.

## Create or promote an admin

Supply `ADMIN_EMAIL` and `ADMIN_PASSWORD` through your process environment, then run:

```powershell
mvn compile exec:java '-Dexec.mainClass=com.inkspell.util.AdminSetup'
```

The utility hashes your supplied password with BCrypt. An existing email is promoted and its password is replaced with the supplied password. A console-capable direct Java invocation also supports prompts. Maven exec usually has no console, so use environment variables there. Remove admin environment variables afterward. Log out and back in to refresh an existing session's role. No admin password or seeded account is provided.

## Replace demo content

Run `pwsh -File scripts/clear-demo.ps1` or execute `db/clear_demo_data.sql` through the MySQL client. It deletes demo books **only when no reader has saved them**, removes fictional placeholder testimonials, and selects a remaining featured book if any remain. Ratings/bookmarks for deleted catalog books cascade; saved reading records and accounts are preserved.

Then enter `/admin`: add/edit your books, choose a featured tome, edit introductory text and replace testimonials. ISBNs resolve to Open Library covers; explicit cover URLs currently accept HTTPS `covers.openlibrary.org` only, matching CSP. Missing images fall back to a generated leather-style cover. Keep ISBNs matched to the title/edition. Unknown pages/years stay NULL; edit them when you have verified edition details. Optional CSV import is not implemented.

## How recommendations work

Each preferred genre earns 3 points. Genres of completed books earn 3 more; currently reading books earn 2; a saved book rated 4–5 earns 4 extra. Candidates in positively weighted genres exclude anything already in your library. A book's score is its genre weight plus `average rating × 0.15` and `min(save count, 20) × 0.02`. Rating and popularity remain small so taste leads. Stable ties retain recently added order. New readers receive a cross-genre fallback with a featured bonus, ratings and popularity. A first pass takes up to two books per genre for variety, then fills remaining slots from the ranked list. Up to eight real catalog books are returned. This is deliberately simple content-based matching, not collaborative filtering.

## Tests and click-through

```powershell
mvn test
pwsh -File scripts/smoke-test.ps1
mvn test '-Dinkspell.integration=true'
```

The integration test requires the running WAR and the same local MySQL configuration as Maven. It creates disposable accounts and one temporary book, exercises real endpoints and cleans its records afterward. Run it against a development database, with no Gemini key configured for its missing-key assertion. Unit tests do not require MySQL. The smoke script prints PASS/FAIL and exits nonzero on failure.

Manual checks:

1. Browse Home, shelf arrows, Discover search/genres/sorts, pagination and Book Details.
2. Register; add a book, change status/page, save notes, rate it, bookmark it, check Library and Dashboard.
3. Save favorite genres; inspect recommendations and the Librarian's missing-key message or live reply.
4. Use a second account and confirm the first reader's notes/collection stay private.
5. Promote your account with the utility, log in again, then edit books, featured selection, landing text and testimonials.
6. Open mobile navigation with keyboard, check narrow/desktop layouts, toggle Soundscape (never autoplays), test a failed cover URL and reduced-motion mode.

## Structure and viva notes

`model`: reading status enum; `dao`: JDBC catalog and small prepared-statement helper; `service`: validation, reading lifecycle, recommendations, admin transactions and Gemini; `controller`: page/auth/action/admin/chat servlets; `filter`: auth/CSRF/security headers; `config`: application configuration and cookie setup; `util`: database, BCrypt, throttling and explicit CLI setup.

JSPs are views under `WEB-INF`, so users cannot request them directly. Shared layout fragments live in `WEB-INF/includes`. Library ownership is enforced in SQL using the authenticated session's user ID, never a browser-supplied user ID. Transactions protect multi-step writes. Database relations and unique constraints enforce ownership relationships and one save/rating/bookmark per reader/book. Runtime errors use friendly pages; logs exclude passwords/keys/hash values.

## Troubleshooting and limitations

- Database unavailable: check the server is running, port and DB_URL match, credentials are set, and schema/seed scripts ran. The rest of the server still returns a friendly setup page.
- Port 8080 in use: stop the other server or change `cargo.servlet.port` in pom.xml and your smoke-test base URL.
- Login/CSRF failure: reload the form after a session expires. Cookies must be enabled. Sessions expire after 30 minutes.
- Fonts/covers offline: local serif/sans fallbacks and generated covers remain usable.
- AI errors: check key, model, regional availability, network and quota. External 4xx/5xx/429/timeouts receive friendly messages.
- The development cookie is HttpOnly + SameSite=Lax for local HTTP. Use HTTPS and Secure cookies when deploying publicly, along with production database credentials, proxy limits and monitoring.
- In-memory throttling and chat history reset when Tomcat restarts; this is a single-server student project. JDBC uses short-lived connections rather than a production pool.
- Reference screenshots were not supplied, so visual matching against them is unverified. Some Gothic classics are grouped into the requested Dark Romance shelf as a broad editorial category.
- Live Gemini success and external API failure responses require a real API key and were not verified without one. The optional CSV importer is omitted. No claim is made that every edition's remote cover is guaranteed available.

See `docs/QA.md` for the checks actually performed during this build.
