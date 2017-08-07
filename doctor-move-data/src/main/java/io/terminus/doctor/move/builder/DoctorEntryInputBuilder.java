package io.terminus.doctor.move.builder;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/8/4.
 */
@Component
public class DoctorEntryInputBuilder implements DoctorPigEventInputBuilder{
    @Override
    public BasePigEventInputDto buildPigEventInput(DoctorMoveBasicData moveBasicData,
                                                   View_EventListPig pigRawEvent) {
        return null;
    }
}
