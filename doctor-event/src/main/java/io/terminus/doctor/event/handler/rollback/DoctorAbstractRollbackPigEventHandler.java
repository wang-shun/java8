package io.terminus.doctor.event.handler.rollback;

import io.terminus.doctor.event.handler.DoctorRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
public class DoctorAbstractRollbackPigEventHandler implements DoctorRollbackPigEventHandler {

    @Override
    public boolean canRollback(DoctorPigEvent pigEvent) {
        return false;
    }

    @Override
    public boolean rollback(DoctorPigEvent pigEvent) {
        return false;
    }

    @Override
    public void updateReport(DoctorPigEvent pigEvent) {

    }
}
