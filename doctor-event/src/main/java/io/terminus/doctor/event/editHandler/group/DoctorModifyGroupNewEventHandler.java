package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/4/22.
 * 新建
 */
@Component
public class DoctorModifyGroupNewEventHandler extends DoctorAbstractModifyGroupEventHandler{

    @Override
    public DoctorGroup buildNewGroup(DoctorGroup oldGroup, BaseGroupInput input) {
        DoctorNewGroupInput newInput = (DoctorNewGroupInput) input;
        oldGroup.setBreedId(newInput.getBreedId());
        oldGroup.setBreedName(newInput.getBreedName());
        oldGroup.setOpenAt(DateUtil.toDate(newInput.getEventAt()));
        oldGroup.setGeneticId(newInput.getGeneticId());
        oldGroup.setGeneticName(newInput.getGeneticName());
        return oldGroup;
    }
}
