package io.terminus.doctor.web.core.aspects;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.common.exception.InvalidException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;

/**
 * Created by xjn on 17/2/16.
 */
@Aspect
@Component
public class InvalidExceptionResolver {
    @Autowired
    private MessageSource messageSource;
    private final static String ATTACH = ",猪号/猪群号:";

    @AfterThrowing(value = "execution(* io.terminus.doctor.web.front.event.controller.*.*(..))", throwing = "ex")
    public void invalidException(final JoinPoint point, InvalidException ex) throws Exception {

        String errorMessage = messageSource.getMessage(ex.getError(), ex.getParams(), Locale.CHINA);
        if (ex.isBatchEvent()) {
            errorMessage = errorMessage.concat(ATTACH).concat(ex.getAttach());
        }
        throw new JsonResponseException(errorMessage);
    }

    @Before("execution(* io.terminus.doctor.web.core.aspects.DoctorValidService.valid(..))")
    public void validate(final JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        ExecutableValidator validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .forExecutables();
        Set<ConstraintViolation<Object>> violations = validator.validateParameters(point.getTarget(), method, point.getArgs());
        if (violations.size() > 0) {
            throw new InvalidException(violations.iterator().next().getMessage(), point.getArgs()[1]);
        }
    }
}
