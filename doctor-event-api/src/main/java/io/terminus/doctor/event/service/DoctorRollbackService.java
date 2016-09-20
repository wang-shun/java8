package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;

import javax.validation.constraints.NotNull;

/**
 * Desc: 回滚接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */

public interface DoctorRollbackService {

    /**
     * 回滚猪群事件
     * @param eventId 事件id
     * @return 是否成功
     */
    Response<Boolean> rollbackGroupEvent(@NotNull(message = "eventId.not.null") Long eventId);

    /**
     * 回滚猪事件
     * @param eventId 事件id
     * @return 是否成功
     */
    Response<Boolean> rollbackPigEvent(@NotNull(message = "eventId.not.null") Long eventId);
}
