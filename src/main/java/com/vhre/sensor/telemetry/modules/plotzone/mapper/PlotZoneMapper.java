package com.vhre.sensor.telemetry.modules.plotzone.mapper;

import com.vhre.base.core.base.mapper.BaseMapper;
import com.vhre.sensor.telemetry.modules.plotzone.dto.PlotZoneDTO;
import com.vhre.sensor.telemetry.modules.plotzone.entity.PlotZone;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PlotZoneMapper extends BaseMapper<PlotZone, PlotZoneDTO> {

    @Mapping(target = "id", ignore = true)
    PlotZone toEntity(PlotZoneDTO dto);
}