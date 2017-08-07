package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/8/4.
 * 妊娠检查
 */
@Component
public class DoctorPregCheckInputBuilder implements DoctorPigEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildPigEventInputFromMove(DoctorMoveBasicData moveBasicData,
                                                           View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow) pigRawEvent;

        DoctorPregChkResultDto preg = new DoctorPregChkResultDto();
        builderCommonOperation.fillPigEventCommonInputFromMove(preg, moveBasicData, pigRawEvent);

        preg.setCheckDate(event.getEventAt());
        preg.setCheckMark(event.getRemark());
        PregCheckResult result = PregCheckResult.from(event.getPregCheckResult());
        preg.setCheckResult(result == null ? null : result.getKey());
        return preg;
    }
}
