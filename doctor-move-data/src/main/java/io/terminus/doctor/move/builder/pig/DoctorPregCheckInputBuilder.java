package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/8/4.
 * 妊娠检查
 */
@Component
public class DoctorPregCheckInputBuilder implements DoctorPigEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow) pigRawEvent;

        DoctorPregChkResultDto preg = new DoctorPregChkResultDto();
        builderCommonOperation.fillPigEventCommonInput(preg, moveBasicData, pigRawEvent);

        preg.setCheckDate(event.getEventAt());
        preg.setCheckMark(event.getRemark());
        PregCheckResult result = PregCheckResult.from(event.getPregCheckResult());
        preg.setCheckResult(result == null ? null : result.getKey());
        return preg;
    }

    @Override
    public BasePigEventInputDto buildFromImport(DoctorImportBasicData importBasicData, DoctorImportPigEvent importPigEvent) {
        DoctorPregChkResultDto preg = new DoctorPregChkResultDto();
        builderCommonOperation.fillPigEventCommonInput(preg, importBasicData, importPigEvent);

        preg.setCheckDate(importPigEvent.getEventAt());
        preg.setCheckMark(importPigEvent.getRemark());
        PregCheckResult result = PregCheckResult.from(importPigEvent.getPregCheckResult());
        expectTrue(notNull(result), "result.not.fund", importPigEvent.getPregCheckResult());
        preg.setCheckResult(result.getKey());
        return preg;
    }
}
