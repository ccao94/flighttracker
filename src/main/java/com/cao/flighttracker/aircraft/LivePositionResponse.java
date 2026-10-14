package com.cao.flighttracker.aircraft;

import java.time.Instant;

import com.cao.flighttracker.opensky.StateVector;

public record LivePositionResponse(
        String icao24,
        String callsign,
        Instant lastContact,
        Double latitude,
        Double longitude,
        Double baroAltitudeMeters,
        Boolean onGround,
        Double velocityMetersPerSecond,
        Double trueTrack) {

    public static LivePositionResponse from(StateVector state) {
        return new LivePositionResponse(
                state.icao24(),
                state.callsign(),
                state.lastContact(),
                state.latitude(),
                state.longitude(),
                state.baroAltitude(),
                state.onGround(),
                state.velocity(),
                state.trueTrack());
    }
}