package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * 拼窝
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class FostersPigEventBuilder extends AbstractPigEventBuilder<DoctorFostersDto> {

    @Override
    void buildEventDto(DoctorFostersDto eventDto, DoctorPigEvent pigEvent) {
        pigEvent.setQuantity(eventDto.getFostersCount());
        pigEvent.setWeight(eventDto.getFosterTotalWeight());
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.FOSTERS.getKey().intValue();
    }
}
