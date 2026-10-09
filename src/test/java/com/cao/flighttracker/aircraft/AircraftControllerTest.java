package com.cao.flighttracker.aircraft;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AircraftController.class)
class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AircraftService service;

    private Aircraft sampleAircraft() {
        return new Aircraft("3c6444", "DLH9LF", "D-AIBD", "A319", "Germany");
    }

    @Test
    void getReturnsAircraft() throws Exception {
        when(service.findByIcao24("3c6444")).thenReturn(sampleAircraft());

        mockMvc.perform(get("/api/aircraft/3c6444"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callsign").value("DLH9LF"));
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        when(service.findByIcao24("abcdef")).thenThrow(new AircraftNotFoundException("abcdef"));

        mockMvc.perform(get("/api/aircraft/abcdef"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any(Aircraft.class))).thenReturn(sampleAircraft());

        String body = """
                {"icao24": "3C6444", "callsign": "DLH9LF", "originCountry": "Germany"}
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/aircraft/3c6444"));
    }

    @Test
    void createRejectsInvalidIcao24() throws Exception {
        String body = """
                {"icao24": "not-hex"}
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.icao24").exists());

        verify(service, never()).create(any());
    }

    @Test
    void createReturns409OnDuplicate() throws Exception {
        when(service.create(any(Aircraft.class)))
                .thenThrow(new AircraftAlreadyExistsException("3c6444"));

        String body = """
                {"icao24": "3c6444"}
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}