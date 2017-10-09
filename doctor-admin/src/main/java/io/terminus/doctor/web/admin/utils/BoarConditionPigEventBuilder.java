package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * 公猪体况处理
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class BoarConditionPigEventBuilder extends AbstractPigEventBuilder<DoctorBoarConditionDto> {

    @Override
    void buildEventDto(DoctorBoarConditionDto eventDto, DoctorPigEvent pigEvent) {

    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.CONDITION.getKey().intValue() &&
                pigEvent.getKind().intValue() == DoctorPig.PigSex.BOAR.getKey().intValue();
    }
}
