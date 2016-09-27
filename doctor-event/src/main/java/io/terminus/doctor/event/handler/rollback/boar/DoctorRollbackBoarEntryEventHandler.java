package io.terminus.doctor.event.handler.rollback.boar;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by xiao on 16/9/22.
 */
@Component
public class DoctorRollbackBoarEntryEventHandler extends DoctorAbstractRollbackPigEventHandler{

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return Objects.equals(pigEvent.getType(), PigEvent.ENTRY.getKey()) && Objects.equals(pigEvent.getKind(), DoctorPigEvent.kind.Boar.getValue());
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        DoctorPigSnapshot snapshot = doctorPigSnapshotDao.queryByEventId(pigEvent.getId());
        DoctorPigSnapShotInfo info = JSON_MAPPER.fromJson(snapshot.getPigInfo(), DoctorPigSnapShotInfo.class);
        doctorPigEventDao.delete(pigEvent.getId());
        doctorPigTrackDao.delete(info.getPigTrack().getId());
        doctorPigDao.delete(info.getPig().getId());
        doctorPigSnapshotDao.delete(snapshot.getId());
        createDoctorRevertLog(pigEvent, operatorId, operatorName);
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent) {
        DoctorRollbackDto doctorRollbackDto = DoctorRollbackDto.builder()
                .esBarnId(pigEvent.getBarnId())
                .esPigId(pigEvent.getPigId())
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG, RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT))
                .farmId(pigEvent.getFarmId())
                .eventAt(pigEvent.getEventAt())
                .build();
        return Lists.newArrayList(doctorRollbackDto);
    }
}
