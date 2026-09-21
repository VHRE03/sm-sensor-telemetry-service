package com.vhre.sensor.telemetry.modules.plotzone.repository;

import com.vhre.sensor.telemetry.modules.plotzone.entity.PlotZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlotZoneRepository extends JpaRepository<PlotZone, UUID> {
}
