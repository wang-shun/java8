package io.terminus.doctor.event.editHandler.pig;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupMoveInEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupNewEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.InType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;


/**
 * Created by xjn on 17/4/14.
 * 分娩编辑和回滚
 */
@Component
public class DoctorModifyPigFarrowEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Autowired
    private DoctorModifyGroupNewEventHandler modifyGroupNewEventHandler;
    @Autowired
    private DoctorModifyGroupMoveInEventHandler modifyGroupMoveInEventHandler;

    @Override
    protected void modifyHandleCheck(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        super.modifyHandleCheck(oldPigEvent, inputDto);
        DoctorFarrowingDto farrowingDto = (DoctorFarrowingDto) inputDto;

        if (!Objects.equals(farrowingDto.getFarrowingLiveCount(), oldPigEvent.getLiveCount())) {
            //当前胎次下有断奶事件,不允许编辑分娩的活仔数
            notHasWean(oldPigEvent);

            //校验哺乳数量不小于零
            DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(oldPigEvent.getPigId());
            Integer unweanQty = EventUtil.plusInt(pigTrack.getUnweanQty(),
                    EventUtil.minusInt(farrowingDto.getFarrowingLiveCount(), oldPigEvent.getLiveCount()));
            expectTrue(unweanQty >= 0, "modify.count.lead.to.unwean.lower.zero");
        }
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorFarrowingDto newDto = (DoctorFarrowingDto) inputDto;
        DoctorFarrowingDto oldDto = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFarrowingDto.class);
        DoctorEventChangeDto changeDto = buildEventChange(oldDto, newDto);
        changeDto.setFarmId(oldPigEvent.getFarmId());
        changeDto.setBusinessId(oldPigEvent.getPigId());
        return changeDto;
    }

    private DoctorEventChangeDto buildEventChange(BasePigEventInputDto oldInputDto, BasePigEventInputDto newInputDto) {
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
                .jxCountChange(EventUtil.minusInt(newDto.getJxCount(), oldDto.getJxCount()))
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
        newEvent.setJxCount(newDto.getJxCount());
        newEvent.setDeadCount(newDto.getDeadCount());
        newEvent.setPregDays(getPregDays(oldPigEvent.getPigId(), oldPigEvent.getParity(), inputDto.eventAt()));
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
        if (DateUtils.isSameDay(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
            DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(changeDto.getFarmId(), changeDto.getNewEventAt());
            doctorDailyPigDao.update(buildDailyPig(oldDailyPig, changeDto));

            //旧版
         //   DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(changeDto.getFarmId(), changeDto.getNewEventAt());
         //   oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto));
        } else {
            updateDailyOfDelete(oldEvent);
            updateDailyOfNew(oldEvent, inputDto);
        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent) {

        //1.新建编辑
        DoctorGroupEvent newCreateEvent = doctorGroupEventDao.findByRelPigEventIdAndType(newPigEvent.getId(), GroupEventType.NEW.getValue());
        if (notNull(newCreateEvent)) {
            BaseGroupInput newInput = JSON_MAPPER.fromJson(newCreateEvent.getExtra(), DoctorNewGroupInput.class);
            newInput.setEventAt(DateUtil.toDateString(newPigEvent.getEventAt()));
            modifyGroupNewEventHandler.modifyHandle(newCreateEvent, newInput);
        }

        //2.转入编辑
        DoctorGroupEvent moveInEvent = doctorGroupEventDao.findByRelPigEventIdAndType(newPigEvent.getId(), GroupEventType.MOVE_IN.getValue());
        modifyGroupMoveInEventHandler.modifyHandle(moveInEvent, buildTriggerGroupEventInput(newPigEvent));
    }

    @Override
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        DoctorGroupEvent moveInEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.MOVE_IN.getValue());
        Boolean isRollback = modifyGroupMoveInEventHandler.rollbackHandleCheck(moveInEvent);

        DoctorGroupEvent newCreateEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.NEW.getValue());
        if (notNull(newCreateEvent)) {
            isRollback &= modifyGroupNewEventHandler.rollbackHandleCheck(newCreateEvent);
        }

        return isRollback;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
        //还原本胎次下的初配事件是否分娩字段
        DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(deletePigEvent.getPigId(),
                doctorPigEventDao.findLastParity(deletePigEvent.getPigId()));
        firstMate.setIsDelivery(IsOrNot.NO.getValue());
        doctorPigEventDao.update(firstMate);

        //1.转入回滚
        DoctorGroupEvent deleteGroupEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.MOVE_IN.getValue());
        modifyGroupMoveInEventHandler.rollbackHandle(deleteGroupEvent, operatorId, operatorName);

        //2.新建回滚
        DoctorGroupEvent newCreateEvent = doctorGroupEventDao.findByRelPigEventIdAndType(deletePigEvent.getId(), GroupEventType.NEW.getValue());
        if (notNull(newCreateEvent)) {
            modifyGroupNewEventHandler.rollbackHandle(newCreateEvent, operatorId, operatorName);
        }
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
        DoctorPigDaily oldDailyPig = doctorDailyReportManager.findDoctorPigDaily(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
        DoctorFarrowingDto oldDto = JSON_MAPPER.fromJson(deletePigEvent.getExtra(), DoctorFarrowingDto.class);
        DoctorEventChangeDto changeDto = buildEventChange(oldDto, new DoctorFarrowingDto());
        changeDto.setFarrowNestChange(-1);
        oldDailyPig = buildDailyPig(oldDailyPig, changeDto);
        doctorDailyReportManager.createOrUpdatePigDaily(oldDailyPig);

        //旧版
       // DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(deletePigEvent.getFarmId(), deletePigEvent.getEventAt());
       // oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto));
    }

    @Override
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {
        DoctorFarrowingDto farrowingDto1 = JSON_MAPPER.fromJson(oldPigEvent.getExtra(), DoctorFarrowingDto.class);
        DoctorEventChangeDto changeDto1 = buildEventChange(farrowingDto1, new DoctorFarrowingDto());
        changeDto1.setFarrowNestChange(-1);
        DoctorPigDaily oldDailyPig1 = doctorDailyReportManager.findDoctorPigDaily(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig1, changeDto1));

        //旧版
       // DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(oldPigEvent.getFarmId(), oldPigEvent.getEventAt());
       // oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto1));
    }

    @Override
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {
        DoctorFarrowingDto farrowingDto2 = (DoctorFarrowingDto) inputDto;
        DoctorEventChangeDto changeDto2 = buildEventChange(new DoctorFarrowingDto(), farrowingDto2);
        changeDto2.setFarrowNestChange(1);
        DoctorPigDaily oldDailyPig2 = doctorDailyReportManager.findDoctorPigDaily(newPigEvent.getFarmId(), farrowingDto2.eventAt());
        doctorDailyReportManager.createOrUpdatePigDaily(buildDailyPig(oldDailyPig2, changeDto2));
        //旧版
      //  DoctorDailyReport oldDailyReport = oldDailyReportDao.findByFarmIdAndSumAt(newPigEvent.getFarmId(), inputDto.eventAt());
      //  oldDailyReportManager.createOrUpdateDailyPig(oldBuildDailyPig(oldDailyReport, changeDto2));
    }

    @Override
    protected DoctorPigDaily buildDailyPig(DoctorPigDaily oldDailyPig, DoctorEventChangeDto changeDto) {
        oldDailyPig = super.buildDailyPig(oldDailyPig, changeDto);
        oldDailyPig.setFarrowNest(EventUtil.plusInt(oldDailyPig.getFarrowNest(), changeDto.getFarrowNestChange()));
        oldDailyPig.setFarrowLive(EventUtil.plusInt(oldDailyPig.getFarrowLive(), changeDto.getLiveCountChange()));
        oldDailyPig.setFarrowHealth(EventUtil.plusInt(oldDailyPig.getFarrowHealth(), changeDto.getHealthCountChange()));
        oldDailyPig.setFarrowWeak(EventUtil.plusInt(oldDailyPig.getFarrowWeak(), changeDto.getWeakCountChange()));
        oldDailyPig.setFarrowDead(EventUtil.plusInt(oldDailyPig.getFarrowDead(), changeDto.getDeadCountChange()));
        oldDailyPig.setFarrowWeight(EventUtil.plusDouble(oldDailyPig.getFarrowWeight(), changeDto.getFarrowWeightChange()));
        oldDailyPig.setFarrowjmh(EventUtil.plusInt(oldDailyPig.getFarrowjmh(),
                MoreObjects.firstNonNull(changeDto.getJxCountChange(),0) + MoreObjects.firstNonNull(changeDto.getMnyCountChange(),0))
                + MoreObjects.firstNonNull(changeDto.getBlackCountChange(), 0));
        return oldDailyPig;
    }

    protected DoctorDailyReport oldBuildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto) {
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
        oldDailyPig.setFarrowSjmh(oldDailyPig.getFarrowDead() + oldDailyPig.getFarrowBlack()
                + oldDailyPig.getFarrowMny() + oldDailyPig.getFarrowJx());
        oldDailyPig.setFarrowAll(oldDailyPig.getFarrowLive() + oldDailyPig.getFarrowSjmh());
        return oldDailyPig;
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
        input.setInType(InType.PIGLET.getValue());
        input.setInTypeName(InType.PIGLET.getDesc());
        input.setSource(PigSource.LOCAL.getKey());

        Integer farrowingLiveCount = MoreObjects.firstNonNull(farrowingDto.getFarrowingLiveCount(), 0);

        input.setSex(DoctorGroupTrack.Sex.MIX.getValue());
        input.setQuantity(farrowingLiveCount);
        input.setSowQty(farrowingDto.getLiveSowCount());
        input.setBoarQty(farrowingDto.getLiveBoarCount());
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
        input.setEventSource(pigEvent.getEventSource());
        return input;
    }

    /**
     * 怀孕天数
     * @param pigId 猪id
     * @param parity 胎次
     * @param eventAt 分娩时间
     * @return 怀孕天数
     */
    public int getPregDays(Long pigId, Integer parity, Date eventAt) {
        DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(pigId, parity);
        return DateUtil.getDeltaDays(firstMate.getEventAt(), eventAt) + 1;
    }
}
