package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by xjn on 17/4/22.
 * 新建
 */
@Component
public class DoctorModifyGroupNewEventHandler extends DoctorAbstractModifyGroupEventHandler{

    @Override
    public Boolean rollbackHandleCheck(DoctorGroupEvent deleteGroupEvent) {
        List<DoctorGroupEvent> list = doctorGroupEventDao.findByGroupId(deleteGroupEvent.getGroupId());
        if(list.size() > 2) {
            return false;
        }
        if (list.size() == 2) {
            DoctorGroupEvent newEvent = Objects.equals(list.get(0).getType(), GroupEventType.NEW.getValue())
                    ? list.get(0) : list.get(1);

            return Objects.equals(newEvent.getIsAuto(), IsOrNot.YES.getValue()) && (Objects.equals(list.get(0).getRelGroupEventId(), list.get(1).getRelGroupEventId())
                    || Objects.equals(list.get(0).getRelPigEventId(), list.get(1).getRelPigEventId()));
        }
        return true;
    }

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
