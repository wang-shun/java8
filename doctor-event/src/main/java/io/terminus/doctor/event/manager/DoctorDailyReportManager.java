package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.DoctorLiveStockDailyReport;
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

    @Autowired
    public DoctorDailyReportManager(DoctorDailyReportDao doctorDailyReportDao) {
        this.doctorDailyReportDao = doctorDailyReportDao;
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
}
