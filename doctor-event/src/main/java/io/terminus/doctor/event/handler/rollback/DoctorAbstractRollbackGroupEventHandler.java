package io.terminus.doctor.event.handler.rollback;

import io.terminus.doctor.event.handler.DoctorRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc: 猪群事件回滚处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
public abstract class DoctorAbstractRollbackGroupEventHandler implements DoctorRollbackGroupEventHandler {

    @Override
    public boolean canRollback(DoctorGroupEvent groupEvent) {
        return false;
    }

    @Override
    public boolean rollback(DoctorGroupEvent groupEvent) {
        return false;
    }

    @Override
    public void updateReport(DoctorGroupEvent groupEvent) {

    }
}
