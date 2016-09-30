package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by xiao on 16/9/22.
 */
@Component
public class DoctorRollbackSowEntryEventHandler extends DoctorAbstractRollbackPigEventHandler{

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {

        return Objects.equals(pigEvent.getType(), PigEvent.ENTRY.getKey()) &&
                Objects.equals(pigEvent.getKind(), DoctorPig.PIG_TYPE.SOW.getKey()) &&
                isLastEvent(pigEvent);
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        DoctorPigSnapshot snapshot = doctorPigSnapshotDao.queryByEventId(pigEvent.getId());
        DoctorPigSnapShotInfo info = JSON_MAPPER.fromJson(snapshot.getPigInfo(), DoctorPigSnapShotInfo.class);
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigEvent.getPigId());
        DoctorPig doctorPig = doctorPigDao.findById(pigEvent.getPigId());
        doctorPigEventDao.delete(pigEvent.getId());
        doctorPigDao.delete(info.getPig().getId());
        doctorPigTrackDao.delete(info.getPigTrack().getId());
        doctorPigSnapshotDao.delete(snapshot.getId());
        createDoctorRevertLog(pigEvent, doctorPigTrack, doctorPig, operatorId, operatorName);
        workFlowRollback(pigEvent);
    }

    @Override
        public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        DoctorRollbackDto doctorRollbackDto = DoctorRollbackDto.builder()
                .esBarnId(pigEvent.getBarnId())
                .esPigId(pigEvent.getPigId())
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG, RollbackType.SEARCH_PIG_DELETE,
                        RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT))
                .farmId(pigEvent.getFarmId())
                .eventAt(pigEvent.getEventAt())
                .build();
        return Lists.newArrayList(doctorRollbackDto);
    }
}
