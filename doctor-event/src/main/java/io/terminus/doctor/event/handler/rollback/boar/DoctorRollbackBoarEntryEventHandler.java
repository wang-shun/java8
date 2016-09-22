package io.terminus.doctor.event.handler.rollback.boar;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by xiao on 16/9/22.
 */
public class DoctorRollbackBoarEntryEventHandler extends DoctorAbstractRollbackPigEventHandler{

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return Objects.equals(pigEvent.getType(), PigEvent.ENTRY.getKey());
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorPigEvent pigEvent) {
       return handleRollbackWithStatus(pigEvent, DoctorRevertLog.Type.BOAR.getValue());
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent) {
        DoctorRollbackDto doctorRollbackDto = DoctorRollbackDto.builder()
                .esBarnId(pigEvent.getBarnId())
                .esPigId(pigEvent.getPigId())
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG, RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT))
                .farmId(pigEvent.getFarmId())
                .eventAt(new Date())
                .build();
        return Lists.newArrayList(doctorRollbackDto);
    }
}
