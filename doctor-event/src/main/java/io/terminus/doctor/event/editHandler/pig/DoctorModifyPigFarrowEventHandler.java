package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupMoveInEventHandler;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.sow.DoctorSowFarrowingHandler;
import io.terminus.doctor.event.model.DoctorDailyPig;
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
                .farmId(oldPigEvent.getFarmId())
                .businessId(oldPigEvent.getPigId())
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
    protected void updateDaily(DoctorPigEvent oldEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyPig oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
        } else {
            //更新新时间的日记录
            DoctorDailyPig oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            DoctorFarrowingDto farrowingDto1 = (DoctorFarrowingDto) inputDto;
            DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                    .liveCountChange(farrowingDto1.getFarrowingLiveCount())
                    .healthCountChange(farrowingDto1.getHealthCount())
                    .weakCountChange(farrowingDto1.getWeakCount())
                    .blackCountChange(farrowingDto1.getBlackCount())
                    .deadCountChange(farrowingDto1.getDeadCount())
                    .jxCountChange(farrowingDto1.getJxCount())
                    .mnyCountChange(farrowingDto1.getMnyCount())
                    .farrowWeightChange(farrowingDto1.getBirthNestAvg())
                    .build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));

            //更新原时间的日记录
            DoctorDailyPig oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
            DoctorFarrowingDto farrowingDto2 = JSON_MAPPER.fromJson(oldEvent.getExtra(), DoctorFarrowingDto.class);
            DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                    .liveCountChange(EventUtil.minusInt(0, farrowingDto2.getFarrowingLiveCount()))
                    .healthCountChange(EventUtil.minusInt(0, farrowingDto2.getHealthCount()))
                    .weakCountChange(EventUtil.minusInt(0, farrowingDto2.getWeakCount()))
                    .blackCountChange(EventUtil.minusInt(0, farrowingDto2.getBlackCount()))
                    .deadCountChange(EventUtil.minusInt(0, farrowingDto2.getDeadCount()))
                    .jxCountChange(EventUtil.minusInt(0, farrowingDto2.getJxCount()))
                    .mnyCountChange(EventUtil.minusInt(0, farrowingDto2.getMnyCount()))
                    .farrowWeightChange(EventUtil.minusDouble(0D, farrowingDto2.getBirthNestAvg()))
                    .build();
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig2, changeDto2));
        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        DoctorGroupEvent oldGroupEvent = doctorGroupEventDao.findByRelPigEventId(newPigEvent.getId());
        doctorModifyMoveInEventHandler.modifyHandle(oldGroupEvent, doctorSowFarrowingHandler.buildTriggerGroupEventInput(newPigEvent));
    }

    @Override
    protected DoctorDailyPig buildDailyPig(DoctorDailyPig oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig.setFarrowLive(EventUtil.plusInt(oldDailyPig.getFarrowLive(), changeDto.getLiveCountChange()));
        oldDailyPig.setFarrowHealth(EventUtil.plusInt(oldDailyPig.getFarrowHealth(), changeDto.getHealthCountChange()));
        oldDailyPig.setFarrowWeak(EventUtil.plusInt(oldDailyPig.getFarrowWeak(), changeDto.getWeakCountChange()));
        oldDailyPig.setFarrowBlack(EventUtil.plusInt(oldDailyPig.getFarrowBlack(), changeDto.getBlackCountChange()));
        oldDailyPig.setFarrowDead(EventUtil.plusInt(oldDailyPig.getFarrowDead(), changeDto.getDeadCountChange()));
        oldDailyPig.setFarrowJx(EventUtil.plusInt(oldDailyPig.getFarrowJx(), changeDto.getJxCountChange()));
        oldDailyPig.setFarrowMny(EventUtil.plusInt(oldDailyPig.getFarrowMny(), changeDto.getMnyCountChange()));
        oldDailyPig.setFarrowWeight(EventUtil.plusDouble(oldDailyPig.getFarrowWeight(), changeDto.getFarrowWeightChange()));
        oldDailyPig.setFarrowAvgWeight(EventUtil.getAvgWeight(oldDailyPig.getFarrowWeight(), oldDailyPig.getFarrowLive()));
        return oldDailyPig;
    }
}
