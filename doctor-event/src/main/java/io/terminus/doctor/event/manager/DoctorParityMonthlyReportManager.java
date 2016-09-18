package io.terminus.doctor.event.manager;

import io.terminus.doctor.event.dao.DoctorParityMonthlyReportDao;
import io.terminus.doctor.event.model.DoctorParityMonthlyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 13:47 16/9/13
 */
@Component
public class DoctorParityMonthlyReportManager {

    private final DoctorParityMonthlyReportDao doctorParityMonthlyReportDao;

    @Autowired
    public DoctorParityMonthlyReportManager(DoctorParityMonthlyReportDao doctorParityMonthlyReportDao){
        this.doctorParityMonthlyReportDao = doctorParityMonthlyReportDao;
    }

    /**
     * 删除sumAt数据, 再批量创建
     * @param monthlyReports 月报
     * @param sumAt 统计日期
     */
    @Transactional
    public void createMonthlyReports(List<DoctorParityMonthlyReport> monthlyReports, Date sumAt) {
        doctorParityMonthlyReportDao.deleteBySumAt(sumAt);
        doctorParityMonthlyReportDao.creates(monthlyReports);
    }
}
