package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorRollbackDto;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Desc: 回滚接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */

public interface DoctorRollbackService {

    /**
     * 回滚猪群事件
     * @param eventId      事件id
     * @param operatorId   回滚人id
     * @param operatorName 回滚人name
     * @return 是否成功
     */
    RespWithEx<Boolean> rollbackGroupEvent(@NotNull(message = "eventId.not.null") Long eventId,
                                           @NotNull(message = "userId.not.null") Long operatorId,
                                           String operatorName);

    /**
     * 回滚猪事件
     * @param eventId      事件id
     * @param operatorId   回滚人id
     * @param operatorName 回滚人name
     * @return 是否成功
     */
    RespWithEx<Boolean> rollbackPigEvent(@NotNull(message = "eventId.not.null") Long eventId,
                                       @NotNull(message = "userId.not.null") Long operatorId,
                                       String operatorName);

    /**
     * 更新日报和es
     * @param rollbackDtos 回滚携带信息
     * @return 是否成功
     */
    Response<Boolean> rollbackReportAndES(List<DoctorRollbackDto> rollbackDtos);
}
