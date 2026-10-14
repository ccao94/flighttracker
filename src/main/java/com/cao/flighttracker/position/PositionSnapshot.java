package com.cao.flighttracker.position;

import java.time.Instant;

import com.cao.flighttracker.aircraft.Aircraft;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "position_snapshot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"icao24", "recorded_at"}))
public class PositionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "icao24", nullable = false)
    private Aircraft aircraft;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    private Double latitude;
    private Double longitude;
    private Double baroAltitude;
    private Boolean onGround;
    private Double velocity;
    private Double trueTrack;

    protected PositionSnapshot() {
    }

    public PositionSnapshot(Aircraft aircraft, Instant recordedAt, Double latitude,
                            Double longitude, Double baroAltitude, Boolean onGround,
                            Double velocity, Double trueTrack) {
        this.aircraft = aircraft;
        this.recordedAt = recordedAt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.baroAltitude = baroAltitude;
        this.onGround = onGround;
        this.velocity = velocity;
        this.trueTrack = trueTrack;
    }

    public Long getId() {
        return id;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getBaroAltitude() {
        return baroAltitude;
    }

    public Boolean getOnGround() {
        return onGround;
    }

    public Double getVelocity() {
        return velocity;
    }

    public Double getTrueTrack() {
        return trueTrack;
    }
}