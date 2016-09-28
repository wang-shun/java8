package io.terminus.doctor.event.handler.rollback.group;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowEntryEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
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

    @Autowired
    private DoctorRollbackSowEntryEventHandler doctorRollbackSowEntryEventHandler;

    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        if (!Objects.equals(groupEvent.getType(), GroupEventType.TRANS_GROUP.getValue())) {
            return false;
        }
        if (!isLastEvent(groupEvent)) {
            return false;
        }

        //商品猪转种猪会触发猪的进场事件，所以需要校验猪的进场事件是否是最新事件
        DoctorPigEvent toPigEvent = doctorPigEventDao.findByRelGroupEventId(groupEvent.getId());
        return RespHelper.orFalse(doctorPigEventReadService.isLastEvent(toPigEvent.getPigId(), toPigEvent.getId()));

    }

    @Override
    protected void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        log.info("this is a turn seed event:{}", groupEvent);
        //先回滚猪的进场事件
        DoctorPigEvent toPigEvent = doctorPigEventDao.findByRelGroupEventId(groupEvent.getId());
        doctorRollbackSowEntryEventHandler.rollback(toPigEvent, operatorId, operatorName);
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

        DoctorPigEvent toPigEvent = doctorPigEventDao.findByRelGroupEventId(groupEvent.getId());
        DoctorRollbackDto toDto = new DoctorRollbackDto();
        toDto.setOrgId(toPigEvent.getOrgId());
        toDto.setFarmId(toPigEvent.getFarmId());
        toDto.setEventAt(groupEvent.getEventAt());
        toDto.setEsBarnId(toPigEvent.getBarnId());
        toDto.setEsPigId(toPigEvent.getPigId());

        //更新统计：猪舍，猪
        fromDto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG));
        return Lists.newArrayList(fromDto, toDto);
    }
}
