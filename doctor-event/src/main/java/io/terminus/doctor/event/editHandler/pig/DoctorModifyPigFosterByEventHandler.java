package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/4/19.
 * 被拼窝
 */
@Component
public class DoctorModifyPigFosterByEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        return false;
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorFosterByDto newDto = (DoctorFosterByDto) inputDto;
        newEvent.setWeight(newDto.getFosterByTotalWeight());
        newEvent.setQuantity(newDto.getFosterByCount());
        return newEvent;
    }

}
