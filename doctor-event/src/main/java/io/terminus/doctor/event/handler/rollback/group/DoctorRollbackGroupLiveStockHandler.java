package io.terminus.doctor.event.handler.rollback.group;

import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 猪只存栏回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/22
 */
@Slf4j
@Component
public class DoctorRollbackGroupLiveStockHandler extends DoctorAbstractRollbackGroupEventHandler {
    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        //允许猪只存栏事件回滚
        return Objects.equals(groupEvent.getType(), GroupEventType.LIVE_STOCK.getValue()) && isLastEvent(groupEvent);
    }

    @Override
    protected void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        log.info("this is a livestock event:{}", groupEvent);
        sampleRollback(groupEvent, operatorId, operatorName);
    }

    @Override
    public List<DoctorRollbackDto> updateReport(DoctorGroupEvent groupEvent) {
        return null;    //不需要更新统计
    }
}
