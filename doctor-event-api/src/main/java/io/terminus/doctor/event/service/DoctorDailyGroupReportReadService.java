package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Desc: 猪群相关日报计算接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */

public interface DoctorDailyGroupReportReadService {

    /**
     * 根据日期计算单个猪场猪群相关日报统计
     * @param farmId 猪场id
     * @param date 日期
     * @return 日报统计
     */
    Response<DoctorDailyReportDto> getGroupDailyReportByFarmIdAndDate(@NotNull(message = "farmId.not.null") Long farmId,
                                                                      @NotNull(message = "date.not.null") Date date);
    /**
     * 根据日期计算猪群相关日报统计
     * @param date 日期
     * @return 日报统计list
     */
    Response<List<DoctorDailyReportDto>> getGroupDailyReportsByDate(@NotNull(message = "date.not.null") Date date);
}
