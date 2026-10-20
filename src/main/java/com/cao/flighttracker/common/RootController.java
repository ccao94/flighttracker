package com.cao.flighttracker.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "FlightTracker API");
        body.put("description", "Tracks real aircraft by ICAO24 using ADS-B data from the OpenSky Network");
        body.put("source", "https://github.com/ccao94/flighttracker");
        body.put("endpoints", List.of(
                "GET /api/aircraft",
                "GET /api/aircraft/{icao24}",
                "GET /api/aircraft/{icao24}/position",
                "GET /api/aircraft/{icao24}/history?limit=50",
                "GET /api/aircraft/{icao24}/stats?days=7",
                "GET /actuator/health"));
        return body;
    }
}