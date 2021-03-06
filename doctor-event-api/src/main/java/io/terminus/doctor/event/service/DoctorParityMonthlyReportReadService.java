package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorParityMonthlyReport;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 胎次产仔分析月报读服务
 * Date: 2016-09-13
 */

public interface DoctorParityMonthlyReportReadService {

    /**
     * 根据id查询胎次产仔分析月报
     * @param doctorParityMonthlyReportId 主键id
     * @return 胎次产仔分析月报
     */
    Response<DoctorParityMonthlyReport> findDoctorParityMonthlyReportById(Long doctorParityMonthlyReportId);
}
