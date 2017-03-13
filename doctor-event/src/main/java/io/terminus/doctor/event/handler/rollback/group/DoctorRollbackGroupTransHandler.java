package io.terminus.doctor.event.handler.rollback.group;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorEventRelation;
import io.terminus.doctor.event.model.DoctorGroupEvent;
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
    @Autowired private DoctorRollbackGroupNewHandler doctorRollbackGroupNewHandler;
    @Autowired private DoctorRollbackGroupCloseHandler doctorRollbackGroupCloseHandler;

    @Override
    public boolean handleCheck(DoctorGroupEvent groupEvent) {
        //猪群转群会触发目标猪群的转入猪群事件，所以需要校验目标猪群的转入猪群是否是最新事件
        if (!Objects.equals(groupEvent.getType(), GroupEventType.TRANS_GROUP.getValue())) {
            return false;
        }

        //判断事件链的最后一个事件，是否是最新事件
        if (!isRelLastEvent(groupEvent)) {
            return false;
        }

        //如果触发关闭猪群事件，说明此事件肯定不是最新事件
        Long eventId = doctorEventRelationDao.findByOriginAndType(groupEvent.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
        DoctorGroupEvent close = doctorGroupEventDao.findById(eventId);
        if (isCloseEvent(close)) {
            return doctorRollbackGroupCloseHandler.handleCheck(close);
        }
        return isLastEvent(groupEvent);
    }

    @Override
    public void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        log.info("this is a trans event:{}", groupEvent);
        Long eventId = doctorEventRelationDao.findByOriginAndType(groupEvent.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
        DoctorGroupEvent to1 = doctorGroupEventDao.findById(eventId);

        //如果关闭猪群
        if (isCloseEvent(to1)) {
            Long to2Id = doctorEventRelationDao.findByOriginAndType(to1.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
            DoctorGroupEvent to2 = doctorGroupEventDao.findById(to2Id);

            rollbackMoveIn(to2, operatorId, operatorName);
            doctorRollbackGroupCloseHandler.rollback(to1, operatorId, operatorName);    //回滚关闭猪群
        }
        else {
            rollbackMoveIn(to1, operatorId, operatorName);
        }
        sampleRollback(groupEvent, operatorId, operatorName);
    }

    //如果有新建猪群, 就需要先回滚新建猪群事件
    private void rollbackMoveIn(DoctorGroupEvent to2, Long operatorId, String operatorName) {
        if (Objects.equals(to2.getType(), GroupEventType.NEW.getValue())) {
            Long toGroupEventId = doctorEventRelationDao.findByOriginAndType(to2.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
            DoctorGroupEvent moveIn = doctorGroupEventDao.findById(toGroupEventId);

            doctorRollbackGroupMoveInHandler.rollback(moveIn, operatorId, operatorName);
            doctorRollbackGroupNewHandler.rollback(to2, operatorId, operatorName);
        } else {
            doctorRollbackGroupMoveInHandler.rollback(to2, operatorId, operatorName);
        }
    }

    @Override
    public List<DoctorRollbackDto> updateReport(DoctorGroupEvent groupEvent) {
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
        //如果关闭猪群
        if (Objects.equals(trans.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            toDto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP_DELETE));
        } else {
            toDto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP));
        }

        return Lists.newArrayList(fromDto, toDto);
    }
}
