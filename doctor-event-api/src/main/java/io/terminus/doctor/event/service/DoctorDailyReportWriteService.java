package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Desc: 猪场日报表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */

public interface DoctorDailyReportWriteService {

    /**
     * 批量创建DoctorDailyReport(先删除, 再创建)
     * @param dailyReportDtos 日报统计list
     * @param sumAt 统计时间
     * @return 是否成功
     */
    Response<Boolean> createDailyReports(List<DoctorDailyReportDto> dailyReportDtos,
                                         @NotNull(message = "date.not.null") Date sumAt);


    /**
     * 实时计算某猪场某天的日报统计
     * @param farmId 猪场id
     * @param date 日期
     * @return 是否成功
     */
    Response<Boolean> realtimeDailyReport(@NotNull(message = "farmId.not.null") Long farmId,
                                          @NotNull(message = "date.not.null") Date date);
}