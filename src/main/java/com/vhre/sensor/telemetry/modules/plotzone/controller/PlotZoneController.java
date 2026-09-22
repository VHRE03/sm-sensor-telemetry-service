package com.vhre.sensor.telemetry.modules.plotzone.controller;

import com.vhre.base.core.base.controller.BaseController;
import com.vhre.sensor.telemetry.modules.plotzone.dto.PlotZoneDTO;
import com.vhre.sensor.telemetry.modules.plotzone.entity.PlotZone;
import com.vhre.sensor.telemetry.modules.plotzone.service.PlotZoneService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plot-zones")
@Tag(name = "Plot Zone Management", description = "Endpoints for managing plot zones")
public class PlotZoneController extends BaseController<PlotZone, PlotZoneDTO, UUID> {
    public PlotZoneController(PlotZoneService service) {
        super(service);
    }
}
