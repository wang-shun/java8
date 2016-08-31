package io.terminus.doctor.event.handler.sow;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dao.redis.DailyReport2UpdateDao;
import io.terminus.doctor.event.dao.redis.DailyReportHistoryDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.report.daily.DoctorCheckPregDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.KongHuaiPregCheckResult;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Execution;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Component
public class DoctorSowPregCheckHandler extends DoctorAbstractEventFlowHandler {

    private final DailyReport2UpdateDao dailyReport2UpdateDao;
    private final DailyReportHistoryDao dailyReportHistoryDao;
    private final DoctorDailyReportDao doctorDailyReportDao;

    @Autowired
    public DoctorSowPregCheckHandler(DoctorPigDao doctorPigDao,
                                     DoctorPigEventDao doctorPigEventDao,
                                     DoctorPigTrackDao doctorPigTrackDao,
                                     DoctorPigSnapshotDao doctorPigSnapshotDao,
                                     DoctorRevertLogDao doctorRevertLogDao,
                                     DoctorBarnDao doctorBarnDao,
                                     DailyReport2UpdateDao dailyReport2UpdateDao,
                                     DailyReportHistoryDao dailyReportHistoryDao,
                                     DoctorDailyReportDao doctorDailyReportDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao, doctorBarnDao);
        this.dailyReport2UpdateDao = dailyReport2UpdateDao;
        this.dailyReportHistoryDao = dailyReportHistoryDao;
        this.doctorDailyReportDao = doctorDailyReportDao;
    }

    @Override
    protected IsOrNot eventCreatePreHandler(Execution execution, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String, Object> extra, Map<String, Object> context) {
        Integer pregCheckResult = (Integer) extra.get("checkResult");
        //妊娠检查结果，从extra中拆出来
        doctorPigEvent.setPregCheckResult(pregCheckResult);

        //校验能否妊娠检查与检查结果是否正确
        checkCanPregCheckResult(doctorPigTrack.getStatus(), pregCheckResult);

        //妊娠检查事件时间
        DateTime checkDate = new DateTime(Long.valueOf(extra.get("checkDate").toString()));
        doctorPigEvent.setCheckDate(checkDate.toDate());

        //查找最近一次配种事件
        DoctorPigEvent lastMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
        if (notNull(lastMate) && !Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())) {
            DateTime mattingDate = new DateTime(Long.valueOf(lastMate.getExtraMap().get("matingDate").toString()));
            if (notNull(mattingDate)) {
                int npd = Math.abs(Days.daysBetween(checkDate, mattingDate).getDays());

                if (Objects.equals(pregCheckResult, PregCheckResult.FANQING.getKey())) {
                    //返情对应的pfNPD
                    doctorPigEvent.setPfnpd(doctorPigEvent.getPfnpd() + npd);
                    doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
                } else if (Objects.equals(pregCheckResult, PregCheckResult.YING.getKey())) {
                    //阴性对应的pyNPD
                    doctorPigEvent.setPynpd(doctorPigEvent.getPynpd() + npd);
                    doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
                } else if (Objects.equals(pregCheckResult, PregCheckResult.LIUCHAN.getKey())) {
                    //流产对应的plNPD
                    doctorPigEvent.setPlnpd(doctorPigEvent.getPlnpd() + npd);
                    doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
                }
            }

        }

        //根据猪类判断, 如果是逆向: 空怀 => 阳性, 需要删掉以前的空怀事件
        if ((Objects.equals(doctorPigTrack.getStatus(), PigStatus.KongHuai.getKey()))) {
            DoctorPigEvent lastPregEvent = doctorPigEventDao.queryLastPregCheck(doctorPigTrack.getPigId());
            if (lastPregEvent == null || !PregCheckResult.KONGHUAI_RESULTS.contains(lastPregEvent.getPregCheckResult())) {
                throw new ServiceException("preg.check.not.allow");
            }

            log.info("remove old preg check event info:{}", lastPregEvent);
            doctorPigEvent.setId(lastPregEvent.getId());    //把id放进去, 用于更新数据
            doctorPigEvent.setRelEventId(lastPregEvent.getRelEventId()); //重新覆盖下relEventId
            updateDailyReport(lastPregEvent.getEventAt(), lastPregEvent.getPregCheckResult(), doctorPigTrack);
            return IsOrNot.YES;
        }

        return IsOrNot.NO;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {


        Integer pregCheckResult = (Integer) extra.get("checkResult");

        //往extra增加一些特殊标志位用来表明配种类型
        if (Objects.equals(pregCheckResult, PregCheckResult.FANQING.getKey())) {
            extra.put("fanqingToMate", true);
            extra.put("pregCheckResult", KongHuaiPregCheckResult.FANQING.getKey());
        } else if (Objects.equals(pregCheckResult, PregCheckResult.YING.getKey())) {
            extra.put("yinToMate", true);
            extra.put("pregCheckResult", KongHuaiPregCheckResult.YING.getKey());
        } else if (Objects.equals(pregCheckResult, PregCheckResult.LIUCHAN.getKey())) {
            extra.put("liuchanToMateCheck", true);
            extra.put("pregCheckResult", KongHuaiPregCheckResult.LIUCHAN.getKey());
        }else if (Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())){
            extra.put("pregCheckResult", PigStatus.Pregnancy.getKey());
        }

        doctorPigTrack.addAllExtraMap(extra);

        if (Objects.equals(pregCheckResult, PregCheckResult.UNSURE.getKey())) {
            // 不修改状态
        } else if (Objects.equals(pregCheckResult, PregCheckResult.YANG.getKey())) {
            // 阳性
            doctorPigTrack.setStatus(PigStatus.Pregnancy.getKey());
        } else {
            // 其余默认 没有怀孕
            doctorPigTrack.setStatus(PigStatus.KongHuai.getKey());
        }
        Map<String, Object> express = execution.getExpression();
        express.put("pregCheckResult", pregCheckResult);
        express.put("currentBarnType", getBarnById(doctorPigTrack.getCurrentBarnId()).getPigType());
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    @Override
    protected void afterEventCreateHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, DoctorPigSnapshot doctorPigSnapshot, Map<String, Object> extra) {
        //如果是阳性
        if (Objects.equals(PigStatus.Pregnancy.getKey(), doctorPigTrack.getStatus())) {
            //对应的最近一次的 周期配种的初陪 的 isImpregnation 字段变成true
            DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
            if (notNull(firstMate)) {
                firstMate.setIsImpregnation(1);
                doctorPigEventDao.update(firstMate);
            }
        }
    }

    //校验能否置成此妊娠检查状态
    private static void checkCanPregCheckResult(Integer pigStatus, Integer checkResult) {
        //已配种状态直接返回
        if (Objects.equals(pigStatus, PigStatus.Mate.getKey())) {
            return;
        }

        //阳性只能到空怀状态
        if (Objects.equals(pigStatus, PigStatus.Pregnancy.getKey())) {
            if (!PregCheckResult.KONGHUAI_RESULTS.contains(checkResult)) {
                throw new ServiceException("preg.check.result.not.allow");
            }
            return;
        }

        //空怀或流程只能到阳性状态
        if (Objects.equals(pigStatus, PigStatus.KongHuai.getKey())) {
            if (!Objects.equals(checkResult, PregCheckResult.YANG.getKey())) {
                throw new ServiceException("preg.check.result.not.allow");
            }
            return;
        }
        //如果不是 已配种, 妊娠检查结果状态, 不允许妊娠检查
        throw new ServiceException("preg.check.not.allow");
    }

    //恶心的办法更新日报妊检统计, 先更新数据库, 再把redis里的删掉, 这样查redis的时候查不到, 就直接查数据库了
    private void updateDailyReport(Date updateAt, Integer checkResult, DoctorPigTrack pigTrack) {
        Date updateStartAt = Dates.startOfDay(updateAt);

        //存一下覆盖掉的日期
        dailyReport2UpdateDao.saveDailyReport2Update(updateStartAt, pigTrack.getFarmId());

        DoctorDailyReport report = doctorDailyReportDao.findByFarmIdAndSumAt(pigTrack.getFarmId(), updateStartAt);
        if (report != null && notEmpty(report.getData())) {
            DoctorDailyReportDto dto = report.getReportData();
            DoctorCheckPregDailyReport preg = dto.getCheckPreg();

            PregCheckResult result = PregCheckResult.from(checkResult);
            checkNotNull(result, "preg.check.result.error");
            switch (result) {
                case YING:
                    preg.setNegative(preg.getNegative() < 0 ? 0 : preg.getNegative() - 1);
                    break;
                case LIUCHAN:
                    preg.setLiuchan(preg.getLiuchan() < 0 ? 0 : preg.getLiuchan() - 1);
                    break;
                case FANQING:
                    preg.setFanqing(preg.getFanqing() < 0 ? 0 : preg.getFanqing() - 1);
                    break;
                default:
                    break;
            }
            dto.setCheckPreg(preg);
            report.setReportData(dto);
            doctorDailyReportDao.update(report);

            //删掉redis中的日报(空怀的那天)
            dailyReportHistoryDao.deleteDailyReport(pigTrack.getFarmId(), updateStartAt);
        }
    }
}
