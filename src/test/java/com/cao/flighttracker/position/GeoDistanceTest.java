package com.cao.flighttracker.position;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class GeoDistanceTest {

    @Test
    void samePointIsZero() {
        assertThat(GeoDistance.haversineKm(48.85, 2.35, 48.85, 2.35)).isZero();
    }

    @Test
    void oneDegreeOfLatitudeIsAbout111Km() {
        assertThat(GeoDistance.haversineKm(48.0, 2.0, 49.0, 2.0))
                .isCloseTo(111.19, within(0.1));
    }

    @Test
    void parisToLondon() {
        double distance = GeoDistance.haversineKm(48.8566, 2.3522, 51.5074, -0.1278);
        assertThat(distance).isCloseTo(343.5, within(1.0));
    }
}