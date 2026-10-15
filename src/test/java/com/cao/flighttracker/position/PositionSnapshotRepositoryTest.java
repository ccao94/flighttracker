package com.cao.flighttracker.position;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;

import com.cao.flighttracker.TestcontainersConfiguration;
import com.cao.flighttracker.aircraft.Aircraft;
import com.cao.flighttracker.aircraft.AircraftRepository;

@SpringBootTest(properties = "flighttracker.collector.enabled=false")
@Import(TestcontainersConfiguration.class)
@Transactional
class PositionSnapshotRepositoryTest {

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private PositionSnapshotRepository snapshotRepository;

    private Aircraft aircraft;

    @BeforeEach
    void setUp() {
        aircraft = aircraftRepository.save(
                new Aircraft("39de4d", "TVF64BH", null, null, "France"));
    }

    private PositionSnapshot snapshotAt(long epochSecond) {
        return new PositionSnapshot(aircraft, Instant.ofEpochSecond(epochSecond),
                48.6, 2.3, 1000.0, false, 240.0, 198.0);
    }

    @Test
    void historyIsOrderedFromMostRecentAndLimited() {
        snapshotRepository.save(snapshotAt(1000));
        snapshotRepository.save(snapshotAt(3000));
        snapshotRepository.save(snapshotAt(2000));

        List<PositionSnapshot> history = snapshotRepository
                .findByAircraftIcao24OrderByRecordedAtDesc("39de4d", Limit.of(2));

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getRecordedAt()).isEqualTo(Instant.ofEpochSecond(3000));
        assertThat(history.get(1).getRecordedAt()).isEqualTo(Instant.ofEpochSecond(2000));
    }

    @Test
    void existsQueryFindsRecordedPosition() {
        snapshotRepository.save(snapshotAt(1000));

        assertThat(snapshotRepository.existsByAircraftIcao24AndRecordedAt(
                "39de4d", Instant.ofEpochSecond(1000))).isTrue();
        assertThat(snapshotRepository.existsByAircraftIcao24AndRecordedAt(
                "39de4d", Instant.ofEpochSecond(9999))).isFalse();
    }

    @Test
    void databaseRejectsDuplicatePosition() {
        snapshotRepository.saveAndFlush(snapshotAt(1000));

        assertThatThrownBy(() -> snapshotRepository.saveAndFlush(snapshotAt(1000)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aircraftCanBeFoundByCountry() {
        aircraftRepository.save(new Aircraft("3c6444", "DLH9LF", null, null, "Germany"));

        assertThat(aircraftRepository.findByOriginCountry("France"))
                .extracting(Aircraft::getIcao24)
                .containsExactly("39de4d");
    }

    @Test
    void statsQueryReturnsPeriodInChronologicalOrder() {
        snapshotRepository.save(snapshotAt(3000));
        snapshotRepository.save(snapshotAt(1000));
        snapshotRepository.save(snapshotAt(2000));

        List<PositionSnapshot> result = snapshotRepository
                .findByAircraftIcao24AndRecordedAtAfterOrderByRecordedAtAsc(
                        "39de4d", Instant.ofEpochSecond(1500));

        assertThat(result)
                .extracting(PositionSnapshot::getRecordedAt)
                .containsExactly(Instant.ofEpochSecond(2000), Instant.ofEpochSecond(3000));
    }
}