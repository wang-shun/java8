package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by xjn on 17/4/22.
 * 关闭
 */
@Component
public class DoctorModifyGroupCloseEventHandler extends DoctorAbstractModifyGroupEventHandler{

    @Override
    public Boolean rollbackHandleCheck(DoctorGroupEvent deleteGroupEvent) {
        //产房已有未关闭猪群,则不准许删除之前的关闭猪群事件
        if (Objects.equals(deleteGroupEvent.getPigType(), PigType.DELIVER_SOW.getValue())) {
            List<DoctorGroup> groupList = doctorGroupDao.findByCurrentBarnId(deleteGroupEvent.getBarnId());
            if (groupList.size() > 0) {
                return false;
            }
        }
        DoctorBarn doctorBarn = doctorBarnDao.findById(deleteGroupEvent.getBarnId());
        return Objects.equals(doctorBarn.getStatus(), DoctorBarn.Status.USING.getValue());
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        return DoctorEventChangeDto.builder()
                .oldEventAt(oldGroupEvent.getEventAt())
                .newEventAt(DateUtil.toDate(input.getEventAt()))
                .build();
    }

    @Override
    public DoctorGroup buildNewGroup(DoctorGroup oldGroup, BaseGroupInput input) {
        oldGroup.setCloseAt(DateUtil.toDate(input.getEventAt()));
        return oldGroup;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        oldGroupTrack.setCloseAt(changeDto.getNewEventAt());
        return oldGroupTrack;
    }

    @Override
    protected DoctorGroup buildNewGroupForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroup oldGroup) {
        oldGroup.setStatus(DoctorGroup.Status.CREATED.getValue());
        return oldGroup;
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        oldGroupTrack.setCloseAt(DateUtil.toDate("1970-01-01"));
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        doctorGroupBatchSummaryDao.deleteByGroupId(deleteGroupEvent.getGroupId());
    }
}
