package com.example.dccproject;

import jakarta.persistence.*;

@Entity
@Table(name = "telemetry")
public class DroneRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← auto-increment id
    private Long id;

    // Every field below → becomes a column automatically
    private String droneId;
    private String task;

    private double lat;
    private double lng;
    private double destLat;
    private double destLng;

    private double battery;
    private double temperature;
    private double rpm;
    private double signalStrength;
    private double airspeedKmh;

    private String status;
    private String riskScore;
    private String timestamp;


    public DroneRecord() {
    }


    public DroneRecord(drone d) {
        this.droneId = d.droneId;
        this.task = d.task;
        this.lat = d.lat;
        this.lng = d.lng;
        this.destLat = d.destLat;
        this.destLng = d.destLng;
        this.battery = d.battery;
        this.temperature = d.temperature;
        this.rpm = d.rpm;
        this.signalStrength = d.signalStrength;
        this.airspeedKmh = d.airspeedKmh;
        this.status = d.status;
        this.riskScore = d.riskScore;
        this.timestamp = d.timestamp;
    }


    public Long getId() {
        return id;
    }

    public String getDroneId() {
        return droneId;
    }

    public String getTask() {
        return task;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getDestLat() {
        return destLat;
    }

    public double getDestLng() {
        return destLng;
    }

    public double getBattery() {
        return battery;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getRpm() {
        return rpm;
    }

    public double getSignalStrength() {
        return signalStrength;
    }

    public double getAirspeedKmh() {
        return airspeedKmh;
    }

    public String getStatus() {
        return status;
    }

    public String getRiskScore() {
        return riskScore;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

