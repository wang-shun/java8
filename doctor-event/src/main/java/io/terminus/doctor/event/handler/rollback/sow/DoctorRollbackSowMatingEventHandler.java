package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

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
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        handleRollbackWithStatus(pigEvent, operatorId, operatorName);
        DoctorMatingDto dto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorMatingDto.class);
        Long boarId = dto.getMatingBoarPigId();
        if (isNull(boarId)) {
            DoctorPig pig = doctorPigDao.findPigByFarmIdAndPigCodeAndSex(pigEvent.getFarmId(), pigEvent.getBoarCode(), DoctorPig.PigSex.BOAR.getKey());
            if(!isNull(pig)){
                boarId = pig.getId();
            }
        }
        if(!isNull(boarId)){
            DoctorPigTrack boarTrack = doctorPigTrackDao.findByPigId(boarId);

            if (boarTrack.getCurrentParity() > 0){
                boarTrack.setCurrentParity(boarTrack.getCurrentParity() -1);
                doctorPigTrackDao.update(boarTrack);
            }
        }
    }

    @Override
        public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        DoctorRollbackDto doctorRollbackDto = DoctorRollbackDto.builder()
                .esPigId(pigEvent.getPigId())
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_PIG, RollbackType.DAILY_MATE))
                .farmId(pigEvent.getFarmId())
                .orgId(pigEvent.getOrgId())
                .eventAt(pigEvent.getEventAt())
                .build();
        return Lists.newArrayList(doctorRollbackDto);
    }
}
