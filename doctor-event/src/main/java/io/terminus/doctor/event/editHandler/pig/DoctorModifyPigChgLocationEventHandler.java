package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupTransGroupEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.getAfterDay;

/**
 * Created by xjn on 17/4/19.
 * 转舍
 */
@Component
public class DoctorModifyPigChgLocationEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Autowired
    private DoctorModifyGroupTransGroupEventHandler doctorModifyGroupTransGroupEventHandler;
    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorChgLocationDto newDto = (DoctorChgLocationDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .oldEventAt(oldPigEvent.getEventAt())
                .newEventAt(inputDto.eventAt())
                .eventType(oldPigEvent.getType())
                .toBarnId(newDto.getChgLocationToBarnId())
                .build();
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        DoctorBarn doctorBarn = doctorBarnDao.findById(changeDto.getToBarnId());
        oldPigTrack.setCurrentBarnId(doctorBarn.getId());
        oldPigTrack.setCurrentBarnName(doctorBarn.getName());
        oldPigTrack.setCurrentBarnType(doctorBarn.getPigType());
        return oldPigTrack;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        if (Objects.equals(deletePigEvent.getType(), PigEvent.CHG_LOCATION.getKey())
                && Objects.equals(deletePigEvent.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            DoctorGroupEvent transGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.TRANS_GROUP.getValue());
            doctorModifyGroupTransGroupEventHandler.rollbackHandle(transGroupEvent, operatorId, operatorName);
        }
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        updateDailyOfDelete(deletePigEvent);
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setCurrentBarnId(deletePigEvent.getBarnId());
        oldPigTrack.setCurrentBarnName(deletePigEvent.getBarnName());
        oldPigTrack.setCurrentBarnType(deletePigEvent.getBarnType());
        if (Objects.equals(deletePigEvent.getType(), PigEvent.TO_FARROWING.getKey())) {
            oldPigTrack.setStatus(PigStatus.Pregnancy.getKey());
        }
        return oldPigTrack;
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorEventChangeDto changeDto;
        if (Objects.equals(newPigEvent.getType(), PigEvent.TO_FARROWING.getKey())) {
            changeDto = DoctorEventChangeDto.builder()
                    .toFarrowChangeCount(1)
                    .toMatingChangeCount(-1)
                    .build();
        } else if (Objects.equals(newPigEvent.getType(), PigEvent.TO_MATING.getKey())) {
            changeDto = DoctorEventChangeDto.builder()
                    .toMatingChangeCount(1)
                    .toFarrowChangeCount(-1)
                    .build();
        } else {
            return;
        }
        DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), inputDto.eventAt());
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
        doctorDailyPigDao.updateDailySowPigLiveStock(newPigEvent.getFarmId(), getAfterDay(inputDto.eventAt()),
                0, changeDto.getToMatingChangeCount(), changeDto.getToFarrowChangeCount());
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        DoctorEventChangeDto changeDto;
        if (Objects.equals(oldPigEvent.getType(), PigEvent.TO_FARROWING.getKey())) {
            changeDto = DoctorEventChangeDto.builder()
                    .toFarrowChangeCount(-1)
                    .toMatingChangeCount(1)
                    .build();
        } else if (Objects.equals(oldPigEvent.getType(), PigEvent.TO_MATING.getKey())) {
            changeDto = DoctorEventChangeDto.builder()
                    .toMatingChangeCount(-1)
                    .toFarrowChangeCount(1)
                    .build();
        } else {
            return;
        }
        DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
        doctorDailyPigDao.updateDailySowPigLiveStock(oldPigEvent.getFarmId(), getAfterDay(oldPigEvent.getEventAt()),
                0, changeDto.getToMatingChangeCount(), changeDto.getToFarrowChangeCount());
    }

    @Override
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig.setSowPh(EventUtil.plusInt(oldDailyPig.getSowPh(), changeDto.getToMatingChangeCount()));
        oldDailyPig.setSowPhEnd(EventUtil.plusInt(oldDailyPig.getSowPhEnd(), changeDto.getToMatingChangeCount()));
        oldDailyPig.setSowPhToCf(EventUtil.plusInt(oldDailyPig.getSowPhToCf(), changeDto.getToFarrowChangeCount()));
        oldDailyPig.setSowCf(EventUtil.plusInt(oldDailyPig.getSowCf(), changeDto.getToFarrowChangeCount()));
        oldDailyPig.setSowCfEnd(EventUtil.plusInt(oldDailyPig.getSowCfEnd(), changeDto.getToFarrowChangeCount()));
        return oldDailyPig;
    }
}
