package com.vhre.sensor.telemetry.modules.plotzone.service;

import com.vhre.base.core.base.service.BaseServiceImpl;
import com.vhre.sensor.telemetry.modules.plotzone.dto.PlotZoneDTO;
import com.vhre.sensor.telemetry.modules.plotzone.entity.PlotZone;
import com.vhre.sensor.telemetry.modules.plotzone.mapper.PlotZoneMapper;
import com.vhre.sensor.telemetry.modules.plotzone.repository.PlotZoneRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlotZoneServiceImpl extends BaseServiceImpl<PlotZone, PlotZoneDTO, UUID> implements PlotZoneService {
    public PlotZoneServiceImpl(PlotZoneRepository repository, PlotZoneMapper mapper) {
        super(repository, mapper);
    }
}
