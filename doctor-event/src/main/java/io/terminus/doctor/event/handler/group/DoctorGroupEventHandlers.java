package io.terminus.doctor.event.handler.group;

import io.terminus.doctor.event.handler.DoctorGroupEventHandler;
import lombok.Data;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 20:33 17/3/11
 */

@Data
public class DoctorGroupEventHandlers {

    private Map<Integer, DoctorGroupEventHandler> eventHandlerMap;
}
