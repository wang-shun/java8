package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.getAfterDay;

/**
 * Created by xjn on 17/4/18.
 * 进场
 */
@Component
public class DoctorModifyPigEntryEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Override
    public boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        List<DoctorPigEvent> list = doctorPigEventDao.findByPigId(deletePigEvent.getPigId());
        return list.size() == 1;
    }

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
        oldPig.setBirthDate(newDto.getBirthday());
        return oldPig;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (DateUtils.isSameDay(changeDto.getNewEventAt(), changeDto.getOldEventAt())
                && Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.BOAR.getKey())) {
            DoctorFarmEntryDto newDto = (DoctorFarmEntryDto) inputDto;

            //1.活公猪 => 其他
            if (Objects.equals(oldPigEvent.getBoarType(), BoarEntryType.HGZ.getKey())
                    && !Objects.equals(newDto.getBoarType(), BoarEntryType.HGZ.getKey())) {
                updateDailyOfDelete(oldPigEvent);
                return;
            }

            //2.其他 => 火公猪
            if (!Objects.equals(oldPigEvent.getBoarType(), BoarEntryType.HGZ.getKey())
                    && Objects.equals(newDto.getBoarType(), BoarEntryType.HGZ.getKey())) {
                updateDailyOfNew(oldPigEvent, inputDto);
                return;
            }
            return;
        }
        updateDailyOfDelete(oldPigEvent);
        updateDailyOfNew(oldPigEvent, inputDto);
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        updateDailyOfDelete(deletePigEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.BOAR.getKey())
                && !Objects.equals(oldPigEvent.getBoarType(), BoarEntryType.HGZ.getKey())) {
            return;
        }
        //默认普通进场
        Integer entrySource = 1;
        if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())
                && notNull(oldPigEvent.getRelGroupEventId())) {
            entrySource = 2;
        }

        DoctorPigDaily oldDailyPig1 = doctorDailyReportManager.findDoctorPigDaily(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .pigSex(oldPigEvent.getKind())
                .entrySource(entrySource)
                .entryCountChange(-1)
                .phCountChange(-1)
                .build();
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig1, changeDto1));
        //旧版
        //DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        //createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto1));

        if (Objects.equals(oldPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            //更新母猪存栏
            updateDailySowPigLiveStock(oldPigEvent.getFarmId(),  getAfterDay(oldPigEvent.getEventAt()),
                    changeDto1.getEntryCountChange(), changeDto1.getPhCountChange(), changeDto1.getCfCountChange());

            //更新空怀母猪数
            updateDailyPhStatusLiveStock(oldPigEvent.getFarmId(), oldPigEvent.getEventAt()
                    , 0, -1, 0);
        } else {
            updateDailyBoarPigLiveStock(oldPigEvent.getFarmId(), getAfterDay(oldPigEvent.getEventAt()), changeDto1.getEntryCountChange());
        }
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorFarmEntryDto newDto = (DoctorFarmEntryDto) inputDto;
        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.BOAR.getKey())
                && !Objects.equals(newDto.getBoarType(), BoarEntryType.HGZ.getKey())) {
            return;
        }
        //默认普通进场
        Integer entrySource = 1;
        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())
                && notNull(newPigEvent.getRelGroupEventId())) {
            entrySource = 2;
        }
        DoctorPigDaily oldDailyPig2 = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), inputDto.eventAt());
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .pigSex(newPigEvent.getKind())
                .entrySource(entrySource)
                .entryCountChange(1)
                .phCountChange(1)
                .build();
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig2, changeDto2));
        //旧版
       // DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), inputDto.eventAt());
       // oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto2));

        if (Objects.equals(newPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            //更新母猪存栏
            updateDailySowPigLiveStock(newPigEvent.getFarmId(), getAfterDay(inputDto.eventAt()),
                    changeDto2.getEntryCountChange(), changeDto2.getPhCountChange(), changeDto2.getCfCountChange());

            //更新空怀母猪数
            updateDailyPhStatusLiveStock(newPigEvent.getFarmId(), inputDto.eventAt()
                    , 0, 1, 0);

        } else {
            updateDailyBoarPigLiveStock(newPigEvent.getFarmId(), getAfterDay(inputDto.eventAt()), changeDto2.getEntryCountChange());
        }
    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);

        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.BOAR.getKey())) {
            oldDailyPig.setBoarIn(EventUtil.plusInt(oldDailyPig.getBoarIn(), changeDto.getEntryCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.plusInt(oldDailyPig.getBoarEnd(), changeDto.getEntryCountChange()));
            return oldDailyPig;
        }

        oldDailyPig.setSowPhEnd(EventUtil.plusInt(oldDailyPig.getSowPhEnd(), changeDto.getEntryCountChange()));
        if (changeDto.getEntrySource() == 2) {
            oldDailyPig.setSowPhReserveIn(EventUtil.plusInt(oldDailyPig.getSowPhReserveIn(), changeDto.getEntryCountChange()));
        }
        oldDailyPig.setSowPhEntryIn(EventUtil.plusInt(oldDailyPig.getSowPhEntryIn(), changeDto.getEntryCountChange()));
        return oldDailyPig;
    }

    protected DoctorDailyReport oldBuildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {

        if (Objects.equals(changeDto.getPigSex(), DoctorPig.PigSex.BOAR.getKey())) {
            oldDailyPig.setBoarIn(EventUtil.plusInt(oldDailyPig.getBoarIn(), changeDto.getEntryCountChange()));
            oldDailyPig.setBoarEnd(EventUtil.plusInt(oldDailyPig.getBoarEnd(), changeDto.getEntryCountChange()));
            return oldDailyPig;
        }

        oldDailyPig.setSowIn(EventUtil.plusInt(oldDailyPig.getSowIn(), changeDto.getEntryCountChange()));
        oldDailyPig.setSowEnd(EventUtil.plusInt(oldDailyPig.getSowEnd(), changeDto.getEntryCountChange()));
        oldDailyPig.setSowPh(EventUtil.plusInt(oldDailyPig.getSowPh(), changeDto.getEntryCountChange()));
        oldDailyPig.setSowPhEnd(EventUtil.plusInt(oldDailyPig.getSowPhEnd(), changeDto.getEntryCountChange()));
        if (changeDto.getEntrySource() == 2) {
            oldDailyPig.setSowPhReserveIn(EventUtil.plusInt(oldDailyPig.getSowPhReserveIn(), changeDto.getEntryCountChange()));
        }
        oldDailyPig.setSowPhInFarmIn(EventUtil.plusInt(oldDailyPig.getSowPhInFarmIn(), changeDto.getEntryCountChange()));
        return oldDailyPig;
    }
}
