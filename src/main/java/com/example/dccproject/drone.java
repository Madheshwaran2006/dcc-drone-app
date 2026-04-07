package com.example.dccproject;

import java.time.Instant;

public class drone {

    // Identity
    public String droneId;
    public String task;

    // Location
    public double lat, lng;
    public double destLat, destLng;

    // Telemetry
    public double battery;
    public double temperature;
    public double rpm;
    public double signalStrength;
    public double airspeedKmh;


    public String status;       // available | on-mission | recalled | failed
    public String riskScore;    // LOW | MEDIUM | HIGH
    public String timestamp;


    public boolean reached = false;


    public drone(String droneId, String task,
                 double lat, double lng,
                 double destLat, double destLng) {

        this.droneId       = droneId;
        this.task          = task;
        this.lat           = lat;
        this.lng           = lng;
        this.destLat       = destLat;
        this.destLng       = destLng;

        // Initial telemetry values
        this.battery       = 100.0;
        this.temperature   = 30.0;
        this.rpm           = 4500.0;
        this.signalStrength = 95.0;
        this.airspeedKmh   = 0.0;

        // Initial state
        this.status        = "on-mission";
        this.riskScore     = "LOW";
        this.timestamp     = Instant.now().toString();
    }


    // Battery % consumed per km flown (calibrated: 100% battery = ~50 km range)
    public static final double BATTERY_PER_KM = 2.0;

    // Chennai bounding box — drones stay within this area
    public static final double CHENNAI_LAT_MIN = 12.80;
    public static final double CHENNAI_LAT_MAX = 13.35;
    public static final double CHENNAI_LNG_MIN = 79.95;
    public static final double CHENNAI_LNG_MAX = 80.55;

    public void update() {

        if (status.equals("recalled") || status.equals("failed")) return;

        if (battery <= 0) {
            battery = 0;
            status  = "failed";
            rpm     = 0;
            airspeedKmh = 0;
            timestamp = Instant.now().toString();
            return;
        }

        if (reachedDestination()) {
            lat         = destLat;
            lng         = destLng;
            reached     = true;
            status      = "available";
            task        = "Idle";
            airspeedKmh = 0;
            rpm         = 0;
            timestamp   = Instant.now().toString();
            return;
        }

        // Speed 150–200 km/h, 2s tick
        double speedKmh   = 150.0 + Math.random() * 50.0;
        double stepMeters = (speedKmh / 3.6) * 2.0;

        double prevLat    = lat;
        double prevLng    = lng;
        double distToDest = haversineMeters(lat, lng, destLat, destLng);
        double fraction   = Math.min(1.0, stepMeters / distToDest);

        double newLat = lat + (destLat - lat) * fraction;
        double newLng = lng + (destLng - lng) * fraction;

        // Clamp to Chennai boundary
        lat = Math.max(CHENNAI_LAT_MIN, Math.min(CHENNAI_LAT_MAX, newLat));
        lng = Math.max(CHENNAI_LNG_MIN, Math.min(CHENNAI_LNG_MAX, newLng));

        double distMoved = haversineMeters(prevLat, prevLng, lat, lng);
        airspeedKmh = Math.round((distMoved / 2.0) * 3.6 * 10.0) / 10.0;

        // Drain: BATTERY_PER_KM per km flown (realistic, not per-tick flat)
        battery -= (distMoved / 1000.0) * BATTERY_PER_KM;
        if (battery < 0) battery = 0;

        temperature   = 30 + Math.random() * 15;
        rpm           = 4000 + Math.random() * 2000;

        double distFromOrigin = haversineMeters(13.0827, 80.2707, lat, lng);
        signalStrength = Math.max(20, 100 - (distFromOrigin / 50.0));
        signalStrength += (Math.random() * 4 - 2);

        // Risk scoring
        if (battery < 20 || temperature > 42 || signalStrength < 30) riskScore = "HIGH";
        else if (battery < 40 || temperature > 38 || signalStrength < 50) riskScore = "MEDIUM";
        else riskScore = "LOW";

        timestamp = Instant.now().toString();
    }

    /**
     * Returns battery % needed to fly from current position to destLat/destLng
     * plus a 10% safety reserve for return.
     */
    public double batteryNeededForMission(double dLat, double dLng) {
        double distMeters = haversineMeters(lat, lng, dLat, dLng);
        double distKm     = distMeters / 1000.0;
        return distKm * BATTERY_PER_KM + 10.0; // +10% safety reserve
    }

    public boolean canCompleteMission(double dLat, double dLng) {
        return battery >= batteryNeededForMission(dLat, dLng);
    }

    public boolean reachedDestination() {
        return haversineMeters(lat, lng, destLat, destLng) < 20;
    }


    public static double haversineMeters(double lat1, double lng1,
                                         double lat2, double lng2) {
        final double R = 6371000; // Earth radius in metres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}