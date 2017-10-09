package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.terminus.common.utils.Arguments.isNull;

/**
 * 转舍
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class ChgLocationPigEventBuilder extends AbstractPigEventBuilder<DoctorChgLocationDto> {

    @Autowired
    private DoctorPigReadService doctorPigReadService;
    @Autowired
    private DoctorBarnReadService doctorBarnReadService;

    @Override
    void buildEventDto(DoctorChgLocationDto eventDto, DoctorPigEvent pigEvent) {
        String fromBarnName;
        if (isNull(eventDto.getChgLocationFromBarnId())) {
            DoctorPigTrack doctorPigTrack1 = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigEvent.getPigId()));
            fromBarnName = doctorPigTrack1.getCurrentBarnName();
            eventDto.setChgLocationFromBarnId(doctorPigTrack1.getCurrentBarnId());
        } else {
            DoctorBarn fromBarn = RespHelper.or500(doctorBarnReadService.findBarnById(eventDto.getChgLocationFromBarnId()));
            fromBarnName = fromBarn.getName();
        }
        eventDto.setChgLocationFromBarnName(fromBarnName);
        eventDto.setChgLocationToBarnName(RespHelper.or500(doctorBarnReadService.findBarnById(eventDto.getChgLocationToBarnId())).getName());
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.CHG_LOCATION.getKey().intValue() || pigEvent.getType().intValue() == PigEvent.TO_MATING.getKey() || pigEvent.getType().intValue() == PigEvent.TO_FARROWING.getKey();
    }
}
