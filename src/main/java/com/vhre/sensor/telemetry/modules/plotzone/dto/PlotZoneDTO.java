package com.vhre.sensor.telemetry.modules.plotzone.dto;

import com.vhre.base.core.base.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Plot Zone data transfer object")
public class PlotZoneDTO extends BaseDTO {
    @Schema(description = "Hectares of the plot zone", example = "100")
    @NotNull(message = "The hectares is mandatory")
    @Min(value = 0, message = "The hectares must be at least 0")
    private BigDecimal hectares;

    @Schema(description = "Minimum moisture threshold for the plot zone", example = "15")
    @NotNull(message = "The minimum moisture threshold is mandatory")
    @Min(value = 0, message = "The minimum moisture threshold must be at least 0")
    private BigDecimal minimumMoistureThreshold;
}
