package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.model.DoctorGroupEvent;

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
     * @return true 需要更新report, false 不需要
     */
    boolean rollback(DoctorGroupEvent groupEvent);

    /**
     * 更新报表
     */
    void updateReport(DoctorGroupEvent groupEvent);
}
