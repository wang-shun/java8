package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by xjn on 17/4/18.
 */
@Component
public class DoctorModifyPigEntryEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorFarmEntryDto newDto = (DoctorFarmEntryDto) inputDto;
        newEvent.setBreedId(newDto.getBreed());
        newEvent.setBreedName(newDto.getBreedName());
        newEvent.setBreedTypeId(newDto.getBreedType());
        newEvent.setBreedTypeName(newDto.getBreedTypeName());
        newEvent.setBoarType(newDto.getBoarType());
        newEvent.setSource(newDto.getSource());
        return newEvent;
    }

    @Override
    public DoctorPig buildNewPig(DoctorPig oldPig, BasePigEventInputDto inputDto) {
        DoctorFarmEntryDto newDto = (DoctorFarmEntryDto) inputDto;
        oldPig.setBreedId(newDto.getBreed());
        oldPig.setBreedName(newDto.getBreedName());
        oldPig.setGeneticId(newDto.getBreedType());
        oldPig.setGeneticName(newDto.getBreedTypeName());
        oldPig.setInFarmDate(newDto.getInFarmDate());
        oldPig.setSource(newDto.getSource());
        oldPig.setBoarType(newDto.getBoarType());
        oldPig.setPigFatherCode(newDto.getFatherCode());
        oldPig.setPigMotherCode(newDto.getMotherCode());
        return oldPig;
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            return;
        }
        DoctorDailyReport oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());

        DoctorDailyReport oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
    }

    private DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, Integer quantityChange) {
        oldDailyPig.setSowIn(EventUtil.plusInt(oldDailyPig.getSowIn(), quantityChange));
        oldDailyPig.setSowEnd(EventUtil.plusInt(oldDailyPig.getSowEnd(), quantityChange));
        return oldDailyPig;
    }
}
