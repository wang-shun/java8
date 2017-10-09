package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 断奶
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class WeanPigEventBuilder extends AbstractPigEventBuilder<DoctorWeanDto> {

    @Autowired
    private DoctorPigEventReadService doctorPigEventReadService;

    @Override
    void buildEventDto(DoctorWeanDto eventDto, DoctorPigEvent pigEvent) {
        pigEvent.setWeanCount(eventDto.getPartWeanPigletsCount());
        pigEvent.setWeanAvgWeight(eventDto.getPartWeanAvgWeight());

        DoctorPigEvent farrowEvent = RespHelper.orServEx(doctorPigEventReadService.getFarrowEventByParity(pigEvent.getPigId(), pigEvent.getParity()));
        if (null == farrowEvent)
            throw new InvalidException("last.farrow.not.null", pigEvent.getPigId());
        pigEvent.setFeedDays(DateUtil.getDeltaDays(farrowEvent.getEventAt(), eventDto.eventAt()) + 1);

    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.WEAN.getKey().intValue();
    }
}
