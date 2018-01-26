package io.terminus.doctor.event.advice;

import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.helper.DoctorConcurrentControl;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 18/1/26.
 * email:xiaojiannan@terminus.io
 */
@Aspect
@Component
public class concurrentControllerAdvicor {

    @Autowired
    private DoctorConcurrentControl doctorConcurrentControl;

    @Before(value = "execution(* io.terminus.doctor.event.handler.*.*.handle(..))")
    public void setKey(final JoinPoint point) {
        Object obj = point.getTarget();
        if (obj instanceof DoctorPigEventHandler) {
            DoctorPigEvent executeEvent = (DoctorPigEvent) point.getArgs()[2];
            if (isNull(executeEvent.getEventSource())
                    || Objects.equals(executeEvent.getEventSource(), SourceType.INPUT.getValue())) {
                String key = executeEvent.getFarmId().toString() + executeEvent.getKind().toString() + executeEvent.getPigCode();
                expectTrue(doctorConcurrentControl.setKey(key), "event.concurrent.error", executeEvent.getPigCode());
            }
        }
    }

    @After(value = "execution(* io.terminus.doctor.event.manager.DoctorPigEventManager.eventHandle(..))")
    public void delAllKey(final JoinPoint point){
        doctorConcurrentControl.delAll();
    }
}
