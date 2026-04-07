package com.example.dccproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/drones")
public class simulator {

    @Autowired
    private droneservice droneService;

    // Base location — Chennai
    private static final double BASE_LAT = 13.0827;
    private static final double BASE_LNG = 80.2707;

    List<drone> drones = new ArrayList<>();

    public simulator() {
        // 5 drones parked at different Chennai landmarks
        double[][] bases = {
            {13.0827, 80.2707},  // Chennai Central
            {13.0524, 80.2502},  // Adyar
            {13.1143, 80.2329},  // Ambattur
            {13.0067, 80.2206},  // Tambaram
            {13.1389, 80.2985}   // Perambur
        };
        for (int i = 0; i < 5; i++) {
            drone d = new drone("DRONE-00" + (i + 1), "Idle",
                    bases[i][0], bases[i][1], bases[i][0], bases[i][1]);
            d.status      = "available";
            d.airspeedKmh = 0;
            d.rpm         = 0;
            drones.add(d);
        }
    }

    @Scheduled(fixedRate = 2000)
    public void simulationTick() {
        for (drone d : drones) {
            if (d.status.equals("available")) continue;
            d.update();
            droneService.savetele(d);
            System.out.printf("[%s] battery=%.1f%% status=%s%n", d.droneId, d.battery, d.status);
        }
    }

    @GetMapping
    public List<drone> getDrones() { return drones; }

    @GetMapping("/{id}")
    public drone getDroneById(@PathVariable String id) {
        return drones.stream().filter(d -> d.droneId.equals(id)).findFirst().orElse(null);
    }

    @GetMapping("/available")
    public List<drone> getAvailableDrones() {
        List<drone> available = new ArrayList<>();
        for (drone d : drones) {
            if (d.status.equals("available") && d.battery > 20) available.add(d);
        }
        available.sort((a, b) -> Double.compare(b.battery, a.battery));
        return available;
    }

    @PostMapping("/assign-mission")
    public ResponseEntity<?> assignMission(@RequestBody MissionRequest req) {

        // 1. Validate destination is within Chennai bounds
        if (req.destLat < drone.CHENNAI_LAT_MIN || req.destLat > drone.CHENNAI_LAT_MAX ||
            req.destLng < drone.CHENNAI_LNG_MIN || req.destLng > drone.CHENNAI_LNG_MAX) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Destination is outside Chennai operational boundary"));
        }

        // 2. Among available drones, find ones that can feasibly complete the mission
        List<drone> feasible = drones.stream()
                .filter(d -> d.status.equals("available"))
                .filter(d -> d.canCompleteMission(req.destLat, req.destLng))
                .sorted(Comparator.comparingDouble(
                    // prefer closest drone to destination (less travel = less battery waste)
                    d -> drone.haversineMeters(d.lat, d.lng, req.destLat, req.destLng)))
                .toList();

        if (feasible.isEmpty()) {
            // Tell the user why — find best available and show its battery vs needed
            drone best = drones.stream()
                    .filter(d -> d.status.equals("available"))
                    .max(Comparator.comparingDouble(d -> d.battery))
                    .orElse(null);
            if (best == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No available drones"));
            }
            double needed = best.batteryNeededForMission(req.destLat, req.destLng);
            return ResponseEntity.badRequest().body(Map.of(
                "error", String.format(
                    "No drone can complete this mission. Best drone %s has %.1f%% battery but needs %.1f%%",
                    best.droneId, best.battery, needed)));
        }

        // 3. Assign to the closest feasible drone
        drone best = feasible.get(0);
        best.task    = req.task;
        best.destLat = req.destLat;
        best.destLng = req.destLng;
        best.reached = false;
        best.status  = "on-mission";
        return ResponseEntity.ok(Map.of(
            "drone",    best,
            "message",  best.droneId + " assigned — battery: " + String.format("%.1f", best.battery)
                        + "%, needed: " + String.format("%.1f", best.batteryNeededForMission(req.destLat, req.destLng)) + "%"
        ));
    }
}