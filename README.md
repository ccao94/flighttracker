# FlightTracker

![CI](https://github.com/ccao94/flighttracker/actions/workflows/ci.yml/badge.svg)

**Live API:** https://flighttracker-ppkc.onrender.com
(free hosting, the first request after inactivity can take 2 to 3 minutes)

REST API that tracks real aircraft by their ICAO24 transponder address, using live ADS-B data
from the [OpenSky Network](https://opensky-network.org). A scheduled task builds a position
history for the fleet, and flight statistics are computed from it.

Personal project started in September 2026 to learn Spring, following my internship at
Thales AVS (Training & Simulation).

## Stack

Java 17, Spring Boot 4.1, Spring Data JPA, PostgreSQL, Flyway, RestClient with OAuth2,
JUnit 5, Mockito, Testcontainers, Docker, GitHub Actions. Deployed on Render, database on Neon.

## Run locally

Prerequisite: Docker.

```bash
docker compose up --build
```

The API starts on `http://localhost:8080`. For development, run only the database with
`docker compose up -d db` and start the app with `./mvnw spring-boot:run` (Java 17+).

Secrets come from environment variables, never from the repository:

| Variable | Purpose |
|---|---|
| `OPENSKY_CLIENT_ID`, `OPENSKY_CLIENT_SECRET` | OpenSky API client, for live positions and collection |
| `FLIGHTTRACKER_ADMIN_KEY` | Required in the `X-Admin-Key` header to register aircraft (empty locally) |
| `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD` | Database connection |

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/aircraft` | Fleet, optional `?country=` filter |
| GET | `/api/aircraft/{icao24}/position` | Live position from OpenSky |
| GET | `/api/aircraft/{icao24}/history` | Collected positions, `?limit=` 1 to 500 |
| GET | `/api/aircraft/{icao24}/stats` | Flight statistics, `?days=` 1 to 30 |
| POST | `/api/aircraft` | Register an aircraft, requires `X-Admin-Key` |

Statistics collected for a real Air India long-haul flight over 35 minutes:

```json
{
  "icao24": "801659",
  "positionCount": 8,
  "distanceKm": 509.3,
  "airborneMinutes": 35,
  "maxAltitudeMeters": 11582.4,
  "maxVelocityMetersPerSecond": 244.0
}
```

## Tests

`./mvnw verify` runs 50 tests on every push: unit tests with Mockito, web layer tests with
MockMvc, and integration tests against a disposable PostgreSQL started by Testcontainers,
with the real Flyway migrations. Only Docker is required.

## Design notes

**ICAO24 as primary key.** It is the identifier used by ADS-B, so the API keeps it. Since
`save()` on an existing key silently overwrites the row, duplicates are checked first and
rejected with a 409.

**Schema owned by Flyway.** Tables and named constraints live in versioned SQL migrations.
Hibernate only validates, so an entity that drifts from the schema stops the app at startup.

**OpenSky credits are protected.** Only aircraft in the fleet are queried, and the whole fleet
is fetched in one call per cycle: 1 credit every 5 minutes, whatever the fleet size.

**Distances from real positions only.** Haversine distance between consecutive positions,
counted only when both are airborne and less than 15 minutes apart, so a coverage gap never
becomes an invented straight line. Checked against the flight above: 242 m/s on average for
a measured top speed of 244 m/s.

**Short transactions.** No database transaction wraps an OpenSky call, so a slow response
never holds a connection. Writes live in a separate service because `@Transactional` is
skipped when a method calls another method of the same class.

**Public reads, protected writes.** An admin key, compared in constant time, guards the only
write endpoint. Spring Security would be oversized here.

## Limits and next steps

- The free plan sleeps after 15 minutes without traffic, and collection stops with it.
  Next step: move collection to a scheduled GitHub Actions job.
- Check the admin key before request validation.
- OpenAPI documentation, once springdoc supports Spring Boot 4.1.

Aircraft data provided by the [OpenSky Network](https://opensky-network.org), for non-commercial use.