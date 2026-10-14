package com.cao.flighttracker.position;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cao.flighttracker.aircraft.Aircraft;
import com.cao.flighttracker.aircraft.AircraftNotFoundException;
import com.cao.flighttracker.aircraft.AircraftRepository;
import com.cao.flighttracker.opensky.StateVector;

@Service
public class PositionHistoryService {

    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 500;

    private final PositionSnapshotRepository snapshotRepository;
    private final AircraftRepository aircraftRepository;

    public PositionHistoryService(PositionSnapshotRepository snapshotRepository,
                                  AircraftRepository aircraftRepository) {
        this.snapshotRepository = snapshotRepository;
        this.aircraftRepository = aircraftRepository;
    }

    @Transactional
    public int saveSnapshots(List<StateVector> states) {
        int saved = 0;
        for (StateVector state : states) {
            if (state.lastContact() == null) {
                continue;
            }

            Optional<Aircraft> aircraft = aircraftRepository.findById(state.icao24());
            if (aircraft.isEmpty()) {
                continue;
            }

            boolean alreadyRecorded = snapshotRepository
                    .existsByAircraftIcao24AndRecordedAt(state.icao24(), state.lastContact());
            if (alreadyRecorded) {
                continue;
            }

            snapshotRepository.save(new PositionSnapshot(
                    aircraft.get(),
                    state.lastContact(),
                    state.latitude(),
                    state.longitude(),
                    state.baroAltitude(),
                    state.onGround(),
                    state.velocity(),
                    state.trueTrack()));
            saved++;
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<PositionSnapshot> getHistory(String icao24, int limit) {
        if (!aircraftRepository.existsById(icao24)) {
            throw new AircraftNotFoundException(icao24);
        }
        int boundedLimit = Math.min(Math.max(limit, MIN_LIMIT), MAX_LIMIT);
        return snapshotRepository.findByAircraftIcao24OrderByRecordedAtDesc(
                icao24, Limit.of(boundedLimit));
    }
}