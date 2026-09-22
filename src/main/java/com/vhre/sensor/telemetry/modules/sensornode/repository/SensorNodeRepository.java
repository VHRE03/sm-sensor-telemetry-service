package com.vhre.sensor.telemetry.modules.sensornode.repository;

import com.vhre.sensor.telemetry.modules.sensornode.entity.SensorNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SensorNodeRepository extends JpaRepository<SensorNode, UUID> {
}
