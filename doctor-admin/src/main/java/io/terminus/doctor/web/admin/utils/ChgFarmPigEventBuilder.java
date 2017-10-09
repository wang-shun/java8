package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * 转场
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class ChgFarmPigEventBuilder extends AbstractPigEventBuilder<DoctorChgFarmDto> {

    @Autowired
    private DoctorFarmReadService doctorFarmReadService;
    @Autowired
    private DoctorPigReadService doctorPigReadService;
    @Autowired
    private DoctorBarnReadService doctorBarnReadService;

    @Override
    void buildEventDto(DoctorChgFarmDto eventDto, DoctorPigEvent pigEvent) {

        DoctorFarm fromFarm = RespHelper.or500(doctorFarmReadService.findFarmById(pigEvent.getFarmId()));
        expectTrue(notNull(fromFarm), "farm.not.null", pigEvent.getFarmId());
        //构建来源场信息
        eventDto.setFromFarmId(fromFarm.getId());
        eventDto.setFromFarmName(fromFarm.getName());
        DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigEvent.getPigId()));
        expectTrue(notNull(doctorPigTrack), "pig.track.not.null", pigEvent.getPigId());
        eventDto.setFromBarnId(doctorPigTrack.getCurrentBarnId());
        eventDto.setFromBarnName(doctorPigTrack.getCurrentBarnName());
        //构建转入场信息
        DoctorFarm toFarm = RespHelper.or500(doctorFarmReadService.findFarmById(eventDto.getToFarmId()));
        expectTrue(notNull(fromFarm), "farm.not.null", pigEvent.getFarmId());
        DoctorBarn toBarn = RespHelper.or500(doctorBarnReadService.findBarnById(eventDto.getToBarnId()));
        expectTrue(notNull(toBarn), "barn.not.null", eventDto.getToBarnId());
        eventDto.setToFarmName(toFarm.getName());
        eventDto.setToBarnName(toBarn.getName());
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.CHG_FARM.getKey().intValue();
    }
}
