# linguanest-backend
Java/Spring Boot API for AI-generated language exercises and multimodal grading, plus infra-as-code (Terraform, Kubernetes manifests for EKS) and other backend concerns (DB migrations, async worker, job queue config).

## Stack

- Java 25 (LTS)
- Spring Boot 4.1 (Spring Framework 7), constructor-based dependency injection via Lombok's `@RequiredArgsConstructor`
- Maven (via the included wrapper, `mvnw`/`mvnw.cmd` — no local Maven install required)

## Running locally

Needs a Postgres instance reachable at `spring.datasource.url` (defaults to
`jdbc:postgresql://localhost:5432/linguanest`, overridable via the `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`
env vars) — Flyway migrations (`src/main/resources/db/migration`) run against it on startup, and
`spring.jpa.hibernate.ddl-auto=validate` means the app won't start if the schema is missing or out of sync.

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. Health check: `GET /actuator/health`.

Example API, demonstrating the controller → service → repository DI chain (`src/main/java/com/linguanest/backend/chat`):

```bash
curl -X POST localhost:8080/chats \
  -H 'Content-Type: application/json' \
  -d '{"title":"Spanish practice"}'

curl localhost:8080/chats

curl localhost:8080/chats/<chatId>/messages
```

## Tests

```bash
./mvnw test
```

## Developing in an IDE

The project is a standard Maven layout (`pom.xml` at the root) — any IDE with Maven support (IntelliJ, Eclipse via m2e, or VS Code with the "Extension Pack for Java") will import it and resolve dependencies automatically. IDE-specific project files (`.idea/`, `.project`, `.classpath`, `.vscode/`) are intentionally gitignored since each contributor's IDE regenerates them locally.
