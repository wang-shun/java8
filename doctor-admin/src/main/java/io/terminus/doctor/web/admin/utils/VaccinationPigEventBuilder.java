package io.terminus.doctor.web.admin.utils;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * 防疫
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class VaccinationPigEventBuilder extends AbstractPigEventBuilder<DoctorVaccinationDto> {

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;
    @RpcConsumer
    private DoctorBasicReadService doctorBasicReadService;

    @Override
    void buildEventDto(DoctorVaccinationDto eventDto, DoctorPigEvent pigEvent) {
        DoctorBasicMaterial vaccination = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(eventDto.getVaccinationId()));
        if (null == vaccination)
            throw new InvalidException("basic.material.not.null", eventDto.getVaccinationId());
        eventDto.setVaccinationName(vaccination.getName());

        pigEvent.setBasicId(eventDto.getVaccinationItemId());

        DoctorBasic vaccinationItem = RespHelper.or500(doctorBasicReadService.findBasicById(eventDto.getVaccinationItemId()));
        if (null == vaccinationItem)
            throw new InvalidException("basic.not.null", eventDto.getVaccinationItemId());
        pigEvent.setBasicName(vaccinationItem.getName());
        eventDto.setVaccinationItemName(vaccinationItem.getName());
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.VACCINATION.getKey().intValue();
    }
}
