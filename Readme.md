# Request Management Application
A REST service for managing the lifecycle of requests (Java 21, Spring Boot 3.5, Maven, H2 database).



### The project is built using a hexagonal architecture.




## Architecture

The project is split into 4 Maven modules. The domain logic lives in the `domain` module and is fully isolated from the infrastructure and the HTTP layer.

| Module           | Depends on                        | Responsibility                                                                                    |
|------------------|-----------------------------------|---------------------------------------------------------------------------------------------------|
| `domain`         | no modules                        | Model (`Request`, `RequestState`, `StateChange`), business rules (`RequestService`), ports (`RequestRepository`, `PublicationNumberGenerator`), business exceptions |
| `infrastructure` | `domain`                          | Outbound adapters: JPA/H2 persistence, publication number generation from a database sequence     |
| `api`            | `domain`                          | Inbound adapter: REST controller, DTOs with validation, mapping of domain exceptions to HTTP codes, Swagger |
| `application`    | `domain`, `infrastructure`, `api` | Spring Boot entry point and configuration (`application.properties`)                              |

## Business model

### States and transitions

```
CREATED ──► VERIFIED ──► ACCEPTED ──► PUBLISHED
   │            │            │
   ▼            ▼            ▼
DELETED      REJECTED     REJECTED
```

| State       | Allowed transitions      | Body change |
|-------------|--------------------------|-------------|
| `CREATED`   | `VERIFIED`, `DELETED`    | yes         |
| `VERIFIED`  | `ACCEPTED`, `REJECTED`   | yes         |
| `ACCEPTED`  | `PUBLISHED`, `REJECTED`  | no          |
| `PUBLISHED` | none (final state)       | no          |
| `DELETED`   | none (final state)       | no          |
| `REJECTED`  | none (final state)       | no          |

### Rules

- `name` and `body` are required on creation (`name` is at most 255 characters).
- The body can only be changed in the `CREATED` and `VERIFIED` states.
- Deleting (`DELETED`) and rejecting (`REJECTED`) require a non-blank `reason`.
- On publication the request gets a unique publication number. Numbers come from a database sequence, so they are unique and increasing, and an attempt to publish from a disallowed state does not consume a number.
- Every state change (including creation) is recorded in the history (audit log): previous state, new state, reason and time of the change. The history is append-only.

## API

Base path: `/api/v1/application-requests`

| Method | Path             | Description                                                  | Body                  |
|--------|------------------|--------------------------------------------------------------|-----------------------|
| POST   | `/`              | Create a request (state `CREATED`), returns 201              | `{name, body}`        |
| GET    | `/`              | List with pagination and filters                             | -                     |
| GET    | `/{id}`          | Get a request                                                | -                     |
| PATCH  | `/{id}`          | Change the body (only `CREATED`, `VERIFIED`)                 | `{body}`              |
| POST   | `/{id}/verify`   | `CREATED` → `VERIFIED`                                       | -                     |
| POST   | `/{id}/accept`   | `VERIFIED` → `ACCEPTED`                                      | -                     |
| POST   | `/{id}/publish`  | `ACCEPTED` → `PUBLISHED`, assigns a publication number       | -                     |
| POST   | `/{id}/reject`   | `VERIFIED`/`ACCEPTED` → `REJECTED`                           | `{reason}`            |
| POST   | `/{id}/delete`   | `CREATED` → `DELETED`                                        | `{reason}`            |
| GET    | `/{id}/history`  | History of state changes (audit log)                         | -                     |

### Listing (browsing)

`GET /api/v1/application-requests?name=&state=&page=&size=`

- `name` - part of the name, case-insensitive (optional),
- `state` - exact state, e.g. `VERIFIED` (optional),
- `page` - page number, zero-based (default 0),
- `size` - page size (default 10).

Results are sorted by creation order. The response uses the Spring Data `PagedModel` format: `content` with the requests and `page` with the pagination info (`size`, `number`, `totalElements`, `totalPages`).

### Error handling

Errors are returned as `ProblemDetail` (RFC 9457).

| Domain exception               | HTTP code | When                                                                |
|--------------------------------|-----------|---------------------------------------------------------------------|
| `RequestNotFoundException`     | 404       | no request with the given id                                        |
| `InvalidRequestDataException`  | 400       | blank `name`, `body` or `reason`                                    |
| `InvalidRequestStateException` | 409       | disallowed state transition or body change in a disallowed state    |

In addition, DTO validation (`@Valid`) and invalid parameter formats (e.g. unknown `state`, malformed UUID) return 400.

### Example

```bash
curl -X POST localhost:8080/api/v1/application-requests \
  -H 'Content-Type: application/json' \
  -d '{"name":"Invoice","body":"Content"}'

curl -X POST localhost:8080/api/v1/application-requests/{id}/verify
curl -X POST localhost:8080/api/v1/application-requests/{id}/accept
curl -X POST localhost:8080/api/v1/application-requests/{id}/publish
curl 'localhost:8080/api/v1/application-requests?state=PUBLISHED&name=inv&size=5'
```

## Swagger documentation

After the application starts:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI specification: http://localhost:8080/v3/api-docs

## Running

Requirements: JDK 21+ and Maven.

```bash
mvn clean package
java -jar application/target/application-*.jar
```

The application starts on port 8080.

## Tests

```bash
mvn test
```

- `domain` - unit tests (JUnit 5, Mockito, AssertJ): state graph, service business rules, change history,
- `infrastructure` - repository tests on H2 (`@DataJpaTest`): saving, history, filtering and pagination, publication number uniqueness, sequence,
- `api` - controller tests (`@WebMvcTest`): response codes, DTO validation, exception mapping,
- `application` - full Spring context startup test.

## Assumptions

- The database is in-memory H2 (`jdbc:h2:mem:requests`), so data is lost when the application restarts.
- The publication number sequence is created by the `schema.sql` script and read with an H2-specific native query (`next value for`). It must be adapted when switching to another database.
- There is no authentication or authorization, as it was not part of the requirements.
- A history entry is created only on a state change. Changing only the body is not recorded.

## AI usage

Claude AI was used to generate the project skeleton and part of the tests.
According to the recruiter's guidelines, the use of AI tools was allowed.
