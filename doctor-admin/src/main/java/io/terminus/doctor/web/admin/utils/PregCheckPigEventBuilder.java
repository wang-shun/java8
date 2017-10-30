package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * 妊娠检查
 * Created by sunbo@terminus.io on 2017/9/14.
 */
@Component
public class PregCheckPigEventBuilder extends AbstractPigEventBuilder<DoctorPregChkResultDto> {

    @Override
    void buildEventDto(DoctorPregChkResultDto eventDto, DoctorPigEvent pigEvent) {

        pigEvent.setPregCheckResult(eventDto.getCheckResult());

        pigEvent.setPigStatusAfter(eventDto.getCheckResult().intValue() == PregCheckResult.YANG.getKey().intValue()
                ? PigStatus.Pregnancy.getKey() : PigStatus.KongHuai.getKey());
    }


    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.PREG_CHECK.getKey().intValue();
    }
}
