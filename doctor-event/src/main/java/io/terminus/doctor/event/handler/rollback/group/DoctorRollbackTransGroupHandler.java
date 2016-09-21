package io.terminus.doctor.event.handler.rollback.group;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desc: 猪群转群转群回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
@Component
public class DoctorRollbackTransGroupHandler extends DoctorAbstractRollbackGroupEventHandler {

    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        return false;
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorGroupEvent groupEvent) {
        return null;
    }

    @Override
    protected DoctorRollbackDto handleReport(DoctorGroupEvent groupEvent) {
        return null;
    }
}
