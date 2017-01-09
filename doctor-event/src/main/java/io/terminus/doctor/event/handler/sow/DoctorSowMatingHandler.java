package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
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
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.notEmpty;
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

    // 默认114 天 预产日期
    public static final Integer MATING_PREG_DAYS = 114;

    @Override
    public void preHandle(BasePigEventInputDto eventDto, DoctorBasicInputInfoDto basic) {
    }


    @Override
    public DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());

        // validate extra 配种日期信息
        DateTime matingDate = new DateTime(inputDto.eventAt());
        Map<String, Object> extra = doctorPigTrack.getExtraMap();
        if (doctorPigTrack.getCurrentMatingCount() == 0) {
            extra.put("judgePregDate", matingDate.plusDays(MATING_PREG_DAYS).toDate());
        }
        if (!isNull(extra) &&
                extra.containsKey("hasWeanToMating")
                && Boolean.valueOf(extra.get("hasWeanToMating").toString())) {
            extra.put("hasWeanToMating", false);
            doctorPigTrack.setCurrentParity(doctorPigTrack.getCurrentParity() + 1);
        }
        if (!isNull(extra) &&
                extra.containsKey("enterToMate")
                && Boolean.valueOf(extra.get("enterToMate").toString())) {
            extra.put("enterToMate", false);
        }
        //重复配种就加次数
        doctorPigTrack.setCurrentMatingCount(doctorPigTrack.getCurrentMatingCount() + 1);
        // 构建母猪配种信息
        doctorPigTrack.setExtraMap(extra);
        doctorPigTrack.setStatus(PigStatus.Mate.getKey());
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    @Override
    public void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        doctorPigEvent.setParity(doctorPigTrack.getCurrentParity());
        doctorPigEvent.setCurrentMatingCount(doctorPigTrack.getCurrentMatingCount());
        super.specialHandle(doctorPigEvent, doctorPigTrack, inputDto, basic);

        DoctorMatingDto matingDto = (DoctorMatingDto) inputDto;
        Long boarId = matingDto.getMatingBoarPigId();
        DoctorPigTrack boarPigTrack = this.doctorPigTrackDao.findByPigId(boarId);
        checkState(!isNull(doctorPigTrack), "createMating.boarPigId.fail");
        Integer currentBoarParity = MoreObjects.firstNonNull(boarPigTrack.getCurrentParity(), 0) + 1;
        boarPigTrack.setCurrentParity(currentBoarParity);
        doctorPigTrackDao.update(boarPigTrack);

    }

    @Override
    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());

        //  校验断奶后, 第一次配种, 增加胎次
        Map<String, Object> trackExtraMap = doctorPigTrack.getExtraMap();
        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("hasWeanToMating")
                && Boolean.valueOf(trackExtraMap.get("hasWeanToMating").toString())) {

            //这里说明是断奶后的第一次配种,这个地方统计 dpNPD （断奶到配种的非生产天数）
            //查询最近一次导致断奶的事件
            DoctorPigEvent lastWean = getLeadToWeanEvent(doctorPigTrack.getPigId());
            //断奶时间
            DateTime partWeanDate = new DateTime(lastWean.getEventAt());

            Integer dpNPD = Math.abs(Days.daysBetween(partWeanDate, new DateTime(doctorPigEvent.getEventAt())).getDays());
            doctorPigEvent.setDpnpd(doctorPigEvent.getDpnpd() + dpNPD);
            doctorPigEvent.setNpd(doctorPigEvent.getNpd() + dpNPD);

        }

        //判断是否是进场到第一次配种事件
        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("enterToMate")
                && Boolean.valueOf(trackExtraMap.get("enterToMate").toString())) {

            //这里说明是进场后的第一次配种,这个地方统计 jpNPD （进场到配种非生产天数）
            //查询最近一次进场事件
            DoctorPigEvent lastEnter = doctorPigEventDao.queryLastEnter(doctorPigTrack.getPigId());
            //进场时间
            DateTime lastEnterTime = new DateTime(lastEnter.getEventAt());

            Integer jpNPD = Math.abs(Days.daysBetween(lastEnterTime, new DateTime(doctorPigEvent.getEventAt())).getDays());
            doctorPigEvent.setDpnpd(doctorPigEvent.getJpnpd() + jpNPD);
            doctorPigEvent.setNpd(doctorPigEvent.getNpd() + jpNPD);
        }

        //设置配种类型
        List<DoctorPigEvent> events = doctorPigEventDao.queryAllEventsByPigId(doctorPigTrack.getPigId());
        DoctorMatingType mateType = getPigMateType(events, doctorPigEvent.getEventAt());
        doctorPigEvent.setDoctorMateType(mateType.getKey());
        return doctorPigEvent;
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
     * 获取导致断奶的事件
     * @return
     */
    private DoctorPigEvent getLeadToWeanEvent(Long pigId){
        List<DoctorPigEvent> tempList = doctorPigEventDao.findByPigId(pigId).stream().
                filter(doctorPigEvent -> (doctorPigEvent.getEventAt() !=null) &&(
                        (!Objects.equals(doctorPigEvent.getPigStatusBefore(), PigStatus.Wean.getKey()) && Objects.equals(doctorPigEvent.getPigStatusAfter(), PigStatus.Wean.getKey()))
                                || Objects.equals(doctorPigEvent.getType(), PigEvent.WEAN.getKey()))
                )
                .collect(Collectors.toList());
        if (!Arguments.isNullOrEmpty(tempList)){
            return tempList.stream().max(Comparator.comparing(DoctorPigEvent::getEventAt)).get();
        }
        throw new ServiceException("get.lead.to.wean.event.failed");
    }
}
