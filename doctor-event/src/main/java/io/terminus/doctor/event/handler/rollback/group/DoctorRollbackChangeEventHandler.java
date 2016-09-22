package io.terminus.doctor.event.handler.rollback.group;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 猪群变动回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/22
 */
@Slf4j
@Component
public class DoctorRollbackChangeEventHandler extends DoctorAbstractRollbackGroupEventHandler {

    @Override
    protected boolean handleCheck(DoctorGroupEvent groupEvent) {
        //允许猪群变动事件
        return Objects.equals(groupEvent.getType(), GroupEventType.CHANGE.getValue());
    }

    @Override
    protected DoctorRevertLog handleRollback(DoctorGroupEvent groupEvent) {
        DoctorGroupSnapshot snapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(groupEvent.getId());

        DoctorGroupSnapShotInfo info = JSON_MAPPER.fromJson(snapshot.getFromInfo(), DoctorGroupSnapShotInfo.class);

        //删除此事件 -> 回滚猪群跟踪 -> 回滚猪群 -> 删除镜像
        doctorGroupEventDao.delete(groupEvent.getId());
        doctorGroupTrackDao.update(info.getGroupTrack());
        doctorGroupDao.update(info.getGroup());
        doctorGroupSnapshotDao.delete(snapshot.getId());
        return DoctorRevertLog.builder()
                .fromInfo(snapshot.getToInfo())
                .toInfo(snapshot.getFromInfo())
                .build();
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorGroupEvent groupEvent) {
        DoctorRollbackDto dto = new DoctorRollbackDto();
        dto.setFarmId(groupEvent.getFarmId());
        dto.setEsBarnId(groupEvent.getBarnId());
        dto.setEsGroupId(groupEvent.getGroupId());

        //更新统计：存栏日报，存栏月报，猪舍统计，猪群统计，销售/死淘日报
        List<RollbackType> rollbackTypes = Lists.newArrayList(
                RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT, RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP);

        if (Objects.equals(groupEvent.getChangeTypeId(), DoctorBasicEnums.SALE.getId())) {
            rollbackTypes.add(RollbackType.DAILY_SALE);
        }
        if (Objects.equals(groupEvent.getChangeTypeId(), DoctorBasicEnums.DEAD.getId()) ||
                Objects.equals(groupEvent.getChangeTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
            rollbackTypes.add(RollbackType.DAILY_DEAD);
        }
        dto.setRollbackTypes(rollbackTypes);
        return Lists.newArrayList(dto);
    }
}
