package io.terminus.doctor.event.event;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import lombok.Data;

import java.util.Map;

/**
 * Created by chenzenghui on 16/8/13.
 */
@Data
public class TurnSeedEvent{
    private Map<String,Object> createCasualPigEventResult;

    private DoctorBasicInputInfoDto basicInputInfoDto;
}
