package io.terminus.doctor.event.handler.rollback.boar;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;

import java.util.List;
import java.util.Objects;

/**
 * Created by xiao on 16/9/22.
 */
public class DoctorRollbackBoarDiseaseEventHandler extends DoctorAbstractRollbackPigEventHandler {
    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return Objects.equals(pigEvent.getType(), PigEvent.DISEASE.getKey());
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorPigEvent pigEvent) {
        return handleRollbackWithoutStatus(pigEvent, DoctorRevertLog.Type.BOAR.getValue());
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent) {
        return null;
    }
}
