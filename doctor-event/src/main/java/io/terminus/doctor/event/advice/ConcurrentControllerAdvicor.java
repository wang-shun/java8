package io.terminus.doctor.event.advice;

import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.helper.DoctorConcurrentControl;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * 并发控制
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConcurrentControllerAdvicor {

    private final DoctorConcurrentControl doctorConcurrentControl;

    @Autowired
    public ConcurrentControllerAdvicor(DoctorConcurrentControl doctorConcurrentControl) {
        this.doctorConcurrentControl = doctorConcurrentControl;
    }

//    @Before(value = "execution(* io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.rollbackHandle(..))")
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

    /**
     * 事件增删改完成后释放当前线程拥有的锁
     */
    @After("pigEventHandle() || pigBatchEventHandle() || pigRollback() || pigModify()" +
            "|| newGroupHandle() || batchNewGroupHandle() || groupEventHandle() || groupBatchEventHandle() " +
            "|| groupRollback() || groupModify()")
    public void delAllKey(final JoinPoint point){
        doctorConcurrentControl.delAll();
    }

    @Pointcut(value = "execution(* io.terminus.doctor.event.manager.DoctorPigEventManager.eventHandle(..))")
    private void pigEventHandle(){
    }

    @Pointcut(value = "execution(* io.terminus.doctor.event.manager.DoctorPigEventManager.batchEventsHandle(..))")
    private void pigBatchEventHandle(){
    }

    @Pointcut(value = "execution(Long io.terminus.doctor.event.manager.DoctorGroupManager.createNewGroup(..))")
    private void newGroupHandle(){
    }

    @Pointcut(value = "execution(Long io.terminus.doctor.event.manager.DoctorGroupManager.batchNewGroupEventHandle(..))")
    private void batchNewGroupHandle(){
    }

    @Pointcut(value = "execution(* io.terminus.doctor.event.manager.DoctorGroupEventManager.handleEvent(..))")
    private void groupEventHandle(){
    }

    @Pointcut(value = "execution(* io.terminus.doctor.event.manager.DoctorGroupEventManager.batchHandleEvent(..))")
    private void groupBatchEventHandle(){
    }

    @Pointcut(value = "execution(* io.terminus.doctor.event.manager.DoctorRollbackManager.rollbackPig(..))")
    private void pigRollback(){
    }

    @Pointcut(value = "execution(* io.terminus.doctor.event.manager.DoctorPigEventManager.modifyPigEventHandle(..))")
    private void pigModify(){
    }

    @Pointcut(value = "execution(* io.terminus.doctor.event.manager.DoctorRollbackManager.rollbackGroup(..))")
    private void groupRollback(){
    }

    @Pointcut(value = "execution(* io.terminus.doctor.event.manager.DoctorGroupEventManager.modifyGroupEventHandle(..))")
    private void groupModify(){
    }
}
