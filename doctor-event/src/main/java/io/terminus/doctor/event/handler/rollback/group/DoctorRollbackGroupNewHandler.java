package io.terminus.doctor.event.handler.rollback.group;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 新建猪群回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/22
 */
@Slf4j
@Component
public class DoctorRollbackGroupNewHandler extends DoctorAbstractRollbackGroupEventHandler {
    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        //允许新建猪群事件回滚
        return Objects.equals(groupEvent.getType(), GroupEventType.NEW.getValue()) && isLastEvent(groupEvent);
    }

    @Override
    protected void handleRollback(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        log.info("this is a new event:{}", groupEvent);
        DoctorGroupSnapshot snapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(groupEvent.getId());

        //删除此事件 -> 删除猪群跟踪 -> 删除猪群 -> 删除镜像 -> 创建回滚日志
        doctorGroupEventDao.delete(groupEvent.getId());
        doctorGroupTrackDao.deleteByGroupId(groupEvent.getGroupId());
        doctorGroupDao.delete(groupEvent.getGroupId());
        doctorGroupSnapshotDao.delete(snapshot.getId());
        createRevertLog(groupEvent, snapshot, null, operatorId, operatorName);
    }

    @Override
    public List<DoctorRollbackDto> updateReport(DoctorGroupEvent groupEvent) {
        DoctorRollbackDto dto = new DoctorRollbackDto();
        dto.setOrgId(groupEvent.getOrgId());
        dto.setFarmId(groupEvent.getFarmId());
        dto.setEventAt(groupEvent.getEventAt());
        dto.setEsGroupId(groupEvent.getGroupId());

        //更新统计: 删除猪群
        dto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_GROUP_DELETE));
        return Lists.newArrayList(dto);
    }
}
