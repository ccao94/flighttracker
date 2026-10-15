package com.cao.flighttracker.position;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class FlightStatsCalculatorTest {

    private PositionSnapshot snapshot(long epochSecond, Double latitude, Boolean onGround,
                                      Double altitude) {
        return new PositionSnapshot(null, Instant.ofEpochSecond(epochSecond),
                latitude, 2.0, altitude, onGround, 230.0, 0.0);
    }

    @Test
    void emptyHistoryGivesEmptyStats() {
        FlightStats stats = FlightStatsCalculator.compute("39de4d", List.of());

        assertThat(stats.positionCount()).isZero();
        assertThat(stats.firstSeen()).isNull();
        assertThat(stats.distanceKm()).isZero();
    }

    @Test
    void sumsDistanceAndTimeBetweenConsecutivePositions() {
        // Heading north at 0.1 degree of latitude every 5 minutes
        List<PositionSnapshot> snapshots = List.of(
                snapshot(0, 48.0, false, 10000.0),
                snapshot(300, 48.1, false, 10000.0),
                snapshot(600, 48.2, false, 10000.0));

        FlightStats stats = FlightStatsCalculator.compute("39de4d", snapshots);

        assertThat(stats.positionCount()).isEqualTo(3);
        assertThat(stats.distanceKm()).isCloseTo(22.2, within(0.1));
        assertThat(stats.airborneMinutes()).isEqualTo(10);
        assertThat(stats.firstSeen()).isEqualTo(Instant.ofEpochSecond(0));
        assertThat(stats.lastSeen()).isEqualTo(Instant.ofEpochSecond(600));
    }

    @Test
    void ignoresGapsLongerThanFifteenMinutes() {
        List<PositionSnapshot> snapshots = List.of(
                snapshot(0, 48.0, false, 10000.0),
                snapshot(3600, 50.0, false, 10000.0));

        FlightStats stats = FlightStatsCalculator.compute("39de4d", snapshots);

        assertThat(stats.distanceKm()).isZero();
        assertThat(stats.airborneMinutes()).isZero();
    }

    @Test
    void ignoresSegmentsOnTheGround() {
        List<PositionSnapshot> snapshots = List.of(
                snapshot(0, 48.0, true, 0.0),
                snapshot(300, 48.1, false, 500.0));

        FlightStats stats = FlightStatsCalculator.compute("39de4d", snapshots);

        assertThat(stats.distanceKm()).isZero();
    }

    @Test
    void maxAltitudeIgnoresMissingValues() {
        List<PositionSnapshot> snapshots = List.of(
                snapshot(0, 48.0, false, 1000.0),
                snapshot(300, 48.1, false, null),
                snapshot(600, 48.2, false, 11000.0));

        FlightStats stats = FlightStatsCalculator.compute("39de4d", snapshots);

        assertThat(stats.maxAltitudeMeters()).isEqualTo(11000.0);
    }
}