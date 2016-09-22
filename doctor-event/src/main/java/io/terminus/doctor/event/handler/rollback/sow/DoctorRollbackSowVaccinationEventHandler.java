package io.terminus.doctor.event.handler.rollback.sow;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;

import java.util.List;

/**
 * Created by xiao on 16/9/22.
 */
public class DoctorRollbackSowVaccinationEventHandler extends DoctorAbstractRollbackPigEventHandler {
    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return false;
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorPigEvent pigEvent) {
        return handleRollbackWithoutStatus(pigEvent, DoctorRevertLog.Type.SOW.getValue());
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent) {
        return null;
    }
}
