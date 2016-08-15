package io.terminus.doctor.event.manager;

import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
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
public class DoctorMonthlyReportManager {

    private final DoctorMonthlyReportDao doctorMonthlyReportDao;

    @Autowired
    public DoctorMonthlyReportManager(DoctorMonthlyReportDao doctorMonthlyReportDao) {
        this.doctorMonthlyReportDao = doctorMonthlyReportDao;
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
}
