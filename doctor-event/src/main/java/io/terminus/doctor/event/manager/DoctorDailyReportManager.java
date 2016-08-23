package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

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
    private final DoctorKpiDao doctorKpiDao;
    private final DoctorDailyReportCache doctorDailyReportCache;

    @Autowired
    public DoctorDailyReportManager(DoctorDailyReportDao doctorDailyReportDao,
                                    DoctorKpiDao doctorKpiDao,
                                    DoctorDailyReportCache doctorDailyReportCache) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorKpiDao = doctorKpiDao;
        this.doctorDailyReportCache = doctorDailyReportCache;
    }

    /**
     * 删除sumAt数据, 再批量创建
     * @param dailyReports 日报
     * @param sumAt 统计日期
     */
    @Transactional
    public void createDailyReports(List<DoctorDailyReportDto> dailyReports, Date sumAt) {
        doctorDailyReportDao.deleteBySumAt(sumAt);
        dailyReports.stream()
                .map(r -> {
                    DoctorDailyReport report = new DoctorDailyReport();
                    report.setFarmId(r.getFarmId());

                    //保存当天的母猪与各阶段的仔猪存栏
                    report.setSowCount(r.getSowCount());
                    DoctorLiveStockDailyReport lv = r.getLiveStock();
                    report.setFarrowCount(lv.getFarrow());
                    report.setNurseryCount(lv.getNursery());
                    report.setFattenCount(lv.getFatten());
                    report.setReportData(r);
                    report.setSumAt(sumAt);
                    return report;
                })
                .forEach(report -> {
                    try {
                        doctorDailyReportDao.create(report);
                    } catch (Exception e) {
                        log.error("create daily report failed, report:{}, cause:{}", report, Throwables.getStackTraceAsString(e));
                    }
                });
    }

    /**
     * 实时计算日报后, 创建之
     * @param farmId 猪场id
     * @param sumAt  统计日期
     */
    @Transactional
    public void realTimeDailyReports(Long farmId, Date sumAt) {
        doctorDailyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);

        DoctorDailyReport report = new DoctorDailyReport();
        report.setFarmId(farmId);
        report.setSumAt(sumAt);

        DoctorDailyReportDto dto = doctorDailyReportCache.initDailyReportByFarmIdAndDate(farmId, sumAt);
        report.setSowCount(dto.getSowCount());                      //母猪总存栏
        report.setFarrowCount(dto.getLiveStock().getFarrow());      //产房仔猪
        report.setNurseryCount(dto.getLiveStock().getNursery());    //保育猪
        report.setFattenCount(dto.getLiveStock().getFatten());      //育肥猪
        report.setReportData(dto);
        doctorDailyReportDao.create(report);
    }
}
