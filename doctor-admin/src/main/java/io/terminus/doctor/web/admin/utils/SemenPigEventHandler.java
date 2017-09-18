package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * 采精
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class SemenPigEventHandler extends AbstractPigEventHandler<DoctorSemenDto> {

    @Override
    void buildEventDto(DoctorSemenDto eventDto, DoctorPigEvent pigEvent) {

    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return PigEvent.SEMEN.getKey().intValue() == pigEvent.getType().intValue();
    }
}
