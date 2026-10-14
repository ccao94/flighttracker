package com.cao.flighttracker.opensky;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class OpenSkyClient {

    private final RestClient restClient;
    private final OpenSkyTokenProvider tokenProvider;

    public OpenSkyClient(OpenSkyProperties properties, OpenSkyTokenProvider tokenProvider) {
        this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
        this.tokenProvider = tokenProvider;
    }

    public Optional<StateVector> findCurrentState(String icao24) {
        return findCurrentStates(List.of(icao24)).stream().findFirst();
    }

    public List<StateVector> findCurrentStates(List<String> icao24s) {
        if (icao24s.isEmpty()) {
            return List.of();
        }
        try {
            return fetch(icao24s);
        } catch (HttpClientErrorException.Unauthorized e) {
            // Token rejected by the server: drop it and retry once with a fresh one
            tokenProvider.invalidate();
            return fetch(icao24s);
        }
    }

    private List<StateVector> fetch(List<String> icao24s) {
        OpenSkyStatesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/states/all")
                        .queryParam("icao24", icao24s)
                        .build())
                .header("Authorization", "Bearer " + tokenProvider.getToken())
                .retrieve()
                .body(OpenSkyStatesResponse.class);

        if (response == null || response.states() == null) {
            return List.of();
        }
        return response.states().stream()
                .map(StateVectorMapper::fromRow)
                .toList();
    }
}