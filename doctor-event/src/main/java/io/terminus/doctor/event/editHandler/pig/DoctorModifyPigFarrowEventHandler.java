package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupMoveInEventHandler;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.sow.DoctorSowFarrowingHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * Created by xjn on 17/4/14.
 * 分娩编辑和回滚
 */
@Component
public class DoctorModifyPigFarrowEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Autowired
    private DoctorModifyGroupMoveInEventHandler doctorModifyMoveInEventHandler;
    @Autowired
    private DoctorSowFarrowingHandler doctorSowFarrowingHandler;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorFarrowingDto newFarrowingDto = (DoctorFarrowingDto) inputDto;
        DoctorFarrowingDto oldFarrowingDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFarrowingDto.class);
        return DoctorEventChangeDto.builder()
                .newEventAt(newFarrowingDto.eventAt())
                .oldEventAt(oldFarrowingDto.eventAt())
                .farrowWeightChange(EventUtil.minusDouble(newFarrowingDto.getBirthNestAvg(), oldFarrowingDto.getBirthNestAvg()))
                .liveCountChange(EventUtil.minusInt(newFarrowingDto.getFarrowingLiveCount(), oldFarrowingDto.getFarrowingLiveCount()))
                .healthCountChange(EventUtil.minusInt(newFarrowingDto.getHealthCount(), oldFarrowingDto.getHealthCount()))
                .weakCountChange(EventUtil.minusInt(newFarrowingDto.getWeakCount(), oldFarrowingDto.getWeakCount()))
                .mnyCountChange(EventUtil.minusInt(newFarrowingDto.getMnyCount(), oldFarrowingDto.getMnyCount()))
                .blackCountChange(EventUtil.minusInt(newFarrowingDto.getBlackCount(), oldFarrowingDto.getBlackCount()))
                .deadCountChange(EventUtil.minusInt(newFarrowingDto.getDeadCount(), oldFarrowingDto.getDeadCount()))
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorFarrowingDto newFarrowingDto = (DoctorFarrowingDto) inputDto;
        newEvent.setFarrowWeight(newFarrowingDto.getBirthNestAvg());
        newEvent.setLiveCount(newFarrowingDto.getFarrowingLiveCount());
        newEvent.setHealthCount(newFarrowingDto.getHealthCount());
        newEvent.setWeakCount(newFarrowingDto.getWeakCount());
        newEvent.setMnyCount(newFarrowingDto.getMnyCount());
        newEvent.setBlackCount(newFarrowingDto.getBlackCount());
        newEvent.setDeadCount(newFarrowingDto.getDeadCount());
        return newEvent;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        if (!Objects.equals(oldPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            return oldPigTrack;
        }
        oldPigTrack.setFarrowQty(EventUtil.plusInt(oldPigTrack.getFarrowQty(), changeDto.getLiveCountChange()));
        oldPigTrack.setUnweanQty(EventUtil.plusInt(oldPigTrack.getUnweanQty(), changeDto.getLiveCountChange()));
        oldPigTrack.setFarrowAvgWeight(EventUtil.plusDouble(oldPigTrack.getFarrowAvgWeight(),
                EventUtil.getAvgWeight(changeDto.getFarrowWeightChange(), changeDto.getLiveCountChange())));
        return oldPigTrack;
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        DoctorGroupEvent oldGroupEvent = doctorGroupEventDao.findByRelPigEventId(newPigEvent.getId());
        doctorModifyMoveInEventHandler.modifyHandle(oldGroupEvent, doctorSowFarrowingHandler.buildTriggerGroupEventInput(newPigEvent));
    }
}
