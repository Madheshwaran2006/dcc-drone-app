package com.example.dccproject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface dronerepo extends JpaRepository<DroneRecord,Long> {

    List<DroneRecord> findByDroneIdOrderByIdDesc(String droneId);
    List<DroneRecord> findByRiskScore(String riskscore);
    List<DroneRecord> findByStatus(String status);
    List<DroneRecord> findByDroneIdAndRiskScore(String droneId,String riskScore);

}
