package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupChangeHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 母猪的仔猪变动事件回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/9
 */
@Component
public class DoctorRollbackSowPigletChangeEventHandler extends DoctorAbstractRollbackPigEventHandler {

    @Autowired
    private DoctorRollbackGroupChangeHandler doctorRollbackGroupChangeHandler;

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        if (!Objects.equals(pigEvent.getType(), PigEvent.FARROWING.getKey()) || !isLastEvent(pigEvent)) {
            return false;
        }

        //母猪仔猪变动会触发猪群变动事件，校验猪群变动事件是否是最新事件
        DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelPigEventId(pigEvent.getId());
        return isRelLastGroupEvent(toGroupEvent);
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        //1. 回滚猪群变动
        DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelPigEventId(pigEvent.getId());
        doctorRollbackGroupChangeHandler.rollback(toGroupEvent, operatorId, operatorName);

        //2. 回滚母猪仔猪变动
        handleRollbackWithoutStatus(pigEvent, operatorId, operatorName);
    }

    @Override
    public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        DoctorRollbackDto dto = new DoctorRollbackDto();
        dto.setOrgId(pigEvent.getOrgId());
        dto.setFarmId(pigEvent.getFarmId());
        dto.setEventAt(pigEvent.getEventAt());
        dto.setEsBarnId(pigEvent.getBarnId());

        DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(pigEvent.getPigId());
        dto.setEsGroupId(pigTrack.getGroupId());
        dto.setRollbackTypes(Lists.newArrayList(
                RollbackType.DAILY_LIVESTOCK,
                RollbackType.DAILY_DEAD,
                RollbackType.DAILY_SALE,
                RollbackType.MONTHLY_REPORT,
                RollbackType.GROUP_BATCH,
                RollbackType.SEARCH_BARN,
                RollbackType.SEARCH_GROUP
        ));
        return Lists.newArrayList(dto);
    }
}
