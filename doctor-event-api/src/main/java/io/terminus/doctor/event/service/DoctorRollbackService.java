package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorRollbackDto;

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

    /**
     * 更新日报和es
     * @param rollbackDto 回滚携带信息
     * @return 是否成功
     */
    Response<Boolean> rollbackReportAndES(DoctorRollbackDto rollbackDto);
}
