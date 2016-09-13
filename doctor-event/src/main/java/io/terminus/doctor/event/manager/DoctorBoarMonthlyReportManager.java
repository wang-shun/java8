package io.terminus.doctor.event.manager;

import io.terminus.doctor.event.dao.DoctorBoarMonthlyReportDao;
import io.terminus.doctor.event.model.DoctorBoarMonthlyReport;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 19:47 16/9/12
 */
@Component
public class DoctorBoarMonthlyReportManager {
    private final DoctorBoarMonthlyReportDao doctorBoarMonthlyReportDao;


    @Autowired
    public DoctorBoarMonthlyReportManager(DoctorBoarMonthlyReportDao doctorBoarMonthlyReportDao){
        this.doctorBoarMonthlyReportDao = doctorBoarMonthlyReportDao;
    }

    /**
     * 批量创建
     * @param reports
     * @param sumAt
     */
    @Transactional
    public void createMonthlyReports(List<DoctorBoarMonthlyReport> reports, String sumAt) {
        doctorBoarMonthlyReportDao.deleteBySumAt(sumAt);
        doctorBoarMonthlyReportDao.creates(reports);
    }


    @Transactional
    public void createMonthlyReport(Long farmId, DoctorBoarMonthlyReport boarMonthlyReport, String sumAt) {
        doctorBoarMonthlyReportDao.deleteByFarmIdAndSumAt(farmId, sumAt);
        doctorBoarMonthlyReportDao.create(boarMonthlyReport);
    }



}
