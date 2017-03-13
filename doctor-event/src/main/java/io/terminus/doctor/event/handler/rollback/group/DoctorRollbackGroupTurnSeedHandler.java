package io.terminus.doctor.event.handler.rollback.group;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarEntryEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowEntryEventHandler;
import io.terminus.doctor.event.model.DoctorEventRelation;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 商品猪转种猪事件回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
@Component
public class DoctorRollbackGroupTurnSeedHandler extends DoctorAbstractRollbackGroupEventHandler {

    @Autowired private DoctorRollbackSowEntryEventHandler doctorRollbackSowEntryEventHandler;
    @Autowired private DoctorRollbackBoarEntryEventHandler doctorRollbackBoarEntryEventHandler;
    @Autowired private DoctorRollbackGroupCloseHandler doctorRollbackGroupCloseHandler;

    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        if (!Objects.equals(groupEvent.getType(), GroupEventType.TURN_SEED.getValue())) {
            return false;
        }

        //如果触发关闭猪群事件
        Long toGroupEventId = doctorEventRelationDao.findByOriginAndType(groupEvent.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
        DoctorGroupEvent close = doctorGroupEventDao.findById(toGroupEventId);

        if (isCloseEvent(close) && !doctorRollbackGroupCloseHandler.handleCheck(close)) {
            return false;
        }

        //商品猪转种猪会触发猪的进场事件，所以需要校验猪的进场事件是否是最新事件
        Long toPigEventId = doctorEventRelationDao.findByOriginAndType(groupEvent.getId(), DoctorEventRelation.TargetType.PIG.getValue()).getTriggerEventId();
        DoctorPigEvent toPigEvent = doctorPigEventDao.findById(toPigEventId);
        DoctorPigEvent lastPigEvent = doctorPigEventDao.queryLastPigEventById(toPigEvent.getPigId());
        return Objects.equals(lastPigEvent.getId(), toPigEvent.getId());


    }

    @Override
    protected void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        log.info("this is a turn seed event:{}", groupEvent);
        Long toGroupEventId = doctorEventRelationDao.findByOriginAndType(groupEvent.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
        DoctorGroupEvent close = doctorGroupEventDao.findById(toGroupEventId);

        //如果触发关闭猪群事件, 要回滚关闭事件
        if (isCloseEvent(close)) {
            rollbackEntry(close, operatorId, operatorName);
            doctorRollbackGroupCloseHandler.rollback(close, operatorId, operatorName);
        } else {
            rollbackEntry(groupEvent, operatorId, operatorName);
        }
        sampleRollback(groupEvent, operatorId, operatorName);
    }

    private void rollbackEntry(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        //先回滚猪的进场事件(判断公猪还是母猪进场)
        Long toPigEventId = doctorEventRelationDao.findByOriginAndType(groupEvent.getId(), DoctorEventRelation.TargetType.PIG.getValue()).getTriggerEventId();
        DoctorPigEvent toPigEvent = doctorPigEventDao.findById(toPigEventId);
        if (Objects.equals(toPigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            doctorRollbackSowEntryEventHandler.rollback(toPigEvent, operatorId, operatorName);
        } else {
            doctorRollbackBoarEntryEventHandler.rollback(toPigEvent, operatorId, operatorName);
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
        return Lists.newArrayList(fromDto);
    }
}
