package com.cao.flighttracker.position;

import java.time.Instant;

public record FlightStats(
        String icao24,
        int positionCount,
        Instant firstSeen,
        Instant lastSeen,
        double distanceKm,
        long airborneMinutes,
        Double maxAltitudeMeters,
        Double maxVelocityMetersPerSecond) {
}