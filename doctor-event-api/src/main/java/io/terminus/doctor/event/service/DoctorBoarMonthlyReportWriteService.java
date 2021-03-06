package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorBoarMonthlyReport;

import java.util.Date;
import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 公猪生产成绩月报写服务
 * Date: 2016-09-12
 */

public interface DoctorBoarMonthlyReportWriteService {

    /**
     * 创建DoctorBoarMonthlyReport
     * @param doctorBoarMonthlyReport
     * @return 主键id
     */
    Response<Long> createDoctorBoarMonthlyReport(DoctorBoarMonthlyReport doctorBoarMonthlyReport);

    /**
     * 更新DoctorBoarMonthlyReport
     * @param doctorBoarMonthlyReport
     * @return 是否成功
     */
    Response<Boolean> updateDoctorBoarMonthlyReport(DoctorBoarMonthlyReport doctorBoarMonthlyReport);

    /**
     * 根据主键id删除DoctorBoarMonthlyReport
     * @param doctorBoarMonthlyReportId
     * @return 是否成功
     */
    Response<Boolean> deleteDoctorBoarMonthlyReportById(Long doctorBoarMonthlyReportId);

    /**
     * 批量创建公猪生产成绩月报
     * @param farmIds
     * @param sumAt
     * @return
     */
    Response<Boolean> createMonthlyReports(List<Long> farmIds, Date sumAt);

    Response<Boolean> createMonthlyReport(Long farmId, Date sumAt);
}