package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.monthly.DoctorMonthlyReportDto;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪场月报表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */

public interface DoctorMonthlyReportReadService {

    /**
     * 根据farmId和统计日期查询猪场月报表
     *
     * @param farmId 猪场id
     * @param sumAt 统计日期 yyyy-MM-dd
     * @return 猪场月报表
     */
    Response<DoctorMonthlyReportDto> findMonthlyReportByFarmIdAndSumAt(@NotNull(message = "farmId.not.null") Long farmId,
                                                                       @NotNull(message = "date.not.null") String sumAt);
}
