package com.cao.flighttracker.position;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cao.flighttracker.aircraft.Aircraft;
import com.cao.flighttracker.aircraft.AircraftRepository;
import com.cao.flighttracker.opensky.StateVector;

@ExtendWith(MockitoExtension.class)
class PositionHistoryServiceTest {

    private static final Instant CONTACT = Instant.ofEpochSecond(1760000005);

    @Mock
    private PositionSnapshotRepository snapshotRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private PositionHistoryService service;

    private StateVector state(Instant lastContact) {
        return new StateVector("39de4d", "TVF64BH", "France", lastContact,
                2.354, 48.674, 998.0, false, 76.4, 198.4, 0.0);
    }

    private Aircraft aircraft() {
        return new Aircraft("39de4d", "TVF64BH", null, null, "France");
    }

    @Test
    void savesNewPositionOfTrackedAircraft() {
        when(aircraftRepository.findById("39de4d")).thenReturn(Optional.of(aircraft()));
        when(snapshotRepository.existsByAircraftIcao24AndRecordedAt("39de4d", CONTACT))
                .thenReturn(false);

        int saved = service.saveSnapshots(List.of(state(CONTACT)));

        assertThat(saved).isEqualTo(1);
        verify(snapshotRepository).save(any(PositionSnapshot.class));
    }

    @Test
    void skipsPositionAlreadyRecorded() {
        when(aircraftRepository.findById("39de4d")).thenReturn(Optional.of(aircraft()));
        when(snapshotRepository.existsByAircraftIcao24AndRecordedAt("39de4d", CONTACT))
                .thenReturn(true);

        int saved = service.saveSnapshots(List.of(state(CONTACT)));

        assertThat(saved).isZero();
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void skipsAircraftNotInFleet() {
        when(aircraftRepository.findById("39de4d")).thenReturn(Optional.empty());

        int saved = service.saveSnapshots(List.of(state(CONTACT)));

        assertThat(saved).isZero();
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void skipsStateWithoutTimestamp() {
        int saved = service.saveSnapshots(List.of(state(null)));

        assertThat(saved).isZero();
        verify(snapshotRepository, never()).save(any());
    }
}