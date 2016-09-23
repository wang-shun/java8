package io.terminus.doctor.event.handler.rollback.group;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 猪群防疫事件回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/22
 */
@Slf4j
@Component
public class DoctorRollbackGroupVaccinHandler extends DoctorAbstractRollbackGroupEventHandler {
    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        //允许猪群防疫事件回滚
        return Objects.equals(groupEvent.getType(), GroupEventType.ANTIEPIDEMIC.getValue());
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        return sampleRollback(groupEvent);
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorGroupEvent groupEvent) {
        return null;    //不需要更新统计
    }
}
