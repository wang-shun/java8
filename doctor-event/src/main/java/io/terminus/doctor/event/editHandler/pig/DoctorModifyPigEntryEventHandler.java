package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.getAfterDay;

/**
 * Created by xjn on 17/4/18.
 * 进场
 */
@Component
public class DoctorModifyPigEntryEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldPigEvent.getEventAt())
                .newEventAt(inputDto.eventAt())
                .build();
    }

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
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        oldPigTrack.setCurrentParity(changeDto.getPigParity());
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (!Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyReport oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder().entryCountChange(-1).build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));
            if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
                doctorDailyPigDao.updateDailySowPigLiveStock(changeDto.getFarmId(), getAfterDay(changeDto.getOldEventAt()), -1);
            } else {
                doctorDailyPigDao.updateDailyBoarPigLiveStock(changeDto.getFarmId(), getAfterDay(changeDto.getOldEventAt()), -1);
            }

            DoctorDailyReport oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder().entryCountChange(1).build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig2, changeDto2));
            if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
                doctorDailyPigDao.updateDailySowPigLiveStock(changeDto.getFarmId(), getAfterDay(changeDto.getNewEventAt()), 1);
            } else {
                doctorDailyPigDao.updateDailyBoarPigLiveStock(changeDto.getFarmId(), getAfterDay(changeDto.getNewEventAt()), 1);
            }
        }
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
        DoctorEventChangeDto changeDto = DoctorEventChangeDto.builder().entryCountChange(-1).build();
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
        if (Objects.equals(deletePigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            doctorDailyPigDao.updateDailySowPigLiveStock(deletePigEvent.getFarmId(), getAfterDay(deletePigEvent.getEventAt()), -1);
        } else {
            doctorDailyPigDao.updateDailyBoarPigLiveStock(deletePigEvent.getFarmId(), getAfterDay(deletePigEvent.getEventAt()), -1);
        }
    }

    @Override
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.SOW.getKey())) {
            oldDailyPig.setSowIn(EventUtil.plusInt(oldDailyPig.getSowIn(), changeDto.getEntryCountChange()));
            oldDailyPig.setSowEnd(EventUtil.plusInt(oldDailyPig.getSowEnd(), changeDto.getEntryCountChange()));
            oldDailyPig.setSowPh(EventUtil.plusInt(oldDailyPig.getSowPh(), changeDto.getEntryCountChange()));
        } else {
            oldDailyPig.setBoarIn(EventUtil.plusInt(oldDailyPig.getBoarIn(), changeDto.getEntryCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.plusInt(oldDailyPig.getBoarEnd(), changeDto.getEntryCountChange()));
        }
        return oldDailyPig;
    }
}
