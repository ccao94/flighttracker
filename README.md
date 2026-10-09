# FlightTracker

REST API for tracking real aircraft by their ICAO24 transponder address, built with Spring Boot.
Designed to use live ADS-B data from the [OpenSky Network](https://opensky-network.org).

Personal project, started in September 2026 to learn the Spring ecosystem, following my
internship at Thales AVS (Training & Simulation).

## Stack

- Java 17, Spring Boot 4.1
- Spring Web, Spring Data JPA (Hibernate), Bean Validation
- PostgreSQL 16 (Docker)
- JUnit 5, Mockito, MockMvc
- Maven

## Architecture

```
aircraft/
├── Aircraft.java                  # JPA entity, ICAO24 as natural primary key
├── AircraftRepository.java        # Spring Data repository, derived query by country
├── AircraftService.java           # business rules, transactions
├── AircraftController.java        # REST endpoints
├── CreateAircraftRequest.java     # input DTO with validation
└── AircraftResponse.java          # output DTO
common/
└── GlobalExceptionHandler.java    # maps exceptions to HTTP errors (RFC 9457)
```

## Run locally

Prerequisites: Java 17+, Docker.

```bash
docker compose up -d db
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`.

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/aircraft` | List all aircraft, optional `?country=` filter |
| GET | `/api/aircraft/{icao24}` | Get one aircraft, 404 if unknown |
| POST | `/api/aircraft` | Register an aircraft, 409 if it already exists |

Example requests are in `requests.http` (VS Code REST Client).

## Tests

```bash
./mvnw test
```

Service tests use Mockito, controller tests use MockMvc. The context test needs the database container running.

## Design notes

**ICAO24 as primary key.** It is the identifier used by ADS-B and OpenSky, so the API stores
it as is, normalized to lowercase. Because `save()` on an existing key silently updates the row,
the service checks `existsById` first and rejects duplicates with a 409.

**DTOs instead of exposing the entity.** Input is validated before reaching the business layer
(6 hexadecimal characters for ICAO24), and the database schema can change without breaking the API.

**Centralized error handling.** Business exceptions are thrown by the service and converted to
HTTP responses in one place, using the standard `ProblemDetail` format.

## Roadmap

- [x] Layered REST API with validation and error handling
- [x] Unit and web layer tests
- [ ] OpenSky client with OAuth2 client credentials
- [ ] Scheduled collection of positions and flight history
- [ ] Aggregation endpoints (flight hours, airports)
- [ ] Flyway migrations, Testcontainers, CI with GitHub Actions