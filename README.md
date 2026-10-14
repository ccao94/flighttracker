# FlightTracker

REST API for tracking real aircraft by their ICAO24 transponder address, built with Spring Boot.
Live positions come from ADS-B data provided by the [OpenSky Network](https://opensky-network.org).

Personal project, started in September 2026 to learn the Spring ecosystem, following my
internship at Thales AVS (Training & Simulation).

## Stack

- Java 17, Spring Boot 4.1
- Spring Web, Spring Data JPA (Hibernate), Bean Validation
- RestClient, OAuth2 client credentials (OpenSky API)
- PostgreSQL 16 (Docker)
- JUnit 5, Mockito, MockMvc
- Maven

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
opensky/
├── OpenSkyProperties.java           # typed configuration
├── OpenSkyTokenProvider.java        # OAuth2 token, cached and refreshed before expiry
├── OpenSkyClient.java               # calls /states/all
├── StateVectorMapper.java           # maps OpenSky positional arrays to records
└── StateVector.java
common/
└── GlobalExceptionHandler.java      # maps exceptions to HTTP errors (RFC 9457)
```

## Run locally

Prerequisites: Java 17+, Docker.

```bash
docker compose up -d db
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`.

## OpenSky credentials

The live position endpoint needs an OpenSky API client
(create one on your [OpenSky account page](https://opensky-network.org/my-opensky/account)).
Credentials are read from environment variables, never from the repository:

```powershell
setx OPENSKY_CLIENT_ID "your_client_id"
setx OPENSKY_CLIENT_SECRET "your_client_secret"
```

The application starts without them, only the live position endpoint needs them.

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/aircraft` | List all aircraft, optional `?country=` filter |
| GET | `/api/aircraft/{icao24}` | Get one aircraft, 404 if unknown |
| GET | `/api/aircraft/{icao24}/position` | Live position from OpenSky, 404 if the aircraft is not flying |
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
./mvnw test
```

19 tests: service rules with Mockito, web layer with MockMvc, and OpenSky array mapping
without any network call. The context test needs the database container running.

## Design notes

**ICAO24 as primary key.** It is the identifier used by ADS-B and OpenSky, so the API stores
it as is, normalized to lowercase. Because `save()` on an existing key silently updates the row,
the service checks `existsById` first and rejects duplicates with a 409.

**DTOs instead of exposing entities or OpenSky data.** Input is validated before reaching the
business layer (6 hexadecimal characters for ICAO24), and neither a schema change nor an
OpenSky format change breaks the API.

**OpenSky credits are protected.** Live positions are only requested for aircraft registered
in the fleet. An unknown ICAO24 returns a 404 without any call to OpenSky.

**OAuth2 token handled by hand.** The token is cached in memory and refreshed 30 seconds before
expiry. A 401 triggers a single retry with a fresh token. Spring Security's OAuth2 client could
do this automatically, it is written by hand here to understand the flow.

**No database transaction around the HTTP call.** The fleet lookup runs in its own short
transaction, so a slow OpenSky response never holds a database connection.

**Upstream errors are explicit.** OpenSky quota exhausted returns 503, any other OpenSky failure
returns 502. Technical details go to the logs, not to the client.

## Roadmap

- [x] Layered REST API with validation and error handling
- [x] Unit and web layer tests
- [x] OpenSky client with OAuth2 client credentials
- [ ] Scheduled collection of positions and flight history
- [ ] Aggregation endpoints (flight hours, airports)
- [ ] Flyway migrations, Testcontainers, OpenAPI documentation
- [ ] Docker image and CI with GitHub Actions

## Data source

Aircraft data provided by the [OpenSky Network](https://opensky-network.org),
for non-commercial use.