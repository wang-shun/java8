package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.event.editHandler.DoctorModifyPigEventHandler;
import lombok.Data;

import java.util.Map;

/**
 * Created by xjn on 17/4/14.
 */
@Data
public class DoctorModifyPigEventHandlers {
    Map<Integer, DoctorModifyPigEventHandler> modifyPigEventHandlerMap;
}
