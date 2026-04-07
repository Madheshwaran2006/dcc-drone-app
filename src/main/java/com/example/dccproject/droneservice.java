package com.example.dccproject;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class droneservice {

    @Autowired
    private dronerepo dr;

    public droneservice(dronerepo dr){
        this.dr = dr;
    }

    public void savetele(drone d)
    {
        DroneRecord record = new DroneRecord(d);
        dr.save(record);
    }
    public List<DroneRecord> getHistroryByDroneId(String droneId)
    {
        return dr.findByDroneIdOrderByIdDesc(droneId);
    }

    public List<DroneRecord> getAllHighriskrecords()
    {
        return dr.findByRiskScore("HIGH");
    }

    public List<DroneRecord> getAllRecalledRecords()
    {
        return dr.findByStatus("recalled");
    }

    public List<DroneRecord> getallrecords()
    {
        return dr.findAll();
    }

    public List<DroneRecord> getHighRiskByDroneId(String droneId) {
        return dr.findByDroneIdAndRiskScore(droneId, "HIGH");
    }
}
