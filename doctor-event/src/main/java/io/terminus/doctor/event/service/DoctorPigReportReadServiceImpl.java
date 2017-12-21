package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorPigReport;
import io.terminus.doctor.event.model.DoctorPigDaily;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.font.SunFontManager;

import java.time.LocalDate;
import java.util.Calendar;
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


        DoctorPigDaily pigDaily = getPigDaily(Collections.singletonList(farmId), start, reportTime);

        DoctorPigReport doctorPigReport = generate(pigDaily);

        //前推114天配种数量
        Date before = DateUtils.addDays(start, -114);
        DoctorPigDaily beforePigDaily = doctorPigDailyDao.countByFarm(pigDaily.getFarmId(), before, before);
        doctorPigReport.setEarlyMating(null == beforePigDaily ? 0 : beforePigDaily.getMatingCount());

        //todo 前期分娩窝数
//            doctorPigReport.setEarlyFarrowNest();
        doctorPigReport.setEarlyFarrowRate(pigDaily.getFarrowNest() / doctorPigReport.getEarlyMating() * 100);
        Date after = DateUtils.addDays(start, 114);
        DoctorPigDaily afterPigDaily = doctorPigDailyDao.countByFarm(farmId, after, after);
        doctorPigReport.setLateFarrowNest(null == afterPigDaily ? 0 : afterPigDaily.getFarrowNest());

        return doctorPigReport;
    }

    @Override
    public DoctorPigReport companyReport(Long orgId, Date start, ReportTime reportTime) {

        //TODO 转场转入无意义
        return null;
    }

    @Override
    public DoctorPigReport orgReport(Long orgId, Date start, ReportTime reportTime) {
        return null;
    }

    private DoctorPigDaily getPigDaily(List<Long> farmIds, Date countDate, ReportTime reportTime) {
        if (farmIds.isEmpty())
            throw new ServiceException("");

        Date start, end;

        if (reportTime == ReportTime.DAY) {
            start = end = countDate;
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


        if (farmIds.size() == 1) {
            return doctorPigDailyDao.countByFarm(farmIds.get(0), start, end);
        } else {
            return doctorPigDailyDao.countByOrg(farmIds, start, end);
        }
    }


    private DoctorPigReport generate(DoctorPigDaily pigDaily) {

        DoctorPigReport doctorPigReport = new DoctorPigReport();

        BeanUtils.copyProperties(pigDaily, doctorPigReport);

        //母猪区=配怀+产房
        //期初
        Integer sowStart = (pigDaily.getSowCfStart() + pigDaily.getSowPhStart());
        //转入=后备进场+后备转入+配怀转场转入+产房转场转入
        Integer sowTransferIn = pigDaily.getSowPhReserveIn() + pigDaily.getSowPhEntryIn() + pigDaily.getSowPhChgFarmIn() + pigDaily.getSowCfInFarmIn();
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


        doctorPigReport.setLateFarrowRate(doctorPigReport.getLateFarrowNest() / pigDaily.getMatingCount() * 100);
        //产仔总数=尖子数+弱子数+
        Integer farrowAll = pigDaily.getFarrowHealth() + pigDaily.getFarrowWeak() + pigDaily.getFarrowjmh() + pigDaily.getFarrowDead();
        doctorPigReport.setAvgFarrow(farrowAll / pigDaily.getFarrowNest() * 100);
        //产仔活子数=
        //TODO 活子数
        Integer farrowLive = 0;
        doctorPigReport.setAvgFarrowLive(farrowLive / pigDaily.getFarrowNest() * 100);
        doctorPigReport.setAvgFarrowHealth(pigDaily.getFarrowHealth() / pigDaily.getFarrowNest() * 100);
        doctorPigReport.setAvgFarrowWeak(pigDaily.getFarrowWeak() / pigDaily.getFarrowNest() * 100);
        doctorPigReport.setAvgWeight(pigDaily.getFarrowWeight() / pigDaily.getFarrowNest() * 100);
        doctorPigReport.setFirstWeight(pigDaily.getFarrowWeight() / farrowLive * 100);


        return doctorPigReport;
    }

}
