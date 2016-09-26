package io.terminus.doctor.event.handler.rollback.boar;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by xiao on 16/9/22.
 */
@Component
public class DoctorRollbackBoarChgFarmEventHandler extends DoctorAbstractRollbackPigEventHandler{
    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return Objects.equals(pigEvent.getType(), PigEvent.CHG_FARM.getKey()) && Objects.equals(pigEvent.getKind(), DoctorPigEvent.kind.Boar.getValue());
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        return handleRollbackWithStatus(pigEvent, DoctorRevertLog.Type.BOAR.getValue());
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent) {
        pigEvent.setExtra(pigEvent.getExtra());
        DoctorRollbackDto doctorRollbackDto = DoctorRollbackDto.builder()
                .esBarnId((Long) pigEvent.getExtraMap().get("chgLocationFromBarnId"))
                .esPigId(pigEvent.getPigId())
                .farmId((Long) pigEvent.getExtraMap().get("chgLocationFromFarmId"))
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG, RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT))
                .eventAt(new Date())
                .build();
        DoctorRollbackDto doctorRollbackDto1 = DoctorRollbackDto.builder()
                .esBarnId((Long) pigEvent.getExtraMap().get("chgLocationToBarnId"))
                .farmId((Long) pigEvent.getExtraMap().get("chgLocationToBarnId"))
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT))
                .eventAt(new Date())
                .build();
        return Lists.newArrayList(doctorRollbackDto, doctorRollbackDto1);
    }
}
