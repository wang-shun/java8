package io.terminus.doctor.common.validate;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/1
 */
@Aspect
public class DoctorServiceInvokeValidator {

    @Before("execution(* io.terminus.doctor..service.*.*(..))")
    public void validate(final JoinPoint point){
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DoctorInvokeValidator.instance().validateParams(point.getTarget(), method, point.getArgs());
    }
}
