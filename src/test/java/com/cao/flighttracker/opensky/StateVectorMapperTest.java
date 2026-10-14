package com.cao.flighttracker.opensky;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class StateVectorMapperTest {

    @Test
    void mapsAFullRow() {
        List<Object> row = Arrays.asList(
                "3c6444", "DLH9LF  ", "Germany", 1760000000, 1760000005,
                2.35, 48.85, 10000.0, false, 230.5, 90.0, -1.2,
                null, 10100.0, "1000", false, 0);

        StateVector state = StateVectorMapper.fromRow(row);

        assertThat(state.icao24()).isEqualTo("3c6444");
        assertThat(state.callsign()).isEqualTo("DLH9LF");
        assertThat(state.lastContact()).isEqualTo(Instant.ofEpochSecond(1760000005));
        assertThat(state.latitude()).isEqualTo(48.85);
        assertThat(state.onGround()).isFalse();
    }

    @Test
    void acceptsIntegersWhereDoublesAreExpected() {
        List<Object> row = Arrays.asList(
                "3c6444", "DLH9LF", "Germany", 1760000000, 1760000005,
                2, 48, 10000, false, 230, 90, 0,
                null, 10100, null, false, 0);

        StateVector state = StateVectorMapper.fromRow(row);

        assertThat(state.velocity()).isEqualTo(230.0);
    }

    @Test
    void keepsNullsForMissingPosition() {
        List<Object> row = Arrays.asList(
                "3c6444", null, "Germany", null, 1760000005,
                null, null, null, true, null, null, null,
                null, null, null, false, 0);

        StateVector state = StateVectorMapper.fromRow(row);

        assertThat(state.callsign()).isNull();
        assertThat(state.latitude()).isNull();
        assertThat(state.onGround()).isTrue();
    }
}