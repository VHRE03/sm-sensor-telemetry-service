package com.vhre.sensor.telemetry.modules.sensornode.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.vhre.base.core.base.entity.BaseEntity;
import com.vhre.sensor.telemetry.modules.plotzone.entity.PlotZone;
import com.vhre.sensor.telemetry.modules.sensornode.enums.ConnectionStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensor_nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorNode extends BaseEntity {
    @Column(nullable = false)
    private Integer batteryLevel;

    @Column(name = "connection_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private PlotZone zone;
}
