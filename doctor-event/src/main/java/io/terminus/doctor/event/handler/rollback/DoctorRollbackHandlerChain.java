package io.terminus.doctor.event.handler.rollback;

import io.terminus.doctor.event.handler.DoctorRollbackGroupEventHandler;
import io.terminus.doctor.event.handler.DoctorRollbackPigEventHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Desc: 回滚拦截器链
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
public class DoctorRollbackHandlerChain {

    @Getter @Setter
    private List<DoctorRollbackGroupEventHandler> rollbackGroupEventHandlers;

    @Getter @Setter
    private List<DoctorRollbackPigEventHandler> rollbackPigEventHandlers;
}
