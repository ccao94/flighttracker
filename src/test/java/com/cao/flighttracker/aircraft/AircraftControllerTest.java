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

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cao.flighttracker.opensky.StateVector;
import com.cao.flighttracker.position.PositionHistoryService;
import com.cao.flighttracker.position.PositionSnapshot;

@WebMvcTest(AircraftController.class)
class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AircraftService service;

    @MockitoBean
    private LivePositionService livePositionService;

    @MockitoBean
    private PositionHistoryService positionHistoryService;

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

    @Test
    void positionReturnsLiveState() throws Exception {
        StateVector state = new StateVector("3c6444", "DLH9LF", "Germany",
                Instant.ofEpochSecond(1760000005), 2.35, 48.85, 10000.0, false, 230.5, 90.0, 0.0);
        when(livePositionService.getLivePosition("3c6444")).thenReturn(state);

        mockMvc.perform(get("/api/aircraft/3c6444/position"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(48.85))
                .andExpect(jsonPath("$.callsign").value("DLH9LF"));
    }

    @Test
    void positionReturns404WhenNotFlying() throws Exception {
        when(livePositionService.getLivePosition("3c6444"))
                .thenThrow(new NoLivePositionException("3c6444"));

        mockMvc.perform(get("/api/aircraft/3c6444/position"))
                .andExpect(status().isNotFound());
    }

    @Test
    void historyReturnsSnapshots() throws Exception {
        PositionSnapshot snapshot = new PositionSnapshot(sampleAircraft(),
                Instant.ofEpochSecond(1760000005), 48.67, 2.35, 998.0, false, 76.4, 198.4);
        when(positionHistoryService.getHistory("3c6444", 50)).thenReturn(List.of(snapshot));

        mockMvc.perform(get("/api/aircraft/3c6444/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].latitude").value(48.67))
                .andExpect(jsonPath("$.length()").value(1));
    }
}