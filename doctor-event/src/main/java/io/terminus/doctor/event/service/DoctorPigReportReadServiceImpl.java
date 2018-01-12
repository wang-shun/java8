package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorPigReport;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorPigDaily;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/12/13.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorPigReportReadServiceImpl implements DoctorPigReportReadService {

    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    @Override
    public DoctorPigReport farmReport(Long farmId, Date start, ReportTime reportTime) {

        DoctorPigReport doctorPigReport = generate(getPigDaily(Collections.singletonList(farmId), start, reportTime), new SpecialKeyHandle() {
            @Override
            public int generateTransferIn(DoctorPigDaily pigDaily) {

                //转入=后备进场+后备转入+配怀转场转入+产房转场转入
                return pigDaily.getSowPhReserveIn() +
                        pigDaily.getSowPhEntryIn() +
                        pigDaily.getSowPhChgFarmIn() +
                        pigDaily.getSowCfInFarmIn();
            }

        });
        //todo 前期分娩窝数
//            doctorPigReport.setEarlyFarrowNest();

        return doctorPigReport;
    }

    @Override
    public DoctorPigReport companyReport(List<Long> farmIds, Date start, ReportTime reportTime) {

        return generate(getPigDaily(farmIds, start, reportTime), new SpecialKeyHandle() {
            @Override
            public int generateTransferIn(DoctorPigDaily pigDaily) {
                //转场转入无意义
                return pigDaily.getSowPhReserveIn() +
                        pigDaily.getSowPhEntryIn();
            }
        });
    }

    @Override
    public DoctorPigReport orgReport(List<Long> farmIds, Date start, ReportTime reportTime) {
        return generate(getPigDaily(farmIds, start, reportTime), new SpecialKeyHandle() {
            @Override
            public int generateTransferIn(DoctorPigDaily pigDaily) {
                //转场转入无意义
                return pigDaily.getSowPhReserveIn() +
                        pigDaily.getSowPhEntryIn();
            }
        });
    }


    public DateDuration getDuration(Date countDate, ReportTime reportTime) {

        Date start, end;

        if (reportTime == ReportTime.DAY) {
            start = countDate;
            end = Dates.endOfDay(countDate);
        } else if (reportTime == ReportTime.WEEK) {
            start = DateUtil.weekStart(countDate);
            end = DateUtil.weekEnd(countDate);
        } else if (reportTime == ReportTime.MONTH) {
            start = DateUtil.monthStart(countDate);
            end = DateUtil.monthEnd(countDate);
        } else if (reportTime == ReportTime.SEASON) {

            DateTime d = new DateTime(countDate);
            int month = d.getMonthOfYear();

            if (month < 4) {
                start = DateUtil.toDate(d.getYear() + "-01-01");
                end = DateUtil.getMonthEnd(DateTime.parse(d.getYear() + "-03-01")).toDate();
            } else if (month < 7) {
                start = DateUtil.toDate(d.getYear() + "-04-01");
                end = DateUtil.getMonthEnd(DateTime.parse(d.getYear() + "-06-01")).toDate();
            } else if (month < 10) {
                start = DateUtil.toDate(d.getYear() + "-07-01");
                end = DateUtil.getMonthEnd(DateTime.parse(d.getYear() + "-09-01")).toDate();
            } else {
                start = DateUtil.toDate(d.getYear() + "-10-01");
                end = DateUtil.getMonthEnd(DateTime.parse(d.getYear() + "-12-01")).toDate();
            }

        } else {
            DateTime d = new DateTime(countDate);
            int year = d.getYear();

            start = DateUtil.toYYYYMM(year + "-01");
            end = DateUtil.getMonthEnd(new DateTime(year, 12, 1, 0, 0)).toDate();
        }
        return new DateDuration(start, end);
    }

    private PigDailyContext getPigDaily(List<Long> farmIds, Date countDate, ReportTime reportTime) {
        if (farmIds.isEmpty())
            throw new ServiceException("");

        PigDailyContext pigDailyContext = new PigDailyContext();

        DateDuration dateDuration = getDuration(countDate, reportTime);
        Date start = dateDuration.getStart();
        Date end = dateDuration.getEnd();

        if (farmIds.size() == 1) {
            pigDailyContext.setPigDaily(doctorPigDailyDao.countByFarm(farmIds.get(0), start, end));
        } else {
            pigDailyContext.setPigDaily(doctorPigDailyDao.countByOrg(farmIds, start, end));
        }
        pigDailyContext.setFarmIds(farmIds);
        pigDailyContext.setDateDuration(dateDuration);
        return pigDailyContext;
    }


    private DoctorPigReport generate(PigDailyContext pigDailyContext, SpecialKeyHandle specialKeyHandle) {

        DoctorPigDaily pigDaily = pigDailyContext.getPigDaily();

        DoctorPigReport doctorPigReport = new DoctorPigReport();

        BeanUtils.copyProperties(pigDaily, doctorPigReport);

        //母猪区=配怀+产房
        //期初
        Integer sowStart = (pigDaily.getSowCfStart() + pigDaily.getSowPhStart());

        //转入
        Integer sowTransferIn = specialKeyHandle.generateTransferIn(pigDaily);

        //母猪死亡
        Integer sowDead = pigDaily.getSowCfDead() + pigDaily.getSowPhDead();
        //母猪淘汰
        Integer sowWeedOut = pigDaily.getSowCfWeedOut() + pigDaily.getSowPhWeedOut();
        //死淘率=死亡+淘汰/期初+转入
        doctorPigReport.setBasicSowDeadAndWeedOutRate((sowDead + sowWeedOut) / (sowStart + sowTransferIn) * 100);

        //转入到配怀的量+装入到产房的量
        doctorPigReport.setSowOtherIn(pigDaily.getSowCfIn() + pigDaily.getSowPhReserveIn());

        doctorPigReport.setSowEnd(pigDaily.getSowCfEnd() + pigDaily.getSowPhEnd());
        doctorPigReport.setSowDailyQuantity(doctorPigReport.getSowEnd() / 1);

        Date beforeStart = DateUtils.addDays(pigDailyContext.getDateDuration().getStart(), -114);
        Date beforeEnd = DateUtils.addDays(pigDailyContext.getDateDuration().getEnd(), -114);
        DoctorPigDaily earlyPigDaily = doctorPigDailyDao.countByOrg(pigDailyContext.getFarmIds(), beforeStart, beforeEnd);
        doctorPigReport.setEarlyMating(earlyPigDaily == null ? 0 : earlyPigDaily.getMatingCount());


        Date afterStart = DateUtils.addDays(pigDailyContext.getDateDuration().getStart(), 114);
        Date afterEnd = DateUtils.addDays(pigDailyContext.getDateDuration().getEnd(), 114);
        DoctorPigDaily afterPigDaily = doctorPigDailyDao.countByOrg(pigDailyContext.getFarmIds(), afterStart, afterEnd);
        doctorPigReport.setLateFarrowNest(afterPigDaily == null ? 0 : afterPigDaily.getFarrowNest());

        doctorPigReport.setEarlyFarrowRate(pigDaily.getFarrowNest() / doctorPigReport.getEarlyMating() * 100);
        doctorPigReport.setLateFarrowRate(doctorPigReport.getLateFarrowNest() / pigDaily.getMatingCount() * 100);
        //产仔总数=尖子数+弱子数+
        Integer farrowAll = pigDaily.getFarrowHealth() + pigDaily.getFarrowWeak() + pigDaily.getFarrowjmh() + pigDaily.getFarrowDead();
        doctorPigReport.setAvgFarrow(farrowAll / pigDaily.getFarrowNest() * 100);
        //产仔活子数=
        //TODO 活子数 直接从表中取
        Integer farrowLive = 0;
        doctorPigReport.setAvgFarrowLive(farrowLive / pigDaily.getFarrowNest() * 100);
        doctorPigReport.setAvgFarrowHealth(pigDaily.getFarrowHealth() / pigDaily.getFarrowNest() * 100);
        doctorPigReport.setAvgFarrowWeak(pigDaily.getFarrowWeak() / pigDaily.getFarrowNest() * 100);
        doctorPigReport.setAvgWeight(pigDaily.getFarrowWeight() / pigDaily.getFarrowNest() * 100);
        doctorPigReport.setFirstWeight(pigDaily.getFarrowWeight() / farrowLive * 100);


        return doctorPigReport;
    }

    private interface SpecialKeyHandle {
        /**
         * 获取转入数量
         *
         * @param pigDaily
         * @return
         */
        int generateTransferIn(DoctorPigDaily pigDaily);
    }

    @Data
    public class PigDailyContext {

        private DateDuration dateDuration;
        private DoctorPigDaily pigDaily;
        private List<Long> farmIds;
    }




}
