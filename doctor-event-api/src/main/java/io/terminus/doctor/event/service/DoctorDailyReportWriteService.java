package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorDailyReport;

/**
 * Desc: 猪场日报表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */

public interface DoctorDailyReportWriteService {

    /**
     * 创建DoctorDailyReport
     * @param dailyReport 猪场日报表实例
     * @return 主键id
     */
    Response<Long> createDailyReport(DoctorDailyReport dailyReport);

    /**
     * 更新DoctorDailyReport
     * @param dailyReport 猪场日报表实例
     * @return 是否成功
     */
    Response<Boolean> updateDailyReport(DoctorDailyReport dailyReport);

    /**
     * 根据主键id删除DoctorDailyReport
     * @param dailyReportId 猪场日报表实例主键id
     * @return 是否成功
     */
    Response<Boolean> deleteDailyReportById(Long dailyReportId);
}