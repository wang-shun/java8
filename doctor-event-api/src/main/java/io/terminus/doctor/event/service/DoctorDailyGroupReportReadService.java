package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪群相关日报计算接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */

public interface DoctorDailyGroupReportReadService {

    /**
     * 根据事件id猪群日报缓存
     * @param eventId 事件id
     * @return 日报统计
     */
    Response<DoctorDailyReportDto> getGroupDailyReportByEventId(@NotNull(message = "eventId.not.null") Long eventId);
}
