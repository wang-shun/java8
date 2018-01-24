package io.terminus.doctor.event.editHandler.pig;

import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorWeanGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupWeanEventHandler;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;


/**
 * Created by xjn on 17/4/17.
 * 断奶编辑回滚处理
 */
@Component
public class DoctorModifyPigWeanEventHandler extends DoctorAbstractModifyPigEventHandler {
    @Autowired
    private DoctorModifyGroupWeanEventHandler modifyGroupWeanEventHandler;
    @Autowired
    private DoctorModifyPigChgLocationEventHandler modifyPigChgLocationEventHandler;

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorWeanDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorWeanDto.class);
        DoctorWeanDto newDto = (DoctorWeanDto) inputDto;
        return DoctorEventChangeDto.builder()
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
                .newEventAt(newDto.eventAt())
                .oldEventAt(oldDto.eventAt())
                .weanCountChange(EventUtil.minusInt(newDto.getPartWeanPigletsCount(), oldDto.getPartWeanPigletsCount()))
                .weanQualifiedCount(EventUtil.minusInt(newDto.getQualifiedCount(), oldDto.getQualifiedCount()))
                .weanAvgWeight(newDto.getPartWeanAvgWeight())
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorWeanDto newDto = (DoctorWeanDto) inputDto;
        newEvent.setFeedDays(getWeanAvgAge(oldPigEvent.getPigId(), oldPigEvent.getParity(), newDto.eventAt()));
        newEvent.setWeanCount(newDto.getPartWeanPigletsCount());
        newEvent.setHealthCount(newDto.getQualifiedCount());
        newEvent.setWeanAvgWeight(newDto.getPartWeanAvgWeight());
        return newEvent;
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        //1.猪群断奶编辑
        DoctorGroupEvent oldGroupEvent = doctorGroupEventDao.findByRelPigEventId(newPigEvent.getId());
        modifyGroupWeanEventHandler.modifyHandle(oldGroupEvent, buildTriggerGroupEventInput(newPigEvent));

        //2.转舍编辑
        DoctorPigEvent chgLocationEvent = doctorPigEventDao.findByRelPigEventId(newPigEvent.getId());
        if (notNull(chgLocationEvent)) {
            DoctorChgLocationDto inputDto = JSON_MAPPER.fromJson(chgLocationEvent.getExtra(), DoctorChgLocationDto.class);
            inputDto.setChangeLocationDate(newPigEvent.getEventAt());
            modifyPigChgLocationEventHandler.modifyHandle(chgLocationEvent, inputDto);
        }
    }

    @Override
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(changeDto.getFarmId(), changeDto.getOldEventAt());
            changeDto.setWeanDayAge(getWeanAvgAge(oldPigEvent.getPigId(), oldPigEvent.getParity(), changeDto.getOldEventAt()));
            doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig, changeDto));
        } else {
            updateDailyOfDelete(oldPigEvent);
            updateDailyOfNew(oldPigEvent, inputDto);
        }

    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        //1.猪群断奶回滚
        DoctorGroupEvent weanGroupEvent = doctorGroupEventDao.findByRelPigEventId(deletePigEvent.getId());
        modifyGroupWeanEventHandler.rollbackHandle(weanGroupEvent, operatorId, operatorName);

        //2.转舍回滚
        DoctorPigEvent chgLocationEvent = doctorPigEventDao.findByRelPigEventId(deletePigEvent.getId());
        if (notNull(chgLocationEvent)) {
            modifyPigChgLocationEventHandler.rollbackHandle(chgLocationEvent, operatorId, operatorName);
        }
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setStatus(PigStatus.FEED.getKey());
        oldPigTrack.setUnweanQty(deletePigEvent.getWeanCount());
        DoctorPigEvent farrowEvent = doctorPigEventDao.getFarrowEventByParity(deletePigEvent.getPigId(), deletePigEvent.getParity());
        oldPigTrack.setFarrowQty(farrowEvent.getLiveCount());
        oldPigTrack.setWeanQty(0);
        oldPigTrack.setFarrowAvgWeight(EventUtil.getAvgWeight(farrowEvent.getWeight(), farrowEvent.getLiveCount()));
        oldPigTrack.setWeanAvgWeight(0D);
        oldPigTrack.setGroupId(deletePigEvent.getGroupId());
        oldPigTrack.setExtraMap(Maps.newHashMap());
        return oldPigTrack;
    }



    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        updateDailyOfDelete(deletePigEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        DoctorPigDaily oldDailyPig1 = doctorDailyReportManager.findDoctorPigDaily(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .weanAvgWeight(oldPigEvent.getWeanAvgWeight())
                .weanCountChange(-oldPigEvent.getWeanCount())
                .weanQualifiedCount(EventUtil.minusInt(0, oldPigEvent.getHealthCount()))
                .weanNestChange(-1)
                .weanDayAge(getWeanAvgAge(oldPigEvent.getPigId(), oldPigEvent.getParity(), oldPigEvent.getEventAt()))
                .build();
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig1, changeDto1));

    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigDaily oldDailyPig2 = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), inputDto.eventAt());
        DoctorWeanDto weanDto2 = (DoctorWeanDto) inputDto;
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .weanAvgWeight(weanDto2.getPartWeanAvgWeight())
                .weanCountChange(weanDto2.getPartWeanPigletsCount())
                .weanQualifiedCount(weanDto2.getQualifiedCount())
                .weanNestChange(1)
                .weanDayAge(getWeanAvgAge(newPigEvent.getPigId(), newPigEvent.getParity(), inputDto.eventAt()))
                .build();
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig2, changeDto2));

    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        oldDailyPig.setWeanWeight(EventUtil.plusDouble(oldDailyPig.getWeanWeight(), oldDailyPig.getWeanWeight()));
        oldDailyPig.setWeanNest(EventUtil.plusInt(oldDailyPig.getWeanNest(), changeDto.getWeanNestChange()));
        oldDailyPig.setWeanCount(EventUtil.plusInt(oldDailyPig.getWeanCount(), changeDto.getWeanCountChange()));
        oldDailyPig.setWeanQualifiedCount(EventUtil.plusInt(oldDailyPig.getWeanQualifiedCount(), changeDto.getWeanQualifiedCount()));
        return oldDailyPig;
    }

    public BaseGroupInput buildTriggerGroupEventInput(DoctorPigEvent pigEvent) {
        DoctorWeanDto partWeanDto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorWeanDto.class);
        DoctorWeanGroupInput input = new DoctorWeanGroupInput();
        input.setPartWeanDate(pigEvent.getEventAt());
        input.setPartWeanPigletsCount(partWeanDto.getPartWeanPigletsCount());
        input.setPartWeanAvgWeight(partWeanDto.getPartWeanAvgWeight());
        input.setQualifiedCount(partWeanDto.getQualifiedCount());
        input.setNotQualifiedCount(partWeanDto.getNotQualifiedCount());
        input.setGroupId(pigEvent.getGroupId());
        input.setEventAt(DateUtil.toDateString(pigEvent.getEventAt()));
        input.setEventSource(pigEvent.getEventSource());
        input.setIsAuto(1);
        input.setCreatorId(pigEvent.getCreatorId());
        input.setCreatorName(pigEvent.getCreatorName());
        input.setRelPigEventId(pigEvent.getId());
        return input;
    }

    /**
     * 获取
     * @param pigId 猪id
     * @param parity 胎次
     * @param eventAt 事件事件
     * @return 断奶平均日龄
     */
    public Integer getWeanAvgAge(Long pigId, Integer parity, Date eventAt) {
        DoctorPigEvent farrowEvent = doctorPigEventDao.getFarrowEventByParity(pigId, parity);
        return DateUtil.getDeltaDays(farrowEvent.getEventAt(), eventAt) + 1;
    }
}
