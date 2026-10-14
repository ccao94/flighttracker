package com.cao.flighttracker.position;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.cao.flighttracker.aircraft.Aircraft;
import com.cao.flighttracker.aircraft.AircraftService;
import com.cao.flighttracker.opensky.OpenSkyClient;
import com.cao.flighttracker.opensky.StateVector;

@Component
@ConditionalOnProperty(name = "flighttracker.collector.enabled", havingValue = "true")
public class PositionCollector {

    private static final Logger log = LoggerFactory.getLogger(PositionCollector.class);

    private final AircraftService aircraftService;
    private final OpenSkyClient openSkyClient;
    private final PositionHistoryService historyService;

    public PositionCollector(AircraftService aircraftService, OpenSkyClient openSkyClient,
                             PositionHistoryService historyService) {
        this.aircraftService = aircraftService;
        this.openSkyClient = openSkyClient;
        this.historyService = historyService;
    }

    @Scheduled(fixedDelayString = "${flighttracker.collector.interval}",
            initialDelayString = "${flighttracker.collector.initial-delay}")
    public void collect() {
        List<String> fleet = aircraftService.findAll().stream()
                .map(Aircraft::getIcao24)
                .toList();
        if (fleet.isEmpty()) {
            return;
        }

        try {
            List<StateVector> states = openSkyClient.findCurrentStates(fleet);
            int saved = historyService.saveSnapshots(states);
            log.info("Collection done: {} tracked, {} reported by OpenSky, {} new positions saved",
                    fleet.size(), states.size(), saved);
        } catch (RestClientException e) {
            log.warn("Position collection failed: {}", e.getMessage());
        }
    }
}