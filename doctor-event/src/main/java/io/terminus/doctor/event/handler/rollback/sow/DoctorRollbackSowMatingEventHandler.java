package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
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
        return Objects.equals(pigEvent.getType(), PigEvent.MATING.getKey()) && isLastEvent(pigEvent);
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        handleRollbackWithStatus(pigEvent, operatorId, operatorName);
        DoctorMatingDto dto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorMatingDto.class);
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(dto.getMatingBoarPigId());
        if (doctorPigTrack.getCurrentParity() > 0){
            doctorPigTrack.setCurrentParity(doctorPigTrack.getCurrentParity() -1);
            doctorPigTrackDao.update(doctorPigTrack);
        }
    }

    @Override
        public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        DoctorRollbackDto doctorRollbackDto = DoctorRollbackDto.builder()
                .esPigId(pigEvent.getPigId())
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_PIG, RollbackType.DAILY_MATE))
                .farmId(pigEvent.getFarmId())
                .eventAt(pigEvent.getEventAt())
                .build();
        return Lists.newArrayList(doctorRollbackDto);
    }
}
