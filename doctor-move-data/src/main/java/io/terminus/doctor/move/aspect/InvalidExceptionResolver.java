package io.terminus.doctor.move.aspect;

import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.move.tools.DoctorMessageConverter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by xjn on 17/8/28.
 *
 */
@Aspect
@Component
public class InvalidExceptionResolver {
    @Autowired
    private DoctorMessageConverter converter;

    @AfterThrowing(value = "execution(* io.terminus.doctor.move.service.DoctorMoveAndImportService.*(..))", throwing = "ex")
    public void invalidException(final JoinPoint point, InvalidException ex) throws Exception {
        throw converter.convert(ex);
    }

    @Before("execution(* io.terminus.doctor.move.tools.DoctorEventInputValidator.valid(..))")
    public void validate(final JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        ExecutableValidator validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .forExecutables();
        Set<ConstraintViolation<Object>> violations = validator.validateParameters(point.getTarget(), method, point.getArgs());
        if (violations.size() > 0) {
            throw new InvalidException(violations.iterator().next().getMessage(), point.getArgs()[0]);
        }
    }
}
