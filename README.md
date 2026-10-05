# Adventure Book API

A REST API for browsing a collection of adventure books, reading through them, and tracking per-player progress with health/consequence mechanics.

## Tech stack

- **Java 25**
- **Spring Boot 4.1.1** — Spring Web MVC, Spring Data JPA, Bean Validation
- **Jackson 3**
- **H2** (in-memory)
- **Flyway** — database schema migrations
- **MapStruct** — DTO mapping
- **Maven**

## Architecture

The code is organized by feature first and by role second. Each feature owns its domain model, exceptions, web layer and persistence.

```
com.adventurebook
├── book/              catalog: owns the Book aggregate
│   ├── BookService, BookController, BookRepository, SectionRepository
│   ├── domain/        Book, Section, Option, Consequence, Category, HealthRules, …
│   ├── exception/     BookNotFound, SectionNotFound, InvalidOption, InvalidDifficulty
│   ├── dto/           request and response records
│   ├── loader/        JSON parsing, validation and startup seeding
│   └── persistence/   JPA entities and repository implementations
├── adventure/         stateless gameplay: begin, read a section, choose an option
│   └── dto/
├── player/            per-player progress
│   ├── PlayerProgressService, PlayerController, PlayerProgressRepository
│   ├── domain/        PlayerProgress, ProgressStatus
│   ├── exception/
│   ├── dto/
│   └── persistence/
└── common/
    ├── exception/     NotFoundException, ConflictException, InvalidRequestException
    └── web/           GlobalExceptionHandler, ErrorResponse
```

### Dependency rules

- Features depend on each other in one direction only: `player → adventure → book`. Every feature can use `common`, and `common` depends on no feature.
- Services depend on repository interfaces (`BookRepository`, `SectionRepository`, `PlayerProgressRepository`) defined at the root of `book` and `player`. The JPA implementations in `persistence/` are the only code that touches entities and Spring Data, and nothing outside `persistence/` uses them.
- `domain/` and `exception/` contain plain Java: no Spring, Jakarta or Hibernate. The domain holds the game rules (`Consequence.applyTo`, `Section.option`) and doesn't depend on services, DTOs or persistence.
- Feature exceptions extend one of the three base types in `common.exception`. `GlobalExceptionHandler` maps each base type to an HTTP status, so a new exception gets the right status without changes to `common`.

## Running the app

### Build

```bash
./mvnw clean compile
```

### Run

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. All endpoints are under the `/api/v1` base path.

### Test

```bash
./mvnw test
```

### Configuration (`application.yaml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:adventurebook
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
  h2:
    console:
      enabled: true
```

The schema is created by Flyway from `src/main/resources/db/migration` on startup. Hibernate only validates that the entities match it (`ddl-auto: validate`), so schema changes go in a new migration file, not in the entities alone.

The database is **in-memory** — all data (books, categories, player progress) resets every time the app restarts.

You can inspect the database directly at `http://localhost:8080/h2-console` while the app is running — JDBC URL `jdbc:h2:mem:adventurebook`, default user `sa`, no password.

### Seed data

On startup, every `*.json` file in `src/main/resources/books/` is parsed and validated. A book is loaded into the collection only if it satisfies all four validity rules:

- Exactly one `BEGIN` section
- At least one `END` section
- Every `gotoId` resolves to an existing section
- Every non-`END` section has at least one option

Files that fail validation, or can't be parsed, are **skipped, not fatal** — check the startup log for lines like:

```
Skipping invalid book crystal-caverns.json: [Section 666 is type NODE but has no options]
```

Seeding is idempotent: each book records the file it came from, and a file that is already loaded is skipped.

## Error response format

Every error returns the same shape, regardless of status code:

```json
{
  "message": "human-readable summary",
  "details": ["optional list of specific violations — empty [] when not applicable"],
  "errorId": null
}
```

`errorId` is only set on `500` responses. It's also written to the server log next to the stack trace, so a client can quote it when reporting a problem.

| Status | When |
|--------|------|
| `400` | Request body fails validation, non-numeric path variable, unknown difficulty, option index out of range |
| `404` | Book, section, or player progress not found |
| `409` | Choosing in a finished adventure, or a concurrent update to the same player's progress |
| `500` | Anything unexpected |

## Manual verification

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
# calling start again during an in-progress run returns the current progress;
# after the run ends (dead or END section), start begins a new run

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