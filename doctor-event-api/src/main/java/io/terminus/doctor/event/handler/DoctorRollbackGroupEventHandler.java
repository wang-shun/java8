package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.model.DoctorGroupEvent;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Desc: 猪群回滚handler
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */

public interface DoctorRollbackGroupEventHandler {

    /**
     * 判断是否可以回滚
     * @param groupEvent 猪群事件
     * @return true 可以, false 不可以
     */
    boolean canRollback(DoctorGroupEvent groupEvent);

    /**
     * 回滚操作
     */
    void rollback(DoctorGroupEvent groupEvent, @NotNull(message = "userId.not.null") Long operatorId, String operatorName);

    /**
     * 更新报表
     */
    List<DoctorRollbackDto> updateReport(DoctorGroupEvent groupEvent);
}
