package io.terminus.doctor.event.editHandler.pig;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupMoveInEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
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

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorFarrowingDto newDto = (DoctorFarrowingDto) inputDto;
        DoctorFarrowingDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFarrowingDto.class);
        DoctorEventChangeDto changeDto = buildEventChange(oldDto, newDto);
        changeDto.setFarmId(oldPigEvent.getFarmId());
        changeDto.setBusinessId(oldPigEvent.getPigId());
        return changeDto;
    }

    @Override
    protected DoctorEventChangeDto buildEventChange(BasePigEventInputDto oldInputDto, BasePigEventInputDto newInputDto) {
        DoctorFarrowingDto newDto = (DoctorFarrowingDto) newInputDto;
        DoctorFarrowingDto oldDto = (DoctorFarrowingDto) oldInputDto;
        return DoctorEventChangeDto.builder()
                .newEventAt(newDto.eventAt())
                .oldEventAt(oldDto.eventAt())
                .farrowWeightChange(EventUtil.minusDouble(newDto.getBirthNestAvg(), oldDto.getBirthNestAvg()))
                .liveCountChange(EventUtil.minusInt(newDto.getFarrowingLiveCount(), oldDto.getFarrowingLiveCount()))
                .healthCountChange(EventUtil.minusInt(newDto.getHealthCount(), oldDto.getHealthCount()))
                .weakCountChange(EventUtil.minusInt(newDto.getWeakCount(), oldDto.getWeakCount()))
                .mnyCountChange(EventUtil.minusInt(newDto.getMnyCount(), oldDto.getMnyCount()))
                .blackCountChange(EventUtil.minusInt(newDto.getBlackCount(), oldDto.getBlackCount()))
                .deadCountChange(EventUtil.minusInt(newDto.getDeadCount(), oldDto.getDeadCount()))
                .build();
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = super.buildNewEvent(oldPigEvent, inputDto);
        DoctorFarrowingDto newDto = (DoctorFarrowingDto) inputDto;
        newEvent.setFarrowWeight(newDto.getBirthNestAvg());
        newEvent.setLiveCount(newDto.getFarrowingLiveCount());
        newEvent.setHealthCount(newDto.getHealthCount());
        newEvent.setWeakCount(newDto.getWeakCount());
        newEvent.setMnyCount(newDto.getMnyCount());
        newEvent.setBlackCount(newDto.getBlackCount());
        newEvent.setDeadCount(newDto.getDeadCount());
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
    protected void updateDailyForModify(DoctorPigEvent oldEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));
        } else {
            DoctorFarrowingDto farrowingDto1 = JSON_MAPPER.fromJson(oldEvent.getExtra(), DoctorFarrowingDto.class);
            DoctorEventChangeDto changeDto1 = buildEventChange(farrowingDto1, new DoctorFarrowingDto());
            changeDto1.setFarrowNestChange(-1);
            DoctorDailyReport oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getOldEventAt());
            oldDailyPig1.setFarrowNest(EventUtil.minusInt(oldDailyPig1.getFarrowNest(), 1));
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));


        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {
        // TODO: 17/4/19 新建编辑
        //转入编辑
        DoctorGroupEvent oldGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(newPigEvent.getId(), GroupEventType.MOVE_IN.getValue());
        doctorModifyMoveInEventHandler.modifyHandle(oldGroupEvent, buildTriggerGroupEventInput(newPigEvent));
    }

    @Override
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig.setFarrowNest(EventUtil.plusInt(oldDailyPig.getFarrowNest(), changeDto.getFarrowNestChange()));
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

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        // TODO: 17/4/19 新建回滚
        //转入回滚
        DoctorGroupEvent deleteGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.MOVE_IN.getValue());
        doctorModifyMoveInEventHandler.rollbackHandle(deleteGroupEvent, operatorId, operatorName);
    }

    @Override
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack) {
        oldPigTrack.setStatus(PigStatus.Farrow.getKey());
        oldPigTrack.setFarrowAvgWeight(0D);
        oldPigTrack.setFarrowQty(0);
        oldPigTrack.setUnweanQty(0);
        oldPigTrack.setGroupId(-1L);
        return oldPigTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent) {
        DoctorDailyReport oldDailyPig = doctorDailyPigDao.findByFarmIdAndSumAt(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
        DoctorFarrowingDto oldDto = JSON_MAPPER.fromJson(deletePigEvent.getExtra(), DoctorFarrowingDto.class);
        DoctorEventChangeDto changeDto = buildEventChange(oldDto, new DoctorFarrowingDto());
        changeDto.setFarrowNestChange(-1);
        oldDailyPig = buildDailyPig(oldDailyPig, changeDto);
        doctorDailyPigDao.update(oldDailyPig);
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        DoctorFarrowingDto farrowingDto1 = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFarrowingDto.class);
        DoctorEventChangeDto changeDto1 = buildEventChange(farrowingDto1, new DoctorFarrowingDto());
        changeDto1.setFarrowNestChange(-1);
        DoctorDailyReport oldDailyPig1 = doctorDailyPigDao.findByFarmIdAndSumAt(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig1, changeDto1));
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorFarrowingDto farrowingDto2 = (DoctorFarrowingDto) inputDto;
        DoctorEventChangeDto changeDto2 = buildEventChange(new DoctorFarrowingDto(), farrowingDto2);
        changeDto2.setFarrowNestChange(1);
        DoctorDailyReport oldDailyPig2 = doctorDailyPigDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), farrowingDto2.eventAt());
        doctorDailyPigDao.update(buildDailyPig(oldDailyPig2, changeDto2));

    }

    public DoctorMoveInGroupInput buildTriggerGroupEventInput(DoctorPigEvent pigEvent) {
        DoctorFarrowingDto farrowingDto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorFarrowingDto.class);
        // Build 新建猪群操作方式
        DoctorSowMoveInGroupInput input = new DoctorSowMoveInGroupInput();
        input.setSowCode(pigEvent.getPigCode());
        input.setSowId(pigEvent.getPigId());
        input.setOrgId(pigEvent.getOrgId());
        input.setOrgName(pigEvent.getOrgName());
        input.setFarmId(pigEvent.getFarmId());
        input.setFarmName(pigEvent.getFarmName());
        input.setGroupCode(farrowingDto.getGroupCode());

        input.setFromBarnId(farrowingDto.getBarnId());
        input.setFromBarnName(farrowingDto.getBarnName());
        input.setToBarnId(farrowingDto.getBarnId());
        input.setToBarnName(farrowingDto.getBarnName());
        input.setPigType(PigType.DELIVER_SOW.getValue());
        input.setInType(DoctorMoveInGroupEvent.InType.PIGLET.getValue());
        input.setInTypeName(DoctorMoveInGroupEvent.InType.PIGLET.getDesc());
        input.setSource(PigSource.LOCAL.getKey());

        Integer farrowingLiveCount = MoreObjects.firstNonNull(farrowingDto.getFarrowingLiveCount(), 0);
        Integer sowCount = MoreObjects.firstNonNull(farrowingDto.getLiveSowCount(), 0);
        Integer boarCount = MoreObjects.firstNonNull(farrowingDto.getLiveBoarCount(), 0);
        if (sowCount == 0 && boarCount == 0) sowCount = farrowingLiveCount;

        input.setSex(DoctorGroupTrack.Sex.MIX.getValue());
        input.setQuantity(farrowingLiveCount);
        input.setSowQty(sowCount);
        input.setBoarQty(boarCount);
        input.setAvgDayAge(1);
        input.setAvgWeight(farrowingLiveCount == 0 ? 0d : Double.parseDouble(String.format("%.2f", farrowingDto.getBirthNestAvg() / farrowingLiveCount)));
        input.setEventAt(DateUtil.toDateString(pigEvent.getEventAt()));
        input.setIsAuto(IsOrNot.YES.getValue());
        input.setCreatorId(pigEvent.getCreatorId());
        input.setCreatorName(pigEvent.getCreatorName());

        input.setSowEvent(true);  //设置为分娩转入
        input.setWeakQty(CountUtil.getIntegerDefault0(farrowingDto.getWeakCount()));
        input.setHealthyQty(CountUtil.getIntegerDefault0(farrowingDto.getHealthCount()));

        input.setRelPigEventId(pigEvent.getId());
        return input;
    }
}
