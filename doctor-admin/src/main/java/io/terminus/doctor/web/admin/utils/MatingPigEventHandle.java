package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 配种
 * Created by sunbo@terminus.io on 2017/9/14.
 */
@Component
public class MatingPigEventHandle extends AbstractPigEventHandler<DoctorMatingDto> {

    @Autowired
    private DoctorPigReadService doctorPigReadService;

    @Override
    void buildEventDto(DoctorMatingDto eventDto, DoctorPigEvent pigEvent) {
        DoctorPig boar = RespHelper.or500(doctorPigReadService.findPigById(eventDto.getMatingBoarPigId()));
        eventDto.setMatingBoarPigCode(boar.getPigCode());
        pigEvent.setBoarCode(boar.getPigCode());
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.MATING.getKey().intValue();
    }
}
