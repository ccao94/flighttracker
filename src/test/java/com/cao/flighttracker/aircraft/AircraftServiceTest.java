package com.cao.flighttracker.aircraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

    @Mock
    private AircraftRepository repository;

    @InjectMocks
    private AircraftService service;

    private Aircraft sampleAircraft() {
        return new Aircraft("3c6444", "DLH9LF", "D-AIBD", "A319", "Germany");
    }

    @Test
    void findByIcao24ReturnsAircraftWhenItExists() {
        Aircraft aircraft = sampleAircraft();
        when(repository.findById("3c6444")).thenReturn(Optional.of(aircraft));

        Aircraft result = service.findByIcao24("3c6444");

        assertThat(result.getCallsign()).isEqualTo("DLH9LF");
    }

    @Test
    void findByIcao24ThrowsWhenAircraftIsMissing() {
        when(repository.findById("abcdef")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByIcao24("abcdef"))
                .isInstanceOf(AircraftNotFoundException.class)
                .hasMessageContaining("abcdef");
    }

    @Test
    void findByCountryReturnsRepositoryResults() {
        when(repository.findByOriginCountry("Germany")).thenReturn(List.of(sampleAircraft()));

        List<Aircraft> result = service.findByCountry("Germany");

        assertThat(result).hasSize(1);
    }

    @Test
    void createSavesNewAircraft() {
        Aircraft aircraft = sampleAircraft();
        when(repository.existsById("3c6444")).thenReturn(false);
        when(repository.save(aircraft)).thenReturn(aircraft);

        Aircraft result = service.create(aircraft);

        assertThat(result).isSameAs(aircraft);
        verify(repository).save(aircraft);
    }

    @Test
    void createRejectsDuplicateAndDoesNotSave() {
        when(repository.existsById("3c6444")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleAircraft()))
                .isInstanceOf(AircraftAlreadyExistsException.class);

        verify(repository, never()).save(any());
    }
}