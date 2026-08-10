# user-service-template

A template Spring Boot microservice for managing **user profile data** — one of a
family of `*-service-template` repos meant to (a) be reused as a starting point for
future projects and (b) demonstrate backend engineering practices.

## Scope & design boundary

This service intentionally owns **profile data only** (name, contact info, status).
It does **not** own credentials, passwords, sessions, or tokens — that responsibility
belongs to a separate `authentication-service-template` (not yet built).

The two services are decoupled by design:

- A `User` profile's `id` is a UUID. It can be supplied by the caller at creation
  time (e.g. once `authentication-service` exists, it will mint the canonical
  identity and pass its UUID here) or generated locally if omitted — which lets
  this service run and be tested completely standalone today.
- There are no synchronous calls between the two services yet. The integration
  point (REST call, shared event, or message queue) is deferred until
  `authentication-service-template` exists, so the boundary is proven out with a
  real second service rather than guessed at upfront.

## Tech stack

- Java 21, Spring Boot 4.1
- Spring Web, Spring Data JPA, Bean Validation
- PostgreSQL + Flyway migrations
- Testcontainers (integration tests run against a real Postgres, not H2)
- Lombok
- Maven

## Running locally

The app uses [Spring Boot's Docker Compose support](https://docs.spring.io/spring-boot/reference/features/dev-services.html):
starting the app also starts a local Postgres container (see `compose.yaml`), no
manual setup required.

```bash
./mvnw spring-boot:run
```

The API is then available at `http://localhost:8080/api/v1/users`.

To point at a different database (e.g. in CI or a real deployment), override:

| Env var       | Default        |
|---------------|----------------|
| `DB_HOST`     | `localhost`    |
| `DB_PORT`     | `5432`         |
| `DB_NAME`     | `user_service` |
| `DB_USERNAME` | `user_service` |
| `DB_PASSWORD` | `user_service` |
| `SERVER_PORT` | `8080`         |

## Running tests

```bash
./mvnw test
```

Unit tests (`UserServiceTest`) use Mockito and don't touch a database.
Integration tests (`UserControllerIntegrationTest`, `UserServiceTemplateApplicationTests`)
spin up a real Postgres container via Testcontainers, so Docker must be running.
`UserServiceTemplateApplicationTests#contextLoads` doubles as an "app boots cleanly"
check - it fails if the full Spring context (config, JPA mappings, Flyway migrations)
can't come up.

A coverage report (JaCoCo) is generated at `target/site/jacoco/index.html` after
`./mvnw test`. CI (`.github/workflows/ci.yml`) runs the same suite on every push/PR
and uploads the report as a build artifact.

## API

| Method | Path                 | Description               |
|--------|----------------------|----------------------------|
| POST   | `/api/v1/users`      | Create a user profile      |
| GET    | `/api/v1/users`      | List all user profiles     |
| GET    | `/api/v1/users/{id}` | Get a user profile by id   |
| PUT    | `/api/v1/users/{id}` | Replace a user profile     |
| DELETE | `/api/v1/users/{id}` | Delete a user profile      |

Validation errors return `400` with an RFC 7807 `ProblemDetail` body (an `errors`
map of field → message). A missing user returns `404`; a duplicate email returns
`409`.

## Internationalization

Error and validation messages are localized based on the `Accept-Language` header.
Supported locales: `en-US` (default/fallback) and `pt-BR`.

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -H 'Accept-Language: pt-BR' \
  -d '{"email":"not-an-email","firstName":"Jane","lastName":"Doe"}'
# -> {"detail":"Falha de validação em um ou mais campos","errors":{"email":"O e-mail deve ter um formato válido"}}
```

Any other/unsupported `Accept-Language` falls back to English. Translations live in
`src/main/resources/messages*.properties`; to add a locale, drop in a new
`messages_xx_XX.properties` file and add it to `LocaleConfig`'s supported list.

### Example

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"jane@example.com","firstName":"Jane","lastName":"Doe"}'
```

## Project structure

```
src/main/java/.../user_service_template/
├── user/                   # domain: entity, repository, service, controller, DTOs
│   └── dto/
├── common/exception/       # domain exceptions + @RestControllerAdvice
└── config/                 # JPA auditing config
src/main/resources/
└── db/migration/           # Flyway migrations
```

## Roadmap

- [ ] `authentication-service-template`: owns credentials/tokens, issues the
      canonical user UUID
- [ ] Decide integration pattern between the two services (sync REST call for
      token validation vs. async "user created/deleted" events)
- [ ] OpenAPI documentation
- [ ] Pagination/filtering on the list endpoint
- [ ] CI workflow (build, test, lint) and Dockerfile for deployment
