package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupMoveInHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupNewHandler;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 母猪分娩回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
@Component
public class DoctorRollbackSowFarrowHandler extends DoctorAbstractRollbackPigEventHandler {

    @Autowired
    private DoctorRollbackGroupMoveInHandler doctorRollbackGroupMoveInHandler;

    @Autowired
    private DoctorRollbackGroupNewHandler doctorRollbackGroupNewHandler;

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        if (!Objects.equals(pigEvent.getType(), PigEvent.FARROWING.getKey())) {
            return false;
        }

        //母猪分娩会触发转入猪群事件，如果有新建猪群，还要校验最新事件(分娩)
        DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelPigEventId(pigEvent.getId());
        return isRelLastGroupEvent(toGroupEvent);
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        //1.判断是否新建猪群
        DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelPigEventId(pigEvent.getId());
        if (Objects.equals(toGroupEvent.getType(), GroupEventType.NEW.getValue())) {
            DoctorGroupEvent moveInEvent = doctorGroupEventDao.findByRelGroupEventId(toGroupEvent.getId());
            doctorRollbackGroupMoveInHandler.rollback(moveInEvent, operatorId, operatorName);
            doctorRollbackGroupNewHandler.rollback(toGroupEvent, operatorId, operatorName);
        } else {
            doctorRollbackGroupMoveInHandler.rollback(toGroupEvent, operatorId, operatorName);
        }

        //2. 母猪分娩
        handleRollbackWithStatus(pigEvent, operatorId, operatorName);
    }

    @Override
        public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        DoctorRollbackDto fromDto = new DoctorRollbackDto();
        fromDto.setOrgId(pigEvent.getOrgId());
        fromDto.setFarmId(pigEvent.getFarmId());
        fromDto.setEventAt(pigEvent.getEventAt());
        fromDto.setEsBarnId(pigEvent.getBarnId());
        fromDto.setEsPigId(pigEvent.getPigId());

        //更新统计：存栏日报，存栏月报，猪舍统计，猪统计, 分娩统计
        fromDto.setRollbackTypes(Lists.newArrayList(RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT,
                RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP, RollbackType.DAILY_FARROW, RollbackType.SEARCH_PIG));

        DoctorRollbackDto toDto = new DoctorRollbackDto();
        toDto.setOrgId(pigEvent.getOrgId());
        toDto.setFarmId(pigEvent.getFarmId());
        toDto.setEventAt(pigEvent.getEventAt());

        Long farrowGroupId = Params.getNullDefualt(pigEvent.getExtraMap(), "farrowGroupId", null);
        if (farrowGroupId == null) {
            toDto.setEsBarnId(pigEvent.getBarnId());
            toDto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP));
            return Lists.newArrayList(fromDto, toDto);
        }

        //如果猪群不存在了，也要删掉索引
        DoctorGroup group = doctorGroupDao.findById(farrowGroupId);
        if (group == null) {
            toDto.setEsGroupId(farrowGroupId);
            toDto.setEsBarnId(pigEvent.getBarnId());
            toDto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP_DELETE));
        } else {
            toDto.setEsGroupId(group.getId());
            toDto.setEsBarnId(group.getCurrentBarnId());
            toDto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP));
        }
        return Lists.newArrayList(fromDto, toDto);
    }

}
