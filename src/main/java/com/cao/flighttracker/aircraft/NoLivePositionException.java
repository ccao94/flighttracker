package com.cao.flighttracker.aircraft;

public class NoLivePositionException extends RuntimeException {

    public NoLivePositionException(String icao24) {
        super("No live position for aircraft: " + icao24);
    }
}