package com.cao.flighttracker.aircraft;

public record AircraftResponse(
        String icao24,
        String callsign,
        String registration,
        String model,
        String originCountry) {

    public static AircraftResponse from(Aircraft aircraft) {
        return new AircraftResponse(
                aircraft.getIcao24(),
                aircraft.getCallsign(),
                aircraft.getRegistration(),
                aircraft.getModel(),
                aircraft.getOriginCountry());
    }
}