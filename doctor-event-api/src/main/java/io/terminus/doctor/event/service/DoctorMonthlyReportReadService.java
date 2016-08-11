package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorMonthlyReport;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Desc: 猪场月报表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */

public interface DoctorMonthlyReportReadService {

    /**
     * 根据id查询猪场月报表
     * @param monthlyReportId 主键id
     * @return 猪场月报表
     */
    Response<DoctorMonthlyReport> findMonthlyReportById(@NotNull(message = "monthlyReportId.not.null") Long monthlyReportId);

    /**
     * 根据farmId查询猪场月报表
     * @param farmId 猪场id
     * @return 猪场月报表
     */
    Response<List<DoctorMonthlyReport>> findMonthlyReportsByFarmId(@NotNull(message = "farmId.not.null") Long farmId);
}
