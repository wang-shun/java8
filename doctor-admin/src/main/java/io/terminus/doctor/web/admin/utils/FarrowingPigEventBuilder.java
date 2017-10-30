package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 分娩
 * Created by sunbo@terminus.io on 2017/9/14.
 */
@Component
public class FarrowingPigEventBuilder extends AbstractPigEventBuilder<DoctorFarrowingDto> {


    @Autowired
    private DoctorPigEventReadService doctorPigEventReadService;

    @Override
    void buildEventDto(DoctorFarrowingDto eventDto, DoctorPigEvent pigEvent) {
        pigEvent.setFarrowWeight(eventDto.getBirthNestAvg());
        pigEvent.setLiveCount(eventDto.getFarrowingLiveCount());
        DoctorPigEvent mateEvent = RespHelper.orServEx(doctorPigEventReadService.findLastFirstMateEvent(pigEvent.getPigId(), pigEvent.getParity()));
        pigEvent.setPregDays(DateUtil.getDeltaDays(mateEvent.getEventAt(), eventDto.eventAt()) + 1);
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.FARROWING.getKey().intValue();
    }
}
