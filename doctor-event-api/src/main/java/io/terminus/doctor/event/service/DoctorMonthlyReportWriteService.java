package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorMonthlyReport;

/**
 * Desc: 猪场月报表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */

public interface DoctorMonthlyReportWriteService {

    /**
     * 创建DoctorMonthlyReport
     * @param monthlyReport 猪场月报表实例
     * @return 主键id
     */
    Response<Long> createMonthlyReport(DoctorMonthlyReport monthlyReport);

    /**
     * 更新DoctorMonthlyReport
     * @param monthlyReport 猪场月报表实例
     * @return 是否成功
     */
    Response<Boolean> updateMonthlyReport(DoctorMonthlyReport monthlyReport);

    /**
     * 根据主键id删除DoctorMonthlyReport
     * @param monthlyReportId 猪场月报表实例主键id
     * @return 是否成功
     */
    Response<Boolean> deleteMonthlyReportById(Long monthlyReportId);
}