package com.cao.flighttracker.opensky;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opensky")
public record OpenSkyProperties(
        String baseUrl,
        String tokenUrl,
        String clientId,
        String clientSecret) {
}