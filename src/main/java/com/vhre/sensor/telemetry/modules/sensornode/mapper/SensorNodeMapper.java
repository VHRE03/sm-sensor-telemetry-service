package com.vhre.sensor.telemetry.modules.sensornode.mapper;

import com.vhre.base.core.base.mapper.BaseMapper;
import com.vhre.sensor.telemetry.modules.sensornode.dto.SensorNodeDTO;
import com.vhre.sensor.telemetry.modules.sensornode.entity.SensorNode;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SensorNodeMapper extends BaseMapper<SensorNode, SensorNodeDTO> {
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "connectionStatus", source = "connectionStatus")
    @Mapping(target = "batteryLevel", source = "batteryLevel")
    SensorNode toEntity(SensorNodeDTO dto);
}
