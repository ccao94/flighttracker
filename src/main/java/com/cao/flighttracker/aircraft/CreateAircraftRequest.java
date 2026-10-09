package com.cao.flighttracker.aircraft;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAircraftRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9a-fA-F]{6}$", message = "must be 6 hexadecimal characters")
        String icao24,

        @Size(max = 10)
        String callsign,

        String registration,
        String model,
        String originCountry) {

    public Aircraft toEntity() {
        return new Aircraft(icao24.toLowerCase(), callsign, registration, model, originCountry);
    }
}