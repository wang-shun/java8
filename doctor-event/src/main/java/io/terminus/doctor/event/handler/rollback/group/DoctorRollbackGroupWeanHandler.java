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
 * Created by xjn on 17/3/15.
 * 猪群断奶事件
 */
@Slf4j
@Component
public class DoctorRollbackGroupWeanHandler extends DoctorAbstractRollbackGroupEventHandler{

    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        return Objects.equals(groupEvent.getType(), GroupEventType.WEAN.getValue()) && isLastEvent(groupEvent);
    }

    @Override
    protected void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        log.info("this is a movein event:{}", groupEvent);
        sampleRollback(groupEvent, operatorId, operatorName);
    }

    @Override
    public List<DoctorRollbackDto> updateReport(DoctorGroupEvent groupEvent) {
        return null;
        // TODO: 17/3/15 断奶回滚报表 
    }
}
