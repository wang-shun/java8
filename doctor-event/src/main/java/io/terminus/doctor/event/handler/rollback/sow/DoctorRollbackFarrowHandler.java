package io.terminus.doctor.event.handler.rollback.sow;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desc: 母猪分娩回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
@Component
public class DoctorRollbackFarrowHandler extends DoctorAbstractRollbackPigEventHandler {

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return false;
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorPigEvent pigEvent) {
        return null;
    }

    @Override
    protected DoctorRollbackDto handleReport(DoctorPigEvent pigEvent) {
        return null;
    }

}
