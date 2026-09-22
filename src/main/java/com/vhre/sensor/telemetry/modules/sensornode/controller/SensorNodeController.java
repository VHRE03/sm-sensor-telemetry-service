package com.vhre.sensor.telemetry.modules.sensornode.controller;

import com.vhre.base.core.base.controller.BaseController;
import com.vhre.sensor.telemetry.modules.sensornode.dto.SensorNodeDTO;
import com.vhre.sensor.telemetry.modules.sensornode.entity.SensorNode;
import com.vhre.sensor.telemetry.modules.sensornode.service.SensorNodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sensor-nodes")
@Tag(name = "Sensor Node Management", description = "Endpoints for managing sensor nodes")
public class SensorNodeController extends BaseController<SensorNode, SensorNodeDTO, UUID> {
    public SensorNodeController(SensorNodeService service) {
        super(service);
    }
}
