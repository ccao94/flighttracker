package com.cao.flighttracker.position;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import com.cao.flighttracker.aircraft.Aircraft;
import com.cao.flighttracker.aircraft.AircraftService;
import com.cao.flighttracker.opensky.OpenSkyClient;
import com.cao.flighttracker.opensky.StateVector;

@ExtendWith(MockitoExtension.class)
class PositionCollectorTest {

    @Mock
    private AircraftService aircraftService;

    @Mock
    private OpenSkyClient openSkyClient;

    @Mock
    private PositionHistoryService historyService;

    @InjectMocks
    private PositionCollector collector;

    private Aircraft aircraft() {
        return new Aircraft("39de4d", "TVF64BH", null, null, "France");
    }

    @Test
    void emptyFleetDoesNotCallOpenSky() {
        when(aircraftService.findAll()).thenReturn(List.of());

        collector.collect();

        verify(openSkyClient, never()).findCurrentStates(anyList());
    }

    @Test
    void savesStatesReturnedByOpenSky() {
        StateVector state = new StateVector("39de4d", "TVF64BH", "France",
                Instant.ofEpochSecond(1760000005), 2.35, 48.67, 998.0, false, 76.4, 198.4, 0.0);
        when(aircraftService.findAll()).thenReturn(List.of(aircraft()));
        when(openSkyClient.findCurrentStates(List.of("39de4d"))).thenReturn(List.of(state));

        collector.collect();

        verify(historyService).saveSnapshots(List.of(state));
    }

    @Test
    void openSkyFailureDoesNotStopTheCollector() {
        when(aircraftService.findAll()).thenReturn(List.of(aircraft()));
        when(openSkyClient.findCurrentStates(anyList()))
                .thenThrow(new RestClientException("OpenSky down"));

        assertThatCode(() -> collector.collect()).doesNotThrowAnyException();

        verify(historyService, never()).saveSnapshots(any());
    }
}