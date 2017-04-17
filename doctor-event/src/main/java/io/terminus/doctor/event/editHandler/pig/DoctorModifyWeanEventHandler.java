package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/4/17.
 * 断奶编辑回滚处理
 */
@Component
public class DoctorModifyWeanEventHandler extends DoctorAbstractModifyPigEventHandler {

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        return null;
    }
}
