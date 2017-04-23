package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.event.editHandler.DoctorModifyGroupEventHandler;
import lombok.Data;

import java.util.Map;

/**
 * Created by xjn on 17/4/14.
 */
@Data
public class DoctorModifyGroupEventHandlers {
    private Map<Integer, DoctorModifyGroupEventHandler> modifyGroupEventHandlerMap;
}
