package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Execution;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.notNull;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorSowMatingHandler extends DoctorAbstractEventFlowHandler {

    // 默认114 天 预产日期
    public static final Integer MATING_PREG_DAYS = 114;


    @Autowired
    public DoctorSowMatingHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao, DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao, DoctorRevertLogDao doctorRevertLogDao, DoctorBarnDao doctorBarnDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao, doctorBarnDao);
    }

    @Override
    protected void eventCreatePreHandler(Execution execution, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String, Object> extra, Map<String, Object> context) {
        DateTime mattingDate = new DateTime(Long.valueOf(extra.get("matingDate").toString()));

        doctorPigEvent.setMattingDate(mattingDate.toDate());
    }

    @Override
    public void specialFlowHandler(Execution execution, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        Long boarId = Long.valueOf(extra.get("matingBoarPigId").toString());
        DoctorPigTrack doctorPigTrack = this.doctorPigTrackDao.findByPigId(boarId);
        checkState(!isNull(doctorPigTrack), "createMating.boarPigId.fail");
        Integer currentBoarParity = MoreObjects.firstNonNull(doctorPigTrack.getCurrentParity(), 0) + 1;
        doctorPigTrack.setCurrentParity(currentBoarParity);
        doctorPigTrackDao.update(doctorPigTrack);
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        // validate extra 配种日期信息
        DateTime matingDate = new DateTime(Long.valueOf(extra.get("matingDate").toString()));
        if (doctorPigTrack.getCurrentMatingCount() == 0) {
            extra.put("judgePregDate", matingDate.plusDays(MATING_PREG_DAYS).toDate());
        }
//        DateTime judgePregDate = new DateTime(Long.valueOf(extra.get("judgePregDate").toString()));
//        checkState(Objects.equals(Days.daysBetween(matingDate, judgePregDate).getDays(), 114), "input.judgePregDate.error");

        //  校验断奶后, 第一次配种, 增加胎次
        Map<String, Object> trackExtraMap = doctorPigTrack.getExtraMap();
        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("hasWeanToMating")
                && Boolean.valueOf(trackExtraMap.get("hasWeanToMating").toString())) {

            extra.put("hasWeanToMating", false);
            doctorPigTrack.setCurrentParity(doctorPigTrack.getCurrentParity() + 1);


            //这里说明是断奶后的第一次配种,这个地方统计 dpNPD （断奶到配种的非生产天数）
            //查询最近一次断奶事件
            DoctorPigEvent lastWean = doctorPigEventDao.queryLastWean(doctorPigTrack.getPigId());
            //断奶时间
            DateTime partWeanDate = new DateTime(Long.valueOf(lastWean.getExtraMap().get("partWeanDate").toString()));

            Integer dpNPD = Math.abs(Days.daysBetween(partWeanDate, matingDate).getDays());

            context.put("dpNPD", dpNPD);

        }

        //判断是否是进场到第一次配种事件
        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("enterToMate")
                && Boolean.valueOf(trackExtraMap.get("enterToMate").toString())) {

            extra.put("enterToMate", false);
            //这里说明是进场后的第一次配种,这个地方统计 jpNPD （进场到配种非生产天数）
            //查询最近一次进场事件
            DoctorPigEvent lastEnter = doctorPigEventDao.queryLastEnter(doctorPigTrack.getPigId());
            //进场时间
            DateTime lastEnterTime = new DateTime(lastEnter.getEventAt());

            Integer jpNPD = Math.abs(Days.daysBetween(lastEnterTime, matingDate).getDays());

            context.put("jpNPD", jpNPD);

            context.put("HP", true);

        }

        //判断是否 流产到配种(妊娠检查)

        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("liuchanToMateCheck")
                && Boolean.valueOf(trackExtraMap.get("liuchanToMateCheck").toString())) {
            extra.put("liuchanToMateCheck", false);

            context.put("LPC", true);
        }

        //判断是否 阴性到配种

        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("yinToMate")
                && Boolean.valueOf(trackExtraMap.get("yinToMate").toString())) {
            extra.put("yinToMate", false);

            context.put("YP", true);
        }

        //判断是否 返情到配种
        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("fanqingToMate")
                && Boolean.valueOf(trackExtraMap.get("fanqingToMate").toString())) {
            extra.put("fanqingToMate", false);

            context.put("FP", true);
        }

        //判断是否 流产到配种(流产事件)
        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("liuchanToMateLiuchan")
                && Boolean.valueOf(trackExtraMap.get("liuchanToMateLiuchan").toString())) {
            extra.put("liuchanToMateLiuchan", false);

            context.put("LPL", true);
        }

        //判断是否 断奶到配种
        if (!isNull(trackExtraMap) &&
                trackExtraMap.containsKey("weanToMate")
                && Boolean.valueOf(trackExtraMap.get("weanToMate").toString())) {
            extra.put("weanToMate", false);

            context.put("DP", true);
        }


        // 构建母猪配种信息
        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.setStatus(PigStatus.Mate.getKey());
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    @Override
    protected void eventCreateAfterHandler(Execution execution, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String, Object> extra, Map<String, Object> context) {
        //更新配种事件当中的配种次数
        doctorPigEvent.setCurrentMatingCount(doctorPigTrack.getCurrentMatingCount());

        if (notNull(context.get("dpNPD"))) {
            Integer dpNPD = (Integer) context.get("dpNPD");
            doctorPigEvent.setDpnpd(doctorPigEvent.getDpnpd() + dpNPD);
            doctorPigEvent.setNpd(doctorPigEvent.getNpd() + dpNPD);
        }

        if (notNull(context.get("jpNPD"))) {
            Integer jpNPD = (Integer) context.get("jpNPD");
            doctorPigEvent.setDpnpd(doctorPigEvent.getJpnpd() + jpNPD);
            doctorPigEvent.setNpd(doctorPigEvent.getNpd() + jpNPD);
        }

        if (notNull(context.get("HP"))) {
            doctorPigEvent.setDoctorMateType(DoctorMatingType.HP.getKey());
        }
        if (notNull(context.get("LPC"))) {
            doctorPigEvent.setDoctorMateType(DoctorMatingType.LPC.getKey());
        }
        if (notNull(context.get("LPL"))) {
            doctorPigEvent.setDoctorMateType(DoctorMatingType.LPL.getKey());
        }
        if (notNull(context.get("DP"))) {
            doctorPigEvent.setDoctorMateType(DoctorMatingType.DP.getKey());
        }
        if (notNull(context.get("YP"))) {
            doctorPigEvent.setDoctorMateType(DoctorMatingType.YP.getKey());
        }
        if (notNull(context.get("FP"))) {
            doctorPigEvent.setDoctorMateType(DoctorMatingType.FP.getKey());
        }

    }
}
