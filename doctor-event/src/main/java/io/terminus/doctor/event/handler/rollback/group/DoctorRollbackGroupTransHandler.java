package io.terminus.doctor.event.handler.rollback.group;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 猪群转群回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
@Component
public class DoctorRollbackGroupTransHandler extends DoctorAbstractRollbackGroupEventHandler {

    @Autowired DoctorRollbackGroupMoveInEventHandler doctorRollbackGroupMoveInEventHandler;

    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        //猪群转群会触发目标猪群的转入猪群事件，所以需要校验目标猪群的转入猪群是否是最新事件
        if (Objects.equals(groupEvent.getType(), GroupEventType.TRANS_GROUP.getValue())) {
            return false;
        }
        DoctorTransGroupEvent event = JSON_MAPPER.fromJson(groupEvent.getExtra(), DoctorTransGroupEvent.class);
        DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelGroupEventId(groupEvent.getId());
        return RespHelper.orFalse(doctorGroupReadService.isLastEvent(event.getToGroupId(), toGroupEvent.getId()));

    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorGroupEvent groupEvent) {
        return null;
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorGroupEvent groupEvent) {
        return null;
    }
}
