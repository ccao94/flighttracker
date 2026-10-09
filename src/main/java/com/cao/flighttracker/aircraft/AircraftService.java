package com.cao.flighttracker.aircraft;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AircraftService {

    private final AircraftRepository repository;

    public AircraftService(AircraftRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Aircraft> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Aircraft findByIcao24(String icao24) {
        return repository.findById(icao24)
                .orElseThrow(() -> new AircraftNotFoundException(icao24));
    }

    @Transactional(readOnly = true)
    public List<Aircraft> findByCountry(String originCountry) {
        return repository.findByOriginCountry(originCountry);
    }

    @Transactional
    public Aircraft create(Aircraft aircraft) {
        if (repository.existsById(aircraft.getIcao24())) {
            throw new AircraftAlreadyExistsException(aircraft.getIcao24());
        }
        return repository.save(aircraft);
    }
}