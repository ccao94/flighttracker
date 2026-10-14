package com.cao.flighttracker.aircraft;

import org.springframework.stereotype.Service;

import com.cao.flighttracker.opensky.OpenSkyClient;
import com.cao.flighttracker.opensky.StateVector;

@Service
public class LivePositionService {

    private final AircraftService aircraftService;
    private final OpenSkyClient openSkyClient;

    public LivePositionService(AircraftService aircraftService, OpenSkyClient openSkyClient) {
        this.aircraftService = aircraftService;
        this.openSkyClient = openSkyClient;
    }

    public StateVector getLivePosition(String icao24) {
        // Throws AircraftNotFoundException if the aircraft is not in the fleet,
        // so OpenSky credits are never spent on unknown aircraft
        aircraftService.findByIcao24(icao24);

        return openSkyClient.findCurrentState(icao24)
                .orElseThrow(() -> new NoLivePositionException(icao24));
    }
}