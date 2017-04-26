package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/4/22.
 * 关闭
 */
@Component
public class DoctorModifyGroupCloseEventHandler extends DoctorAbstractModifyGroupEventHandler{

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
}
