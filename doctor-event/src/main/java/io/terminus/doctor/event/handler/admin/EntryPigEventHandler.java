package io.terminus.doctor.event.handler.admin;

import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.PigEventHandler;
import io.terminus.doctor.event.manager.DoctorPigManager;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorPigReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sunbo@terminus.io on 2017/10/9.
 */
@Component
public class EntryPigEventHandler extends AbstractPigEventHandler implements PigEventHandler {

    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorPigReadService doctorPigReadService;

    @Autowired
    private DoctorPigManager doctorPigManager;

    @Override
    public boolean isSupportedEvent(DoctorPigEvent pigEvent) {
        return pigEvent.getType().intValue() == PigEvent.ENTRY.getKey();
    }

    @Override
    public void handle(DoctorPigEvent pigEvent) {


//        DoctorPig pig = doctorPigDao.findById(pigEvent.getPigId());
//        DoctorFarmEntryDto dto = jsonMapper.fromJson(pigEvent.getExtra(), DoctorFarmEntryDto.class);
//
//        pig.setInFarmDate(dto.getInFarmDate());
//        pig.setBirthDate(dto.getBirthday());
//
//        pig.setBreedId(dto.getBreed());
//        pig.setBreedName(dto.getBreedName());
//
//        pig.setGeneticId(dto.getBreedType());
//        pig.setGeneticName(dto.getBreedTypeName());
//
//        pig.setSource(dto.getSource());
//        pig.setPigFatherCode(dto.getFatherCode());
//        pig.setPigMotherCode(dto.getMotherCode());
//
//        doctorPigDao.update(pig);
    }
}
