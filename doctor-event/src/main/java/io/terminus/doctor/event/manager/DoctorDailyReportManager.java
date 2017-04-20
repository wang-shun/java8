package io.terminus.doctor.event.manager;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */
@Slf4j
@Component
public class DoctorDailyReportManager {

    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorDailyReportCache doctorDailyReportCache;

    @Autowired
    public DoctorDailyReportManager(DoctorDailyReportDao doctorDailyReportDao,
                                    DoctorDailyReportCache doctorDailyReportCache) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorDailyReportCache = doctorDailyReportCache;
    }
    /**
     * 实时计算日报后, 创建之
     * @param farmId 猪场id
     * @param sumAt  统计日期
     */
    @Transactional
    public void realTimeDailyReports(Long farmId, Date sumAt) {
        DoctorDailyReport report = new DoctorDailyReport();
        report.setFarmId(farmId);
        report.setSumAt(DateUtil.toDateString(sumAt));

        DoctorDailyReportDto dto = doctorDailyReportCache.initDailyReportByFarmIdAndDate(farmId, sumAt);
//        report.setSowCount(dto.getSowCount());                      //母猪总存栏
//        report.setFarrowCount(dto.getLiveStock().getFarrow());      //产房仔猪
//        report.setNurseryCount(dto.getLiveStock().getNursery());    //保育猪
//        report.setFattenCount(dto.getLiveStock().getFatten());      //育肥猪
//        report.setHoubeiCount(dto.getLiveStock().getHoubei());      //后备猪
//        report.setReportData(dto);
//        doctorDailyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
//        doctorDailyReportDao.create(report);
    }
}
