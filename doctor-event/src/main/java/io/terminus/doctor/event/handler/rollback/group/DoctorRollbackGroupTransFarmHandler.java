package io.terminus.doctor.event.handler.rollback.group;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.group.DoctorTransFarmGroupEvent;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 猪群转场回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/23
 */
@Slf4j
@Component
public class DoctorRollbackGroupTransFarmHandler extends DoctorAbstractRollbackGroupEventHandler {

    @Autowired private DoctorRollbackGroupCloseHandler doctorRollbackGroupCloseHandler;
    @Autowired private DoctorRollbackGroupTransHandler doctorRollbackGroupTransHandler;

    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        //猪群转群会触发目标猪群的转入猪群事件，所以需要校验目标猪群的转入猪群是否是最新事件
        if (!Objects.equals(groupEvent.getType(), GroupEventType.TRANS_FARM.getValue())) {
            return false;
        }

        //判断事件链的最后一个事件，是否是最新事件
        if (!isRelLastEvent(groupEvent)) {
            return false;
        }

        //如果触发关闭猪群事件，说明此事件肯定不是最新事件
        DoctorGroupEvent close = doctorGroupEventDao.findByRelGroupEventId(groupEvent.getId());
        if (isCloseEvent(close)) {
            return !doctorRollbackGroupCloseHandler.handleCheck(close);
        }
        return isLastEvent(groupEvent);
    }

    @Override
    protected void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        log.info("this is a trans farm event:{}", groupEvent);
        doctorRollbackGroupTransHandler.rollback(groupEvent, operatorId, operatorName);  //转场回滚和转群代码一致
    }

    @Override
    public List<DoctorRollbackDto> updateReport(DoctorGroupEvent groupEvent) {
        //更新统计：存栏日报，存栏月报，猪舍统计，猪群统计
        List<RollbackType> rollbackTypes = Lists.newArrayList(RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT,
                RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP);

        DoctorRollbackDto fromDto = new DoctorRollbackDto();
        fromDto.setOrgId(groupEvent.getOrgId());
        fromDto.setFarmId(groupEvent.getFarmId());
        fromDto.setEventAt(groupEvent.getEventAt());
        fromDto.setEsBarnId(groupEvent.getBarnId());
        fromDto.setEsGroupId(groupEvent.getGroupId());
        fromDto.setRollbackTypes(rollbackTypes);

        DoctorTransFarmGroupEvent trans = JSON_MAPPER.fromJson(groupEvent.getExtra(), DoctorTransFarmGroupEvent.class);
        DoctorRollbackDto toDto = new DoctorRollbackDto();
        toDto.setOrgId(groupEvent.getOrgId());
        toDto.setFarmId(trans.getToFarmId());   //转场事件的目标猪场
        toDto.setEventAt(groupEvent.getEventAt());
        toDto.setEsBarnId(trans.getToBarnId());
        toDto.setEsGroupId(trans.getToGroupId());
        toDto.setRollbackTypes(rollbackTypes);

        return Lists.newArrayList(fromDto, toDto);
    }
}
