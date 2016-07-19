package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorDailyReport;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪场日报表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */

public interface DoctorDailyReportReadService {

    /**
     * 根据id查询猪场日报表
     * @param dailyReportId 主键id
     * @return 猪场日报表
     */
    Response<DoctorDailyReport> findDailyReportById(@NotNull(message = "dailyReportId.not.null") Long dailyReportId);

    /**
     * 根据farmId和日期查询猪场日报表
     * @param farmId 猪场id
     * @param sumAt  统计日期
     * @return 猪场日报表
     */
    Response<DoctorDailyReport> findDailyReportByFarmIdAndSumAt(@NotNull(message = "farmId.not.null") Long farmId,
                                                                @NotEmpty(message = "sumat.not.empty") String sumAt);
}
