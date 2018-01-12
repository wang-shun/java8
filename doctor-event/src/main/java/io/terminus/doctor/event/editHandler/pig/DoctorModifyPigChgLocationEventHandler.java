package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupTransGroupEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.*;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
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
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (!Objects.equals(changeDto.getOldEventAt(), changeDto.getNewEventAt())) {
            updateDailyOfDelete(oldPigEvent);
            updateDailyOfNew(oldPigEvent, inputDto);
        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        DoctorGroupEvent transGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(newPigEvent.getId(), GroupEventType.TRANS_GROUP.getValue());
        if (notNull(transGroupEvent)) {
            BaseGroupInput newInput = JSON_MAPPER.fromJson(transGroupEvent.getExtra(), DoctorTransGroupInput.class);
            //转舍目前只支持更改时间
            newInput.setEventAt(DateUtil.toDateString(newPigEvent.getEventAt()));
            doctorModifyGroupTransGroupEventHandler.modifyHandle(transGroupEvent, newInput);
        }
    }

    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        DoctorGroupEvent transGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.TRANS_GROUP.getValue());
        if (notNull(transGroupEvent)) {
            return doctorModifyGroupTransGroupEventHandler.rollbackHandleCheck(transGroupEvent);
        }
        return true;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        DoctorGroupEvent transGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.TRANS_GROUP.getValue());
        if (notNull(transGroupEvent)) {
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
        if (Objects.equals(deletePigEvent.getType(), PigEvent.CHG_LOCATION.getKey())
                && Objects.equals(deletePigEvent.getBarnType(), PigType.DELIVER_SOW.getValue())) {
            oldPigTrack.setGroupId(deletePigEvent.getGroupId());
        }
        return oldPigTrack;
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        DoctorEventChangeDto changeDto;
        Integer matingChangeCount;
        Integer farrowChangCount;
        if (Objects.equals(oldPigEvent.getType(), PigEvent.TO_FARROWING.getKey())) {
            changeDto = DoctorEventChangeDto.builder()
                    .chgLocationChangeCount(-1)
                    .chgLocationType(PigEvent.TO_FARROWING.getKey())
                    .build();
            matingChangeCount = 1;
            farrowChangCount = -1;

        } else if (Objects.equals(oldPigEvent.getType(), PigEvent.TO_MATING.getKey())) {
            changeDto = DoctorEventChangeDto.builder()
                    .chgLocationChangeCount(-1)
                    .chgLocationType(PigEvent.TO_MATING.getKey())
                    .build();
            matingChangeCount = -1;
            farrowChangCount = 1;
        } else {
            return;
        }
        DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig, changeDto));
        doctorDailyPigDao.updateDailySowPigLiveStock(oldPigEvent.getFarmId(), getAfterDay(oldPigEvent.getEventAt()),
                0, matingChangeCount, farrowChangCount);
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorEventChangeDto changeDto;
        Integer matingChangeCount;
        Integer farrowChangCount;
        if (Objects.equals(newPigEvent.getType(), PigEvent.TO_FARROWING.getKey())) {
            changeDto = DoctorEventChangeDto.builder()
                    .chgLocationChangeCount(1)
                    .chgLocationType(PigEvent.TO_FARROWING.getKey())
                    .build();
            matingChangeCount = -1;
            farrowChangCount = 1;
        } else if (Objects.equals(newPigEvent.getType(), PigEvent.TO_MATING.getKey())) {
            changeDto = DoctorEventChangeDto.builder()
                    .chgLocationChangeCount(1)
                    .chgLocationType(PigEvent.TO_MATING.getKey())
                    .build();
            matingChangeCount = 1;
            farrowChangCount = -1;
        } else {
            return;
        }
        DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), inputDto.eventAt());
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig, changeDto));
        doctorDailyPigDao.updateDailySowPigLiveStock(newPigEvent.getFarmId(), getAfterDay(inputDto.eventAt()),
                0, matingChangeCount, farrowChangCount);
    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        if (Objects.equals(changeDto.getChgLocationType(), PigEvent.TO_MATING.getKey())) {
            oldDailyPig.setSowPhEnd(EventUtil.plusInt(oldDailyPig.getSowPhEnd(), changeDto.getChgLocationChangeCount()));
            oldDailyPig.setSowPhWeanIn(EventUtil.plusInt(oldDailyPig.getSowPhWeanIn(), changeDto.getChgLocationChangeCount()));
            oldDailyPig.setSowCfEnd(EventUtil.minusInt(oldDailyPig.getSowCfEnd(), changeDto.getChgLocationChangeCount()));
            return oldDailyPig;
        }
        oldDailyPig.setSowPhEnd(EventUtil.minusInt(oldDailyPig.getSowPhEnd(), changeDto.getChgLocationChangeCount()));
        oldDailyPig.setSowCfEnd(EventUtil.plusInt(oldDailyPig.getSowCfEnd(), changeDto.getChgLocationChangeCount()));
        oldDailyPig.setSowCfIn(EventUtil.plusInt(oldDailyPig.getSowCfIn(), changeDto.getChgLocationChangeCount()));
        return oldDailyPig;
    }
}
