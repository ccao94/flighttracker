package com.cao.flighttracker.opensky;

import java.time.Instant;

public record StateVector(
        String icao24,
        String callsign,
        String originCountry,
        Instant lastContact,
        Double longitude,
        Double latitude,
        Double baroAltitude,
        Boolean onGround,
        Double velocity,
        Double trueTrack,
        Double verticalRate) {
}