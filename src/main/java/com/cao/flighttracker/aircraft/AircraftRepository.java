package com.cao.flighttracker.aircraft;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AircraftRepository extends JpaRepository<Aircraft, String> {

    List<Aircraft> findByOriginCountry(String originCountry);
}