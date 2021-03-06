package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigMatingEventHandler;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorSowMatingHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorModifyPigMatingEventHandler doctorModifyPigMatingEventHandler;

    // 默认114 天 预产日期
    public static final Integer MATING_PREG_DAYS = 114;

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        expectTrue(Objects.equals(fromTrack.getStatus(), PigStatus.Entry.getKey())
                        || Objects.equals(fromTrack.getStatus(), PigStatus.KongHuai.getKey())
                        || Objects.equals(fromTrack.getStatus(), PigStatus.Wean.getKey())
                        || Objects.equals(fromTrack.getStatus(), PigStatus.Mate.getKey())
                , "pig.status.failed", PigEvent.from(executeEvent.getType()).getName()
                , PigStatus.from(fromTrack.getStatus()).getName());
        expectTrue(PigType.MATING_TYPES.contains(fromTrack.getCurrentBarnType())
                , "current.barn.type.not.mate", PigType.from(fromTrack.getCurrentBarnType()).getDesc());
        expectTrue(fromTrack.getCurrentMatingCount() < 3, "mate.count.over");
        expectTrue(notNull(executeEvent.getOperatorId()), "mating.operator.not.null");

        if (fromTrack.getCurrentMatingCount() > 0
                && !Objects.equals(executeEvent.getEventSource(), SourceType.MOVE.getValue())) {
            Integer parity = doctorPigEventDao.findLastParity(fromTrack.getPigId());
            doctorModifyPigMatingEventHandler.serialMateValid(fromTrack.getPigId()
                    , parity, executeEvent.getEventAt());
        }
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        // validate extra 配种日期信息
        DateTime matingDate = new DateTime(executeEvent.getEventAt());
        Map<String, Object> extra = toTrack.getExtraMap();
        if (toTrack.getCurrentMatingCount() == 0) {
            extra.put("judgePregDate", matingDate.plusDays(MATING_PREG_DAYS).toDate());
        }
        if (!isNull(extra) &&
                extra.containsKey("hasWeanToMating")
                && Boolean.valueOf(extra.get("hasWeanToMating").toString())) {
            extra.put("hasWeanToMating", false);
//            toTrack.setCurrentParity(toTrack.getCurrentParity() + 1);
        }

        toTrack.setCurrentParity(doctorEventBaseHelper.getCurrentParity(fromTrack.getPigId()));

        if (!isNull(extra) &&
                extra.containsKey("enterToMate")
                && Boolean.valueOf(extra.get("enterToMate").toString())) {
            extra.put("enterToMate", false);
        }
        //重复配种就加次数
        toTrack.setCurrentMatingCount(toTrack.getCurrentMatingCount() + 1);
        // 构建母猪配种信息
        toTrack.setExtraMap(extra);
        toTrack.setStatus(PigStatus.Mate.getKey());
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return toTrack;
    }

    @Override
    public void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack) {
        doctorPigEvent.setCurrentMatingCount(doctorPigTrack.getCurrentMatingCount());
        super.specialHandle(doctorPigEvent, doctorPigTrack);

        Long boarId = JSON_MAPPER.fromJson(doctorPigEvent.getExtra(), DoctorMatingDto.class).getMatingBoarPigId();
        if (Objects.equals(doctorPigEvent.getSource(), SourceType.INPUT.getValue())) {
            DoctorPigTrack boarPigTrack = this.doctorPigTrackDao.findByPigId(boarId);
            expectTrue(notNull(boarPigTrack), "boar.track.not.null", boarId);
            Integer currentBoarParity = MoreObjects.firstNonNull(boarPigTrack.getCurrentParity(), 0) + 1;
            boarPigTrack.setCurrentParity(currentBoarParity);
            doctorPigTrackDao.update(boarPigTrack);
        }
    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        DoctorMatingDto matingDto = (DoctorMatingDto) inputDto;
        matingDto.setJudgePregDate(getJudgePregDate(inputDto.getPigId(), inputDto.eventAt(), doctorPigTrack.getCurrentMatingCount()));
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        doctorPigEvent.setJudgePregDate(matingDto.getJudgePregDate());
        doctorPigEvent.setMateType(matingDto.getMatingType());
        doctorPigEvent.setBoarCode(matingDto.getMatingBoarPigCode());
        //  校验断奶后, 第一次配种, 增加胎次
        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Wean.getKey())) {

            //这里说明是断奶后的第一次配种,这个地方统计 dpNPD （断奶到配种的非生产天数）
            DateTime partWeanDate;

            //查询最近一次导致断奶的事件
            DoctorPigEvent lastWean = doctorPigEventDao.queryLastWean(doctorPigTrack.getPigId());
            expectTrue(notNull(lastWean), "mating.last.wean.not.null", doctorPigTrack.getPigId());
            partWeanDate = new DateTime(lastWean.getEventAt());

            Integer dpNPD = Math.abs(Days.daysBetween(partWeanDate, new DateTime(doctorPigEvent.getEventAt())).getDays());
            doctorPigEvent.setDpnpd(doctorPigEvent.getDpnpd() + dpNPD);
            doctorPigEvent.setNpd(doctorPigEvent.getNpd() + dpNPD);

            //断奶到配种胎次加1
            doctorPigEvent.setParity(doctorPigEvent.getParity() + 1);

        }

        //判断是否是进场到第一次配种事件
        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Entry.getKey())) {

            //这里说明是进场后的第一次配种,这个地方统计 jpNPD （进场到配种非生产天数）
            //查询最近一次进场事件
            DoctorPigEvent lastEnter = doctorPigEventDao.queryLastEnter(doctorPigTrack.getPigId());
            expectTrue(notNull(lastEnter), "mating.last.enter.not.null", doctorPigTrack.getPigId());
            DateTime lastEnterTime = new DateTime(lastEnter.getEventAt());

            Integer jpNPD = Math.abs(Days.daysBetween(lastEnterTime, new DateTime(doctorPigEvent.getEventAt())).getDays());
            doctorPigEvent.setJpnpd(doctorPigEvent.getJpnpd() + jpNPD);
            doctorPigEvent.setNpd(doctorPigEvent.getNpd() + jpNPD);
        }

        //设置配种类型
        List<DoctorPigEvent> events = doctorPigEventDao.queryAllEventsByPigId(doctorPigTrack.getPigId());
        DoctorMatingType mateType = getPigMateType(events, doctorPigEvent.getEventAt());
        doctorPigEvent.setDoctorMateType(mateType.getKey());
        return doctorPigEvent;
    }

    @Override
    protected void updateDailyForNew(DoctorPigEvent newPigEvent) {
        BasePigEventInputDto inputDto = JSON_MAPPER.fromJson(newPigEvent.getExtra(), DoctorMatingDto.class);
        doctorModifyPigMatingEventHandler.updateDailyOfNew(newPigEvent, inputDto);
    }

    //找出 maxDate(此事件配种日期) 之前的第一个妊娠检查事件, 根据妊检结果判定此次配种类型
    public static DoctorMatingType getPigMateType(List<DoctorPigEvent> events, Date maxDate) {
        events = events.stream()
                .filter(e -> Objects.equals(e.getType(), PigEvent.PREG_CHECK.getKey()) && !e.getEventAt().after(maxDate))
                .sorted((a, b) -> b.getEventAt().compareTo(a.getEventAt()))
                .collect(Collectors.toList());

        //如果前面没有妊检, 说明是第一次配种, 配后备
        if (!notEmpty(events)) {
            return DoctorMatingType.HP;
        }

        //阳性或者其他 => 配断奶
        DoctorPigEvent event = events.get(0);
        if (Objects.equals(event.getPregCheckResult(), PregCheckResult.FANQING.getKey())) {
            return DoctorMatingType.FP;
        }
        if (Objects.equals(event.getPregCheckResult(), PregCheckResult.YING.getKey())) {
            return DoctorMatingType.YP;
        }
        if (Objects.equals(event.getPregCheckResult(), PregCheckResult.LIUCHAN.getKey())) {
            return DoctorMatingType.LPC;
        }
        return DoctorMatingType.DP;
    }

    /**
     * 获取最新胎次下预产期
     *
     * @param pigId      猪id
     * @param matingDate 配种日期
     * @return 预产期
     */
    private Date getJudgePregDate(Long pigId, Date matingDate, Integer currentMatingCount) {
        if (currentMatingCount == 0) {
            return new DateTime(matingDate).plusDays(MATING_PREG_DAYS).toDate();
        }
        int parity = doctorPigEventDao.findLastParity(pigId);
        DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(pigId, parity);
        return firstMate.getJudgePregDate();
    }
}
