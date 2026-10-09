package com.cao.flighttracker.aircraft;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aircraft")
public class Aircraft {

    @Id
    @Column(nullable = false, unique = true, length = 6)
    private String icao24;

    @Column(length = 10)
    private String callsign;

    private String registration;

    private String model;

    @Column(name = "origin_country")
    private String originCountry;

    // JPA requires a no-argument constructor
    protected Aircraft() {
    }

    public Aircraft(String icao24, String callsign, String registration,
                    String model, String originCountry) {
        this.icao24 = icao24;
        this.callsign = callsign;
        this.registration = registration;
        this.model = model;
        this.originCountry = originCountry;
    }

    public String getIcao24() {
        return icao24;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }
}