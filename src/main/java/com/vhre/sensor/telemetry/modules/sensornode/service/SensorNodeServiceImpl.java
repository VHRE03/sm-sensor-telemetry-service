package com.vhre.sensor.telemetry.modules.sensornode.service;

import com.vhre.base.core.base.service.BaseServiceImpl;
import com.vhre.sensor.telemetry.modules.sensornode.dto.SensorNodeDTO;
import com.vhre.sensor.telemetry.modules.sensornode.entity.SensorNode;
import com.vhre.sensor.telemetry.modules.sensornode.mapper.SensorNodeMapper;
import com.vhre.sensor.telemetry.modules.sensornode.repository.SensorNodeRepository;

import java.util.UUID;

public class SensorNodeServiceImpl extends BaseServiceImpl<SensorNode, SensorNodeDTO, UUID> implements SensorNodeService {
    public SensorNodeServiceImpl(SensorNodeRepository repository, SensorNodeMapper mapper) {
        super(repository, mapper);
    }
}
