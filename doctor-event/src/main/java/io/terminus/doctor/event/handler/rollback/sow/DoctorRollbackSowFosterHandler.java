package io.terminus.doctor.event.handler.rollback.sow;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupTransHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 拼窝事件回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/26
 */
@Slf4j
@Component
public class DoctorRollbackSowFosterHandler extends DoctorAbstractRollbackPigEventHandler {

    @Autowired
    private DoctorRollbackGroupTransHandler doctorRollbackGroupTransHandler;

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        if (!Objects.equals(pigEvent.getType(), PigEvent.FOSTERS.getKey())) {
            return false;
        }
        //判断母猪被拼窝事件是最新事件 && 判断仔猪转群事件之后的仔猪转入是否是最新事件
        DoctorPigEvent toPigEvent = doctorPigEventDao.findByRelGroupEventId(pigEvent.getId());
        if (toPigEvent != null && Objects.equals(toPigEvent.getType(), PigEvent.FOSTERS_BY.getKey())) {

            //查找由被拼窝触发的转群事件
            DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelPigEventId(toPigEvent.getId());
            return RespHelper.orFalse(doctorPigEventReadService.isLastEvent(toPigEvent.getPigId(), toPigEvent.getId())) &&
                    doctorRollbackGroupTransHandler.handleCheck(toGroupEvent);

        }
        return false;
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        return null;
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent) {
        return null;
    }
}
