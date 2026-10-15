package com.cao.flighttracker.position;

import java.time.Duration;
import java.util.List;

public final class FlightStatsCalculator {

    // Beyond this gap, the aircraft probably left coverage or landed:
    // a straight line between the two points would invent a distance
    static final Duration MAX_GAP = Duration.ofMinutes(15);

    private FlightStatsCalculator() {
    }

    /**
     * Snapshots must be sorted from oldest to most recent.
     */
    public static FlightStats compute(String icao24, List<PositionSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return new FlightStats(icao24, 0, null, null, 0.0, 0, null, null);
        }

        double distanceKm = 0;
        long airborneSeconds = 0;
        Double maxAltitude = null;
        Double maxVelocity = null;

        for (int i = 0; i < snapshots.size(); i++) {
            PositionSnapshot current = snapshots.get(i);
            maxAltitude = max(maxAltitude, current.getBaroAltitude());
            maxVelocity = max(maxVelocity, current.getVelocity());

            if (i == 0) {
                continue;
            }

            PositionSnapshot previous = snapshots.get(i - 1);
            if (isFlightSegment(previous, current)) {
                distanceKm += GeoDistance.haversineKm(
                        previous.getLatitude(), previous.getLongitude(),
                        current.getLatitude(), current.getLongitude());
                airborneSeconds += Duration.between(
                        previous.getRecordedAt(), current.getRecordedAt()).getSeconds();
            }
        }

        return new FlightStats(
                icao24,
                snapshots.size(),
                snapshots.get(0).getRecordedAt(),
                snapshots.get(snapshots.size() - 1).getRecordedAt(),
                Math.round(distanceKm * 10) / 10.0,
                airborneSeconds / 60,
                maxAltitude,
                maxVelocity);
    }

    private static boolean isFlightSegment(PositionSnapshot a, PositionSnapshot b) {
        boolean bothAirborne = Boolean.FALSE.equals(a.getOnGround())
                && Boolean.FALSE.equals(b.getOnGround());
        boolean bothPositioned = a.getLatitude() != null && a.getLongitude() != null
                && b.getLatitude() != null && b.getLongitude() != null;
        boolean closeInTime = Duration.between(a.getRecordedAt(), b.getRecordedAt())
                .compareTo(MAX_GAP) <= 0;
        return bothAirborne && bothPositioned && closeInTime;
    }

    private static Double max(Double current, Double candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null) {
            return candidate;
        }
        return Math.max(current, candidate);
    }
}