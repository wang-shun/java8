package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportTrendDto;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Desc: 猪场报表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */

public interface DoctorCommonReportReadService {

    /**
     * 根据farmId和统计日期查询猪场月报表和趋势图
     *
     * @param farmId 猪场id
     * @param sumAt  统计日期 yyyy-MM-dd
     * @param index  趋势月份数
     * @return 猪场月报表
     */
    Response<DoctorCommonReportTrendDto> findMonthlyReportTrendByFarmIdAndSumAt(@NotNull(message = "farmId.not.null") Long farmId,
                                                                                @NotNull(message = "date.not.null") String sumAt,
                                                                                @Nullable Integer index);

    /**
     * 根据farmId和统计日期查询猪场周报表和趋势图
     *
     * @param farmId 猪场id
     * @param year   年份，默认今年
     * @param week   统计日期 当年的第几周，默认今天所在周
     * @param index  趋势图
     * @return 猪场周报表
     */
    Response<DoctorCommonReportTrendDto> findWeeklyReportTrendByFarmIdAndSumAt(@NotNull(message = "farmId.not.null") Long farmId,
                                                                               @Nullable Integer year,
                                                                               @Nullable Integer week,
                                                                               @Nullable Integer index);
}
