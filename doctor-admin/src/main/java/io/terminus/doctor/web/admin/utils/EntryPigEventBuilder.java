package io.terminus.doctor.web.admin.utils;

import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sunbo@terminus.io on 2017/9/13.
 */
@Component
public class EntryPigEventBuilder extends AbstractPigEventBuilder<DoctorFarmEntryDto> {

    @Autowired
    private DoctorBasicReadService doctorBasicReadService;
    @Autowired
    private DoctorBarnReadService doctorBarnReadService;
    @Autowired
    private DoctorPigWriteService doctorPigWriteService;
    @Autowired
    private DoctorPigReadService doctorPigReadService;

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.ENTRY.getKey().intValue()
                || pigEvent.getType().intValue() == PigEvent.CHG_FARM_IN.getKey();
    }

    @Override
    void buildEventDto(DoctorFarmEntryDto eventDto, DoctorPigEvent pigEvent) {

        //品种
        String breedName = RespHelper.orServEx(doctorBasicReadService.findBasicById(eventDto.getBreed())).getName();
        eventDto.setBreedName(breedName);

        //品系
        if (null != eventDto.getBreedType()) {
            String breedTypeName = RespHelper.orServEx(doctorBasicReadService.findBasicById(eventDto.getBreedType())).getName();
            eventDto.setBreedTypeName(breedTypeName);
        }

        DoctorBarn doctorBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(eventDto.getBarnId()));
        if (null == doctorBarn)
            throw new InvalidException("barn.not.null", eventDto.getBarnId());

        eventDto.setBarnType(doctorBarn.getPigType());
        pigEvent.setBreedId(eventDto.getBreed());
    }

}
