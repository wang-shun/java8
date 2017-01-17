package io.terminus.doctor.event.handler;

import lombok.Data;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: handler chain
 */
@Data
public class DoctorPigEventHandlers {
    private Map<Integer, DoctorPigEventHandler> eventHandlerMap;
}
