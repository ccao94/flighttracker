package com.cao.flighttracker.opensky;

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
        try {
            return fetch(icao24);
        } catch (HttpClientErrorException.Unauthorized e) {
            // Token rejected by the server: drop it and retry once with a fresh one
            tokenProvider.invalidate();
            return fetch(icao24);
        }
    }

    private Optional<StateVector> fetch(String icao24) {
        OpenSkyStatesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/states/all")
                        .queryParam("icao24", icao24)
                        .build())
                .header("Authorization", "Bearer " + tokenProvider.getToken())
                .retrieve()
                .body(OpenSkyStatesResponse.class);

        if (response == null || response.states() == null || response.states().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(StateVectorMapper.fromRow(response.states().get(0)));
    }
}