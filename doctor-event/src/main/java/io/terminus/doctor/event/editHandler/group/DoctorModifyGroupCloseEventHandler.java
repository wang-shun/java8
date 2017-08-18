package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
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
        return true;
    }

    @Override
    public DoctorGroup buildNewGroup(DoctorGroup oldGroup, BaseGroupInput input) {
        oldGroup.setCloseAt(DateUtil.toDate(input.getEventAt()));
        return oldGroup;
    }

    @Override
    protected DoctorGroup buildNewGroupForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroup oldGroup) {
        oldGroup.setStatus(DoctorGroup.Status.CREATED.getValue());
        return oldGroup;
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
        doctorGroupBatchSummaryDao.deleteByGroupId(deleteGroupEvent.getGroupId());
    }
}
