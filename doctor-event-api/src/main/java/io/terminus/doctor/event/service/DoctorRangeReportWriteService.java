package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorRangeReport;

import java.util.Date;
import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 指标月报写服务
 * Date: 2017-04-19
 */

public interface DoctorRangeReportWriteService {

    /**
     * 创建DoctorRangeReport
     * @param doctorRangeReport
     * @return 主键id
     */
    Response<Long> createDoctorRangeReport(DoctorRangeReport doctorRangeReport);

    /**
     * 更新DoctorRangeReport
     * @param doctorRangeReport
     * @return 是否成功
     */
    Response<Boolean> updateDoctorRangeReport(DoctorRangeReport doctorRangeReport);

    /**
     * 根据主键id删除DoctorRangeReport
     * @param doctorRangeReportId
     * @return 是否成功
     */
    Response<Boolean> deleteDoctorRangeReportById(Long doctorRangeReportId);

    Response<Boolean> generateDoctorRangeReports(List<Long> farmIds, Date today);
}