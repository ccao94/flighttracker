package com.cao.flighttracker.aircraft;

public class AircraftAlreadyExistsException extends RuntimeException {

    public AircraftAlreadyExistsException(String icao24) {
        super("Aircraft already exists: " + icao24);
    }
}