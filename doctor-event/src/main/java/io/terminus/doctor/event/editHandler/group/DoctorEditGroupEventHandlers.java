package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.event.editHandler.DoctorEditGroupEventHandler;
import lombok.Data;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 22:01 17/3/8
 */

@Data
public class DoctorEditGroupEventHandlers {

    private Map<Integer, DoctorEditGroupEventHandler> eventHandlerMap;
}
