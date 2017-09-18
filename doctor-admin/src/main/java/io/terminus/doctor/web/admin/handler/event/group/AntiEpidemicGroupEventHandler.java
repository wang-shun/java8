package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Component;

/**
 * 猪群防疫
 * Created by sunbo@terminus.io on 2017/9/18.
 */
@Component
public class AntiEpidemicGroupEventHandler extends AbstractGroupEventHandler<DoctorAntiepidemicGroupInput> {

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    @Override
    protected void buildEventDto(DoctorAntiepidemicGroupInput eventDto, DoctorGroupEvent groupEvent) {
        DoctorBasicMaterial vaccine = RespHelper.orServEx(doctorBasicMaterialReadService.findBasicMaterialById(eventDto.getVaccinId()));
        if (null == vaccine)
            throw new InvalidException("basic.material.not.null", eventDto.getVaccinId());
        eventDto.setVaccinName(vaccine.getName());
        eventDto.setVaccinStaffName(getName(eventDto.getVaccinStaffId()));
        eventDto.setVaccinItemName(getBasicName(eventDto.getVaccinItemId()));

        groupEvent.setVaccinationId(eventDto.getVaccinId());
        groupEvent.setVaccinationName(eventDto.getVaccinName());
        groupEvent.setBasicId(eventDto.getVaccinItemId());
        groupEvent.setBasicName(eventDto.getVaccinItemName());
        groupEvent.setOperatorId(eventDto.getVaccinStaffId());
        groupEvent.setOperatorName(eventDto.getVaccinStaffName());
    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return false;
    }
}
