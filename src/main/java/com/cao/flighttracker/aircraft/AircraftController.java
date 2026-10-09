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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/aircraft")
public class AircraftController {

    private final AircraftService service;

    public AircraftController(AircraftService service) {
        this.service = service;
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

    @PostMapping
    public ResponseEntity<AircraftResponse> create(@Valid @RequestBody CreateAircraftRequest request) {
        Aircraft created = service.create(request.toEntity());
        URI location = URI.create("/api/aircraft/" + created.getIcao24());
        return ResponseEntity.created(location).body(AircraftResponse.from(created));
    }
}