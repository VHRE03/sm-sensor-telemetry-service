package com.vhre.sensor.telemetry.modules.sensornode.dto;

import com.vhre.base.core.base.dto.BaseDTO;
import com.vhre.sensor.telemetry.modules.sensornode.enums.ConnectionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SensorNode data transfer object")
public class SensorNodeDTO extends BaseDTO {
    @Schema(description = "Battery level", example = "50")
    @NotNull(message = "The battery level is mandatory")
    private Integer batteryLevel;

    @Schema(description = "Connection status", example = "ACTIVE")
    @NotNull(message = "The connection status is mandatory")
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus;

    @Schema(description = "Zone ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull(message = "The zone ID is mandatory")
    private UUID zoneId;
}
