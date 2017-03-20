package io.terminus.doctor.event.handler.rollback;

import com.google.common.collect.Table;
import io.terminus.doctor.event.handler.DoctorRollbackGroupEventHandler;
import io.terminus.doctor.event.handler.DoctorRollbackPigEventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Desc: 回滚拦截器链
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRollbackHandlerChain {

    @Getter @Setter
    private Map<Integer, DoctorRollbackGroupEventHandler> rollbackGroupEventHandlers;

    @Getter @Setter
    private Table<Integer, Integer, DoctorRollbackPigEventHandler> rollbackPigEventHandlers;
}
