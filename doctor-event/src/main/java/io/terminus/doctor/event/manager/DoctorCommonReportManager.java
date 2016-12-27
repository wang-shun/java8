package io.terminus.doctor.event.manager;

import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.dao.DoctorWeeklyReportDao;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import io.terminus.doctor.event.model.DoctorWeeklyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/12
 */
@Component
public class DoctorCommonReportManager {

    private final DoctorMonthlyReportDao doctorMonthlyReportDao;
    private final DoctorWeeklyReportDao doctorWeeklyReportDao;

    @Autowired
    public DoctorCommonReportManager(DoctorMonthlyReportDao doctorMonthlyReportDao,
                                     DoctorWeeklyReportDao doctorWeeklyReportDao) {
        this.doctorMonthlyReportDao = doctorMonthlyReportDao;
        this.doctorWeeklyReportDao = doctorWeeklyReportDao;
    }

    /**
     * 删除sumAt数据, 再批量创建
     * @param monthlyReports 月报
     * @param sumAt 统计日期
     */
    @Transactional
    public void createMonthlyReports(List<DoctorMonthlyReport> monthlyReports, Date sumAt) {
        doctorMonthlyReportDao.deleteBySumAt(sumAt);
        doctorMonthlyReportDao.creates(monthlyReports);
    }

    @Transactional
    public void createMonthlyReport(Long farmId, DoctorMonthlyReport monthlyReport, Date sumAt) {
        doctorMonthlyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
        doctorMonthlyReportDao.create(monthlyReport);
    }

    /**
     * 删除sumAt数据, 再批量创建
     * @param weeklyReport 周报
     * @param sumAt 统计日期
     */
    @Transactional
    public void createWeeklyReports(List<DoctorWeeklyReport> weeklyReport, Date sumAt) {
        doctorWeeklyReportDao.deleteBySumAt(sumAt);
        doctorWeeklyReportDao.creates(weeklyReport);
    }

    @Transactional
    public void createWeeklyReport(Long farmId, DoctorWeeklyReport weeklyReport, Date sumAt) {
        doctorWeeklyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
        doctorWeeklyReportDao.create(weeklyReport);
    }
}
