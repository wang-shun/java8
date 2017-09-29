package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.exception.InvalidException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AdviceSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by sunbo@terminus.io on 2017/9/27.
 */
@Slf4j
@Component
@Aspect
@Order(300)//需要包裹在事务切面之外
public class DoctorServiceExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @Around("execution(* io.terminus.doctor.basic.service.warehouseV2.*.*(..))")
    public Object exceptionHandle(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        try {
            return proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            try {
                Method method = proceedingJoinPoint.getTarget().getClass().getMethod(proceedingJoinPoint.getSignature().getName(), ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterTypes());
                if (method.isAnnotationPresent(ExceptionHandle.class)) {
                    ExceptionHandle exceptionHandle = method.getDeclaredAnnotation(ExceptionHandle.class);


                    if (throwable instanceof ServiceException)
                        return Response.fail(throwable.getMessage());
                    if (throwable instanceof InvalidException) {
                        InvalidException invalidException = (InvalidException) throwable;
                        return Response.fail(messageSource.getMessage(invalidException.getError(), invalidException.getParams(), Locale.CHINA));
                    }

                    log.error("{}, cause:{}", exceptionHandle.value(), Throwables.getStackTraceAsString(throwable));
                    return Response.fail(exceptionHandle.value());
                } else
                    throw throwable;
            } catch (Exception e) {
                throw throwable;
            }
        }
    }
}
