package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Desc: 猪场报表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */

public interface DoctorCommonReportWriteService {

    /**
     * 创建DoctorMonthlyReport
     * @param farmId 猪场id
     * @param sumAt 结算日期(天初)
     * @return
     */
    Response<Boolean> createMonthlyReport(@NotNull(message = "farmId.not.null") Long farmId,
                                          @NotNull(message = "date.not.null") Date sumAt);

    /**
     * 创建DoctorWeeklyReport
     * @param farmId 猪场id
     * @param sumAt 结算日期(天初)
     * @return
     */
    Response<Boolean> createWeeklyReport(@NotNull(message = "farmId.not.null") Long farmId,
                                         @NotNull(message = "date.not.null") Date sumAt);

    /**
     * 更新四个月的各种率
     * @param farmId 猪场id
     * @param date   日期
     * @return 是否成功
     */
    Response<Boolean> update4MonthReports(@NotNull(message = "farmId.not.null") Long farmId,
                                          @NotNull(message = "date.not.null") Date date);
}