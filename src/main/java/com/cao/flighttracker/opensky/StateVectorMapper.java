package com.cao.flighttracker.opensky;

import java.time.Instant;
import java.util.List;

public final class StateVectorMapper {

    // Positions documented at https://openskynetwork.github.io/opensky-api/rest.html
    private static final int ICAO24 = 0;
    private static final int CALLSIGN = 1;
    private static final int ORIGIN_COUNTRY = 2;
    private static final int LAST_CONTACT = 4;
    private static final int LONGITUDE = 5;
    private static final int LATITUDE = 6;
    private static final int BARO_ALTITUDE = 7;
    private static final int ON_GROUND = 8;
    private static final int VELOCITY = 9;
    private static final int TRUE_TRACK = 10;
    private static final int VERTICAL_RATE = 11;

    private StateVectorMapper() {
    }

    public static StateVector fromRow(List<Object> row) {
        return new StateVector(
                (String) row.get(ICAO24),
                trimToNull((String) row.get(CALLSIGN)),
                (String) row.get(ORIGIN_COUNTRY),
                toInstant(row.get(LAST_CONTACT)),
                toDouble(row.get(LONGITUDE)),
                toDouble(row.get(LATITUDE)),
                toDouble(row.get(BARO_ALTITUDE)),
                (Boolean) row.get(ON_GROUND),
                toDouble(row.get(VELOCITY)),
                toDouble(row.get(TRUE_TRACK)),
                toDouble(row.get(VERTICAL_RATE)));
    }

    private static Double toDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    private static Instant toInstant(Object value) {
        return value == null ? null : Instant.ofEpochSecond(((Number) value).longValue());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}