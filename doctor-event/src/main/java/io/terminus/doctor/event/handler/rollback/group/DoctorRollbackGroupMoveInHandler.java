package io.terminus.doctor.event.handler.rollback.group;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 转入猪群回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/22
 */
@Slf4j
@Component
public class DoctorRollbackGroupMoveInHandler extends DoctorAbstractRollbackGroupEventHandler {

    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        //允许转入猪群事件回滚
        return Objects.equals(groupEvent.getType(), GroupEventType.NEW.getValue());
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        return sampleRollback(groupEvent);
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorGroupEvent groupEvent) {
        DoctorRollbackDto dto = new DoctorRollbackDto();
        dto.setOrgId(groupEvent.getOrgId());
        dto.setFarmId(groupEvent.getFarmId());
        dto.setEventAt(groupEvent.getEventAt());
        dto.setEsBarnId(groupEvent.getBarnId());
        dto.setEsGroupId(groupEvent.getGroupId());

        //更新统计：存栏日报，存栏月报，猪舍统计，猪群统计
        List<RollbackType> rollbackTypes = Lists.newArrayList(
                RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT, RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP);

        dto.setRollbackTypes(rollbackTypes);
        return Lists.newArrayList(dto);
    }
}
