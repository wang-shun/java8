package io.terminus.doctor.event.handler.rollback.boar;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by xiao on 16/9/22.
 */
@Component
public class DoctorRollbackBoarDiseaseEventHandler extends DoctorAbstractRollbackPigEventHandler {
    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return Objects.equals(pigEvent.getType(), PigEvent.DISEASE.getKey()) &&
                Objects.equals(pigEvent.getKind(), DoctorPig.PigSex.BOAR.getKey());
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        handleRollbackWithoutSnapshot(pigEvent, operatorId, operatorName);
    }

    @Override
    public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        return null;
    }
}
