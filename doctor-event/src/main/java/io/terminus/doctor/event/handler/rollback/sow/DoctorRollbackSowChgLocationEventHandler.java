package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupTransHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by xiao on 16/9/22.
 */
@Component
public class DoctorRollbackSowChgLocationEventHandler extends DoctorAbstractRollbackPigEventHandler {
    @Autowired private DoctorBarnDao doctorBarnDao;
    @Autowired private DoctorRollbackGroupTransHandler doctorRollbackGroupTransHandler;
    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return Objects.equals(pigEvent.getType(), PigEvent.CHG_LOCATION.getKey()) && Objects.equals(pigEvent.getKind(), DoctorPig.PIG_TYPE.SOW.getKey());
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        DoctorChgLocationDto dto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorChgLocationDto.class);
        DoctorBarn toBarn = doctorBarnDao.findById(dto.getChgLocationToBarnId());
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigEvent.getPigId());
        DoctorGroupEvent relGroupEvent = doctorGroupEventDao.findByRelPigEventId(pigEvent.getId());
        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()) && Objects.equals(toBarn.getPigType(), PigType.DELIVER_SOW.getValue()) && doctorRollbackGroupTransHandler.handleCheck(relGroupEvent)){
            doctorRollbackGroupTransHandler.handleRollback(relGroupEvent, operatorId, operatorName);
        }
        handleRollbackWithoutStatus(pigEvent, operatorId, operatorName);
    }

    @Override
    public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        DoctorChgLocationDto dto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorChgLocationDto.class);
        DoctorRollbackDto doctorRollbackDto = DoctorRollbackDto.builder()
                .esBarnId(dto.getChgLocationFromBarnId())
                .esPigId(pigEvent.getPigId())
                .farmId(pigEvent.getFarmId())
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG, RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT))
                .eventAt(pigEvent.getEventAt())
                .build();
        DoctorRollbackDto doctorRollbackDto1 = DoctorRollbackDto.builder()
                .esBarnId(dto.getChgLocationToBarnId())
                .farmId(pigEvent.getFarmId())
                .rollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN))
                .eventAt(pigEvent.getEventAt())
                .build();
        return Lists.newArrayList(doctorRollbackDto, doctorRollbackDto1);
    }
}
