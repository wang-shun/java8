package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 疾病
 * Created by sunbo@terminus.io on 2017/9/15.
 */
@Component
public class DiseasePigEventBuilder extends AbstractPigEventBuilder<DoctorDiseaseDto> {

    @Autowired
    private DoctorBasicReadService doctorBasicReadService;

    @Override
    void buildEventDto(DoctorDiseaseDto eventDto, DoctorPigEvent pigEvent) {

        DoctorBasic disease = RespHelper.or500(doctorBasicReadService.findBasicById(eventDto.getDiseaseId()));
        if (null == disease)
            throw new InvalidException("basic.not.null", eventDto.getDiseaseId());

        pigEvent.setBasicId(eventDto.getDiseaseId());
        pigEvent.setBasicName(disease.getName());
        eventDto.setDiseaseName(disease.getName());
    }

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.DISEASE.getKey().intValue();
    }
}
