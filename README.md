# Adventure Book API

A REST API for browsing a collection of adventure books, reading through them, and tracking per-player progress with health/consequence mechanics.

## Tech stack

- **Java 25**
- **Spring Boot 4.1.1** — Spring Web MVC, Spring Data JPA, Bean Validation
- **Jackson 3**
- **H2** (in-memory)
- **Maven**

## Running the app

### Build

```bash
mvn clean compile
```

### Run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. All endpoints are under the `/api/v1` base path.

### Configuration (`application.yaml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:adventurebook
  jpa:
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true
```

The database is **in-memory** — all data (books, categories, player progress) resets every time the app restarts.

You can inspect the database directly at `http://localhost:8080/h2-console` while the app is running — JDBC URL `jdbc:h2:mem:adventurebook`, default user `sa`, no password.

### Seed data

On startup, every `*.json` file in `src/main/resources/books/` is parsed and validated. A book is loaded into the collection only if it satisfies all four validity rules:

- Exactly one `BEGIN` section
- At least one `END` section
- Every `gotoId` resolves to an existing section
- Every non-`END` section has at least one option

Files that fail validation are **skipped, not fatal** — check the startup log for lines like:

```
Skipping invalid book crystal-caverns.json: [Section 666 is type NODE but has no options]
```

## Error response format

Every error returns the same shape, regardless of status code:

```json
{
  "message": "human-readable summary",
  "details": ["optional list of specific violations — empty [] when not applicable"]
}
```
Below is manual `curl` verification per objective, in the order they were built. Replace `{id}`, `{sectionId}`, etc. with real values from your running instance — **ids are randomly generated per run**, never hardcode them.

---

### Objective 1 — List and search books

```bash
# List all
curl "localhost:8080/api/v1/books"

# Search (all filters optional, combine with AND)
curl "localhost:8080/api/v1/books?title=crystal"
curl "localhost:8080/api/v1/books?difficulty=EASY"
curl "localhost:8080/api/v1/books?author=X&difficulty=HARD" 
```
---

### Objective 2 — Book details and category management

```bash
curl "localhost:8080/api/v1/books/{id}"

curl -X POST "localhost:8080/api/v1/books/{id}/categories" \
  -H "Content-Type: application/json" -d '{"category": "horror"}'

curl -X DELETE "localhost:8080/api/v1/books/{id}/categories/HORROR"
```
---

### Objective 3 — Read a book and navigate sections

```bash
curl "localhost:8080/api/v1/books/{id}/sections/begin"
# note the returned section id and each option's "index"

curl "localhost:8080/api/v1/books/{id}/sections/{sectionId}"
# read any specific section directly — should match "begin"'s content if same id
```

Navigating via `choose` is covered under **Objective 4** below, since health tracking was added to the same endpoint immediately after — there is only one live version of `/choose` in the running app.

---

### Objective 4 — Consequences and health

```bash
# Full sequence: begin, then choose with currentHealth supplied by the client
curl "localhost:8080/api/v1/books/{id}/sections/begin"

curl -X POST "localhost:8080/api/v1/books/{id}/sections/{sectionId}/choose" \
  -H "Content-Type: application/json" -d '{"optionIndex": 0, "currentHealth": 10}'
# response includes: section, health, consequenceText, dead, gameOver
```

---

### Objective 5 — Per-player progress

```bash
curl -X POST "localhost:8080/api/v1/players/alice/books/{bookId}/start"
# health 10, at the BEGIN section — persisted to H2

curl -X POST "localhost:8080/api/v1/players/alice/books/{bookId}/choose" \
  -H "Content-Type: application/json" -d '{"optionIndex": 0}'
# no currentHealth in the request — the server already knows it from the DB

curl "localhost:8080/api/v1/players/alice/books/{bookId}/progress"
# matches the last choose response, except consequenceText — that field isn't
# persisted, so progress always reports it as null even after a consequence-bearing choose

# Player isolation
curl -X POST "localhost:8080/api/v1/players/bob/books/{bookId}/start"
curl "localhost:8080/api/v1/players/alice/books/{bookId}/progress"
# alice's progress is untouched by bob starting his own run
```