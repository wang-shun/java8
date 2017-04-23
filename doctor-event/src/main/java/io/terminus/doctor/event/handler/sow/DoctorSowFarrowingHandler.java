package io.terminus.doctor.event.handler.sow;

import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigFarrowEventHandler;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.handler.group.DoctorCommonGroupEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.common.utils.DateUtil.stringToDate;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪分娩事件
 */
@Component
@Slf4j
public class DoctorSowFarrowingHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;
    @Autowired
    private DoctorModifyPigFarrowEventHandler doctorModifyPigFarrowEventHandler;

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        expectTrue(Objects.equals(fromTrack.getStatus(), PigStatus.Farrow.getKey()), "pig.status.failed", PigEvent.from(executeEvent.getType()).getName(), PigStatus.from(fromTrack.getStatus()).getName());
    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        expectTrue(notNull(doctorPigTrack), "pig.track.not.null", inputDto.getPigId());
        DoctorFarrowingDto farrowingDto = (DoctorFarrowingDto) inputDto;

        farrowingDto.setNestCode(generateNestCode(doctorPigTrack.getFarmId(), new DateTime(doctorPigEvent.getEventAt())));

        Map<String, Object> extra = doctorPigEvent.getExtraMap();

        //分娩时间
        DateTime farrowingDate = new DateTime(farrowingDto.eventAt());
        doctorPigEvent.setFarrowingDate(farrowingDate.toDate());
        //查找最近一次初配种事件
        DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
        expectTrue(notNull(firstMate), "first.mate.not.null", doctorPigTrack.getPigId());
        DateTime pregJudgeDate = new DateTime(stringToDate(expectNotNull(firstMate.getExtraMap().get("judgePregDate"), "judge.preg.date.not.null").toString()));
        DateTime mattingDate = new DateTime(firstMate.getEventAt());

        //计算孕期
        doctorPigEvent.setPregDays(Math.abs(Days.daysBetween(farrowingDate, mattingDate).getDays()));

        //分娩窝重
        doctorPigEvent.setFarrowWeight(farrowingDto.getBirthNestAvg());

        //分娩仔猪只数信息
        doctorPigEvent.setLiveCount(CountUtil.getIntegerDefault0(farrowingDto.getFarrowingLiveCount()));
        doctorPigEvent.setHealthCount(CountUtil.getIntegerDefault0(farrowingDto.getHealthCount()));
        doctorPigEvent.setWeakCount(CountUtil.getIntegerDefault0(farrowingDto.getWeakCount()));
        doctorPigEvent.setMnyCount(CountUtil.getIntegerDefault0(farrowingDto.getMnyCount()));
        doctorPigEvent.setJxCount(CountUtil.getIntegerDefault0(farrowingDto.getJxCount()));
        doctorPigEvent.setDeadCount(CountUtil.getIntegerDefault0(farrowingDto.getDeadCount()));
        doctorPigEvent.setBlackCount(CountUtil.getIntegerDefault0(farrowingDto.getBlackCount()));

//        if (farrowingDate.isBefore(pregJudgeDate)) {
//            extra.put("farrowingType", FarrowingType.EARLY.getKey());
//        } else {
//            extra.put("farrowingType", FarrowingType.USUAL.getKey());
//        }
        doctorPigEvent.setExtraMap(extra);
        return doctorPigEvent;
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        Map<String, Object> extra = isNull(toTrack.getExtraMap()) ? Maps.newHashMap() : toTrack.getExtraMap();
        // 对应的 仔猪 猪舍的 信息
        extra.put("toBarnId", toTrack.getCurrentBarnId());
        extra.put("toBarnName", toTrack.getCurrentBarnName());
        //Long pigEventId = (Long) context.get("doctorPigEventId");

        //分娩时记录下 分娩数量
        toTrack.setFarrowQty(executeEvent.getLiveCount());
        toTrack.setUnweanQty(executeEvent.getLiveCount());
        toTrack.setWeanQty(0);  //分娩时 断奶数为0
        toTrack.setFarrowAvgWeight(executeEvent.getFarrowWeight());
        toTrack.setWeanAvgWeight(0D); //分娩时, 断奶均重置成0

        toTrack.setExtraMap(extra);
        toTrack.setStatus(PigStatus.FEED.getKey());  //母猪进入哺乳的状态
        toTrack.setGroupId(executeEvent.getGroupId());
        return toTrack;
    }

    @Override
    protected void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack) {
        super.specialHandle(doctorPigEvent, doctorPigTrack);
        //对应的最近一次的 周期配种的初陪 的 isDelivery 字段变成true
        DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
        expectTrue(notNull(firstMate), "first.mate.not.null", doctorPigTrack.getPigId());
        firstMate.setIsDelivery(1);
        doctorPigEventDao.update(firstMate);
    }

    @Override
    protected void updateDailyForNew(DoctorPigEvent newPigEvent) {
        BasePigEventInputDto inputDto = JSON_MAPPER.fromJson(newPigEvent.getExtra(), DoctorFarrowingDto.class);
        doctorModifyPigFarrowEventHandler.updateDailyOfNew(newPigEvent, inputDto);
    }

    @Override
    public void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack) {
        //触发猪群事件
        DoctorSowMoveInGroupInput input = (DoctorSowMoveInGroupInput) buildTriggerGroupEventInput(doctorPigEvent);
        Long groupId = doctorCommonGroupEventHandler.sowGroupEventMoveIn(doctorEventInfoList, input);

        expectTrue(notNull(groupId), "farrow.group.not.null");
        doctorPigTrack.setGroupId(groupId);
        doctorPigTrackDao.update(doctorPigTrack);

        //向分娩事件事件中放入groupId
        Map<String, Object> extraMap = doctorPigEvent.getExtraMap();
        extraMap.put("farrowGroupId", doctorPigTrack.getGroupId());
        doctorPigEvent.setGroupId(doctorPigTrack.getGroupId());
        doctorPigEvent.setExtraMap(extraMap);
        doctorPigEventDao.update(doctorPigEvent);
    }

    /**
     * 创建对应的猪群
     *
     */
    @Override
    public BaseGroupInput buildTriggerGroupEventInput(DoctorPigEvent doctorPigEvent) {
        return doctorModifyPigFarrowEventHandler.buildTriggerGroupEventInput(doctorPigEvent);
    }

    /**
     * 生成窝号
     * @param farmId 猪场id
     * @param eventAt 事件时间
     * @return 窝号
     */
    private String generateNestCode(Long farmId, DateTime eventAt) {
        Long farrowingCount =  doctorPigEventDao.countPigEventTypeDuration(
                farmId,
                PigEvent.FARROWING.getKey(),
                eventAt.withDayOfMonth(1).withTimeAtStartOfDay().toDate(),
                eventAt.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().toDate());
        return eventAt.toString(DateTimeFormat.forPattern("yyyyMM")) + "-" + farrowingCount;
    }
}
