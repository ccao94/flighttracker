# FlightTracker

![CI](https://github.com/ccao94/flighttracker/actions/workflows/ci.yml/badge.svg)

REST API for tracking real aircraft by their ICAO24 transponder address, built with Spring Boot.
Live positions come from ADS-B data provided by the [OpenSky Network](https://opensky-network.org),
and a scheduled task builds a position history for every aircraft in the fleet.

Personal project, started in September 2026 to learn the Spring ecosystem, following my
internship at Thales AVS (Training & Simulation).

## Stack

- Java 17, Spring Boot 4.1
- Spring Web, Spring Data JPA (Hibernate), Bean Validation, Spring scheduling
- RestClient, OAuth2 client credentials (OpenSky API)
- PostgreSQL 16 (Docker), Flyway migrations
- JUnit 5, Mockito, MockMvc, Testcontainers
- Maven, GitHub Actions

## Architecture

```
aircraft/
├── Aircraft.java                    # JPA entity, ICAO24 as natural primary key
├── AircraftRepository.java          # Spring Data repository, derived query by country
├── AircraftService.java             # fleet business rules, transactions
├── LivePositionService.java         # combines the fleet and OpenSky live data
├── AircraftController.java          # REST endpoints
├── CreateAircraftRequest.java       # input DTO with validation
├── AircraftResponse.java            # output DTO
└── LivePositionResponse.java        # live position DTO, units in field names
position/
├── PositionSnapshot.java            # one recorded position, many-to-one with Aircraft
├── PositionSnapshotRepository.java  # history queries with a row limit
├── PositionHistoryService.java      # deduplicated writes, bounded reads
├── PositionCollector.java           # scheduled collection of the whole fleet
└── PositionSnapshotResponse.java
opensky/
├── OpenSkyProperties.java           # typed configuration
├── OpenSkyTokenProvider.java        # OAuth2 token, cached and refreshed before expiry
├── OpenSkyClient.java               # calls /states/all, one request for many aircraft
├── StateVectorMapper.java           # maps OpenSky positional arrays to records
└── StateVector.java
common/
└── GlobalExceptionHandler.java      # maps exceptions to HTTP errors (RFC 9457)

resources/db/migration/              # Flyway SQL migrations
```

## Run locally

Prerequisites: Java 17+, Docker.

```bash
docker compose up -d db
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`. Flyway applies the database migrations on startup.

## OpenSky credentials

Live positions and the collector need an OpenSky API client
(create one on your [OpenSky account page](https://opensky-network.org/my-opensky/account)).
Credentials are read from environment variables, never from the repository:

```powershell
setx OPENSKY_CLIENT_ID "your_client_id"
setx OPENSKY_CLIENT_SECRET "your_client_secret"
```

The application starts without them, only the OpenSky features need them.

## Position collection

A scheduled task records the position of every aircraft in the fleet, building a history
that the OpenSky REST API does not keep (state vectors are only available for the last hour).

```yaml
flighttracker:
  collector:
    enabled: true
    interval: PT5M
    initial-delay: PT30S
```

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/aircraft` | List all aircraft, optional `?country=` filter |
| GET | `/api/aircraft/{icao24}` | Get one aircraft, 404 if unknown |
| GET | `/api/aircraft/{icao24}/position` | Live position from OpenSky, 404 if the aircraft is not flying |
| GET | `/api/aircraft/{icao24}/history` | Collected positions, most recent first, `?limit=` from 1 to 500 (default 50) |
| POST | `/api/aircraft` | Register an aircraft, 409 if it already exists |

Example requests are in `requests.http` (VS Code REST Client).

Example of a live position response:

```json
{
  "icao24": "39de4d",
  "callsign": "TVF64BH",
  "lastContact": "2026-10-14T16:11:28Z",
  "latitude": 48.6742,
  "longitude": 2.354,
  "baroAltitudeMeters": 998.22,
  "onGround": false,
  "velocityMetersPerSecond": 76.46,
  "trueTrack": 198.43
}
```

## Tests

```bash
./mvnw verify
```

33 tests, run on every push by GitHub Actions:

- **Unit tests** (Mockito): business rules, collector behaviour when OpenSky fails,
  OpenSky array mapping without any network call.
- **Web layer tests** (MockMvc): HTTP status codes, validation and error responses.
- **Integration tests** (Testcontainers): a disposable PostgreSQL container is started,
  Flyway applies the real migrations, and repository queries and constraints are tested
  against it.

Only Docker is required, no local database needs to be running.

## Design notes

**ICAO24 as primary key.** It is the identifier used by ADS-B and OpenSky, so the API stores
it as is, normalized to lowercase. Because `save()` on an existing key silently updates the row,
the service checks `existsById` first and rejects duplicates with a 409.

**DTOs instead of exposing entities or OpenSky data.** Input is validated before reaching the
business layer (6 hexadecimal characters for ICAO24), and neither a schema change nor an
OpenSky format change breaks the API.

**Schema owned by Flyway.** Tables and named constraints are written in versioned SQL
migrations. Hibernate only validates (`ddl-auto: validate`), so an entity that no longer
matches the schema stops the application at startup.

**OpenSky credits are protected.** Live positions are only requested for aircraft registered
in the fleet. An unknown ICAO24 returns a 404 without any call to OpenSky.

**One OpenSky call per collection cycle.** The whole fleet is requested in a single call,
so a cycle costs 1 credit whatever the fleet size: 288 credits per day at a 5 minute interval,
out of 4,000.

**No duplicate positions.** When an aircraft has not transmitted between two cycles, OpenSky
returns the same state again. The service skips it, and a unique constraint on
`(icao24, recorded_at)` guards the database. The same constraint's index serves the history query.

**Writes in a separate service.** `@Transactional` works through a Spring proxy, so a call
between two methods of the same class would silently skip the transaction. The collector
orchestrates, `PositionHistoryService` writes.

**No database transaction around HTTP calls.** Database work runs in short transactions,
so a slow OpenSky response never holds a database connection.

**OAuth2 token handled by hand.** The token is cached in memory and refreshed 30 seconds before
expiry. A 401 triggers a single retry with a fresh token. Spring Security's OAuth2 client could
do this automatically, it is written by hand here to understand the flow.

**Upstream errors are explicit.** OpenSky quota exhausted returns 503, any other OpenSky failure
returns 502. Technical details go to the logs, not to the client.

## Roadmap

- [x] Layered REST API with validation and error handling
- [x] Unit and web layer tests
- [x] OpenSky client with OAuth2 client credentials
- [x] Scheduled collection of positions and flight history
- [x] Flyway migrations
- [x] Integration tests with Testcontainers
- [x] CI with GitHub Actions
- [ ] Flight statistics from the collected history
- [ ] OpenAPI documentation
- [ ] Docker image of the application

## Data source

Aircraft data provided by the [OpenSky Network](https://opensky-network.org),
for non-commercial use.