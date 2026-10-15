package com.cao.flighttracker.position;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionSnapshotRepository extends JpaRepository<PositionSnapshot, Long> {

    boolean existsByAircraftIcao24AndRecordedAt(String icao24, Instant recordedAt);

    List<PositionSnapshot> findByAircraftIcao24OrderByRecordedAtDesc(String icao24, Limit limit);

    List<PositionSnapshot> findByAircraftIcao24AndRecordedAtAfterOrderByRecordedAtAsc(
            String icao24, Instant since);
}