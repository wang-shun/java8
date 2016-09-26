package io.terminus.doctor.event.handler.rollback.group;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.RollbackType;
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

    @Autowired private DoctorRollbackGroupMoveInHandler doctorRollbackGroupMoveInHandler;

    @Override
    public boolean handleCheck(DoctorGroupEvent groupEvent) {
        //猪群转群会触发目标猪群的转入猪群事件，所以需要校验目标猪群的转入猪群是否是最新事件
        if (!Objects.equals(groupEvent.getType(), GroupEventType.TRANS_GROUP.getValue())) {
            return false;
        }
        DoctorTransGroupEvent event = JSON_MAPPER.fromJson(groupEvent.getExtra(), DoctorTransGroupEvent.class);

        //如果新建猪群，还要校验新建猪群之后的事件
        DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelGroupEventId(groupEvent.getId());
        Long groupEventId = toGroupEvent.getId();
        if (Objects.equals(toGroupEvent.getType(), GroupEventType.NEW.getValue())) {
            DoctorGroupEvent totoGroupEvent = doctorGroupEventDao.findByRelGroupEventId(toGroupEvent.getId());
            groupEventId = totoGroupEvent.getId();
        }
        return RespHelper.orFalse(doctorGroupReadService.isLastEvent(event.getToGroupId(), groupEventId));
    }

    @Override
    public DoctorRevertLog handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelGroupEventId(groupEvent.getId());

        //先回滚转入猪群事件， 再回滚转群事件
        doctorRollbackGroupMoveInHandler.rollback(toGroupEvent, operatorId, operatorName);
        return sampleRollback(groupEvent);
    }

    @Override
    public List<DoctorRollbackDto> handleReport(DoctorGroupEvent groupEvent) {
        DoctorRollbackDto fromDto = new DoctorRollbackDto();
        fromDto.setOrgId(groupEvent.getOrgId());
        fromDto.setFarmId(groupEvent.getFarmId());
        fromDto.setEventAt(groupEvent.getEventAt());
        fromDto.setEsBarnId(groupEvent.getBarnId());
        fromDto.setEsGroupId(groupEvent.getGroupId());

        //更新统计：存栏日报，存栏月报，猪舍统计，猪群统计
        fromDto.setRollbackTypes(Lists.newArrayList(RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT,
                RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP));

        DoctorTransGroupEvent trans = JSON_MAPPER.fromJson(groupEvent.getExtra(), DoctorTransGroupEvent.class);
        DoctorRollbackDto toDto = new DoctorRollbackDto();
        toDto.setOrgId(groupEvent.getOrgId());
        toDto.setFarmId(groupEvent.getFarmId());
        toDto.setEventAt(groupEvent.getEventAt());
        toDto.setEsBarnId(trans.getToBarnId());
        toDto.setEsGroupId(trans.getToGroupId());

        //更新统计：猪舍统计，猪群统计
        toDto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP));

        return Lists.newArrayList(fromDto, toDto);
    }
}
