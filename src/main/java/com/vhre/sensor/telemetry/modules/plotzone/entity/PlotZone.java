package com.vhre.sensor.telemetry.modules.plotzone.entity;

import com.vhre.base.core.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity()
@Table(name = "plot_zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlotZone extends BaseEntity {
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal hectares;

    @Column(name = "minimum_moisture_threshold", nullable = false, precision = 12, scale = 4)
    private BigDecimal minimumMoistureThreshold;
}
