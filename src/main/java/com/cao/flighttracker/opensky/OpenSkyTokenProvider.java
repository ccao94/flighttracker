package com.cao.flighttracker.opensky;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class OpenSkyTokenProvider {

    // Refresh a bit before the real expiry so a request never leaves with a dying token
    private static final long REFRESH_MARGIN_SECONDS = 30;

    private final OpenSkyProperties properties;
    private final RestClient restClient = RestClient.create();

    private String token;
    private Instant expiresAt;

    public OpenSkyTokenProvider(OpenSkyProperties properties) {
        this.properties = properties;
    }

    public synchronized String getToken() {
        if (token != null && Instant.now().isBefore(expiresAt)) {
            return token;
        }
        return refresh();
    }

    public synchronized void invalidate() {
        token = null;
    }

    private String refresh() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        TokenResponse response = restClient.post()
                .uri(properties.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        token = response.accessToken();
        expiresAt = Instant.now().plusSeconds(response.expiresIn() - REFRESH_MARGIN_SECONDS);
        return token;
    }

    record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn) {
    }
}