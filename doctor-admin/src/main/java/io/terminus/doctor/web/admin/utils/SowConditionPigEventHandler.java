package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * 母猪体况
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class SowConditionPigEventHandler extends AbstractPigEventHandler<DoctorConditionDto> {

    @Override
    void buildEventDto(DoctorConditionDto eventDto, DoctorPigEvent pigEvent) {
        if (eventDto.getConditionWeight() != null) {
            pigEvent.setWeight(eventDto.getConditionWeight());
        }
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.CONDITION.getKey().intValue() &&
                pigEvent.getKind().intValue() == DoctorPig.PigSex.SOW.getKey().intValue();
    }
}
