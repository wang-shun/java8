package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by xiao on 16/9/22.
 */
@Component
public class DoctorRollbackSowMatingEventHandler extends DoctorAbstractRollbackPigEventHandler {
    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {

        return Objects.equals(pigEvent.getType(), PigEvent.MATING.getKey());
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        return handleRollbackWithStatus(pigEvent, DoctorRevertLog.Type.SOW.getValue());
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent) {
        DoctorRollbackDto doctorRollbackDto = DoctorRollbackDto.builder()
                .esPigId(pigEvent.getPigId())
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_PIG, RollbackType.DAILY_MATE, RollbackType.MONTHLY_REPORT))
                .farmId(pigEvent.getFarmId())
                .eventAt(pigEvent.getEventAt())
                .build();
        return Lists.newArrayList(doctorRollbackDto);
    }
}
