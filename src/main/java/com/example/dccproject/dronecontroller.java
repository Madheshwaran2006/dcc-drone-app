package com.example.dccproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/drones")
public class dronecontroller {
    @Autowired
    private droneservice ds;


    public dronecontroller( droneservice ds) {
        this.ds = ds;
        ;
    }


    @GetMapping("/history/{id}")
    public List<DroneRecord> getHistory(@PathVariable String id) {
        return ds.getHistroryByDroneId(id);
    }


    @GetMapping("/all")
    public List<DroneRecord> getAllRecords() {
        return ds.getallrecords();
    }


    @GetMapping("/alerts")
    public List<DroneRecord> getAlerts() {
        return ds.getAllHighriskrecords();
    }


    @GetMapping("/recalled")
    public List<DroneRecord> getRecalled() {
        return ds.getAllRecalledRecords();
    }


    @GetMapping("/alerts/{id}")
    public List<DroneRecord> getAlertsByDrone(@PathVariable String id) {
        return ds.getHighRiskByDroneId(id);
    }
}
