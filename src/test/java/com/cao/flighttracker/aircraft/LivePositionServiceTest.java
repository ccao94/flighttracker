package com.cao.flighttracker.aircraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cao.flighttracker.opensky.OpenSkyClient;
import com.cao.flighttracker.opensky.StateVector;

@ExtendWith(MockitoExtension.class)
class LivePositionServiceTest {

    @Mock
    private AircraftService aircraftService;

    @Mock
    private OpenSkyClient openSkyClient;

    @InjectMocks
    private LivePositionService service;

    private StateVector sampleState() {
        return new StateVector("3c6444", "DLH9LF", "Germany", Instant.ofEpochSecond(1760000005),
                2.35, 48.85, 10000.0, false, 230.5, 90.0, 0.0);
    }

    @Test
    void returnsPositionWhenAircraftIsFlying() {
        when(openSkyClient.findCurrentState("3c6444")).thenReturn(Optional.of(sampleState()));

        StateVector result = service.getLivePosition("3c6444");

        assertThat(result.latitude()).isEqualTo(48.85);
    }

    @Test
    void throwsWhenAircraftIsNotFlying() {
        when(openSkyClient.findCurrentState("3c6444")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLivePosition("3c6444"))
                .isInstanceOf(NoLivePositionException.class);
    }

    @Test
    void doesNotCallOpenSkyForUnknownAircraft() {
        when(aircraftService.findByIcao24("abcdef")).thenThrow(new AircraftNotFoundException("abcdef"));

        assertThatThrownBy(() -> service.getLivePosition("abcdef"))
                .isInstanceOf(AircraftNotFoundException.class);

        verify(openSkyClient, never()).findCurrentState(any());
    }
}