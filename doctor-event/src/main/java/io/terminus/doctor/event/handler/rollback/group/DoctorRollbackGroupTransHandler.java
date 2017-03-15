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

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

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
//        if (!Objects.equals(groupEvent.getType(), GroupEventType.TRANS_GROUP.getValue())) {
//            return false;
//        }

        DoctorEventRelation eventRelation = doctorEventRelationDao.findByOriginAndType(groupEvent.getId(), DoctorEventRelation.TargetType.GROUP.getValue());
        expectTrue(notNull(eventRelation), "relate.group.event.not.null" , groupEvent.getId());
        DoctorGroupEvent moveInEvent = doctorGroupEventDao.findById(eventRelation.getTriggerEventId());
        boolean canRollback = isLastEvent(moveInEvent);
        //如果触发关闭猪群事件，说明此事件肯定不是最新事件
        DoctorGroupEvent close = doctorGroupEventDao.findByRelGroupEventIdAndType(groupEvent.getId(), GroupEventType.CLOSE.getValue());
        if (isCloseEvent(close)) {
            return canRollback && doctorRollbackGroupCloseHandler.handleCheck(close);
        }

        return canRollback && isLastEvent(groupEvent);
    }

    @Override
    public void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        log.info("this is a trans event:{}", groupEvent);
        //1.回滚转入时间
        DoctorEventRelation eventRelation = doctorEventRelationDao.findByOriginAndType(groupEvent.getId(), DoctorEventRelation.TargetType.GROUP.getValue());
        expectTrue(notNull(eventRelation), "relate.group.event.not.null" , groupEvent.getId());
        DoctorGroupEvent moveIn = doctorGroupEventDao.findById(eventRelation.getTriggerEventId());
        doctorRollbackGroupMoveInHandler.rollback(moveIn, operatorId, operatorName);

        //2.如果有新建,回滚新建
        DoctorGroupEvent newCreateEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(groupEvent.getId(), GroupEventType.NEW.getValue());
        if (notNull(newCreateEvent)) {
            doctorRollbackGroupNewHandler.rollback(newCreateEvent, operatorId, operatorName);
        }
        //3.如果关闭猪群,回滚关闭猪群
        DoctorGroupEvent close = doctorGroupEventDao.findByRelGroupEventIdAndType(groupEvent.getId(), GroupEventType.CLOSE.getValue());
        if (isCloseEvent(close)) {
            doctorRollbackGroupCloseHandler.rollback(close, operatorId, operatorName);    //回滚关闭猪群
        }

        //4.回滚自己
        sampleRollback(groupEvent, operatorId, operatorName);
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
