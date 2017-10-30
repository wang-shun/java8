package io.terminus.doctor.web.admin.handler.event.group;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.parana.user.model.UserProfile;
import org.springframework.stereotype.Component;

/**
 * 猪群疾病
 * Created by sunbo@terminus.io on 2017/9/18.
 */
@Component
public class DiseaseGroupEventHandler extends AbstractGroupEventHandler<DoctorDiseaseGroupInput> {


    @Override
    protected void buildEventDto(DoctorDiseaseGroupInput eventDto, DoctorGroupEvent groupEvent) {

        eventDto.setDiseaseName(getBarnName(eventDto.getDiseaseId()));
        eventDto.setDoctorName(getName(eventDto.getDoctorId()));

        groupEvent.setBasicId(eventDto.getDiseaseId());
        groupEvent.setBasicName(eventDto.getDiseaseName());
        groupEvent.setOperatorId(eventDto.getDoctorId());
        groupEvent.setOperatorName(eventDto.getDoctorName());
    }

    @Override
    public boolean isSupported(DoctorGroupEvent groupEvent) {
        return false;
    }
}
