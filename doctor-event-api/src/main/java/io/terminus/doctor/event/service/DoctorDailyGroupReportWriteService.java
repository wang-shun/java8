package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;

import javax.validation.constraints.NotNull;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/20
 */

public interface DoctorDailyGroupReportWriteService {

    /**
     * 根据事件id更新猪群日报缓存
     * @param eventId 事件id
     * @return 是否成功
     */
    Response<Boolean> updateDailyGroupReportCache(@NotNull(message = "eventId.not.null") Long eventId);
}
