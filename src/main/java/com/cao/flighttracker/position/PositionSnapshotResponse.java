package com.cao.flighttracker.position;

import java.time.Instant;

public record PositionSnapshotResponse(
        Instant recordedAt,
        Double latitude,
        Double longitude,
        Double baroAltitudeMeters,
        Boolean onGround,
        Double velocityMetersPerSecond,
        Double trueTrack) {

    public static PositionSnapshotResponse from(PositionSnapshot snapshot) {
        return new PositionSnapshotResponse(
                snapshot.getRecordedAt(),
                snapshot.getLatitude(),
                snapshot.getLongitude(),
                snapshot.getBaroAltitude(),
                snapshot.getOnGround(),
                snapshot.getVelocity(),
                snapshot.getTrueTrack());
    }
}