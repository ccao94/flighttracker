package com.cao.flighttracker.aircraft;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cao.flighttracker.position.PositionHistoryService;
import com.cao.flighttracker.position.PositionSnapshotResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/aircraft")
public class AircraftController {

    private final AircraftService service;
    private final LivePositionService livePositionService;
    private final PositionHistoryService positionHistoryService;

    public AircraftController(AircraftService service, LivePositionService livePositionService,
                              PositionHistoryService positionHistoryService) {
        this.service = service;
        this.livePositionService = livePositionService;
        this.positionHistoryService = positionHistoryService;
    }

    @GetMapping
    public List<AircraftResponse> list(@RequestParam(required = false) String country) {
        List<Aircraft> aircraft = (country == null)
                ? service.findAll()
                : service.findByCountry(country);
        return aircraft.stream().map(AircraftResponse::from).toList();
    }

    @GetMapping("/{icao24}")
    public AircraftResponse get(@PathVariable String icao24) {
        return AircraftResponse.from(service.findByIcao24(icao24.toLowerCase()));
    }

    @GetMapping("/{icao24}/position")
    public LivePositionResponse position(@PathVariable String icao24) {
        return LivePositionResponse.from(livePositionService.getLivePosition(icao24.toLowerCase()));
    }

    @GetMapping("/{icao24}/history")
    public List<PositionSnapshotResponse> history(@PathVariable String icao24,
                                                  @RequestParam(defaultValue = "50") int limit) {
        return positionHistoryService.getHistory(icao24.toLowerCase(), limit).stream()
                .map(PositionSnapshotResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<AircraftResponse> create(@Valid @RequestBody CreateAircraftRequest request) {
        Aircraft created = service.create(request.toEntity());
        URI location = URI.create("/api/aircraft/" + created.getIcao24());
        return ResponseEntity.created(location).body(AircraftResponse.from(created));
    }
}