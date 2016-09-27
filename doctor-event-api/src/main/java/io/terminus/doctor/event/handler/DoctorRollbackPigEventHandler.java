package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.model.DoctorPigEvent;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪事件回滚操作
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */

public interface DoctorRollbackPigEventHandler {

    /**
     * 判断是否可以回滚
     * @param pigEvent 猪事件
     * @return true 可以, false 不可以
     */
    boolean canRollback(DoctorPigEvent pigEvent);

    /**
     * 回滚操作
     */
    void rollback(DoctorPigEvent pigEvent, @NotNull(message = "userId.not.null") Long operatorId, String operatorName);

    /**
     * 更新报表
     */
    void updateReport(DoctorPigEvent pigEvent);
}
