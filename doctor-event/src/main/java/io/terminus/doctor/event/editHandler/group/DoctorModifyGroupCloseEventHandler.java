package io.terminus.doctor.event.editHandler.group;

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
    protected DoctorGroup buildNewGroupForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroup oldGroup) {
        oldGroup.setStatus(DoctorGroup.Status.CREATED.getValue());
        return oldGroup;
    }
}
