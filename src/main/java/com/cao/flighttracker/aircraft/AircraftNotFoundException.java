package com.cao.flighttracker.aircraft;

public class AircraftNotFoundException extends RuntimeException {

    public AircraftNotFoundException(String icao24) {
        super("Aircraft not found: " + icao24);
    }
}