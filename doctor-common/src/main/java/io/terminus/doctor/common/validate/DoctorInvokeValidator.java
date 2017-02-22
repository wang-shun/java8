package io.terminus.doctor.common.validate;

import io.terminus.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/1
 */
@Slf4j
public class DoctorInvokeValidator {
    private static final DoctorInvokeValidator instance = new DoctorInvokeValidator();

    public static DoctorInvokeValidator instance() {
        return instance;
    }

    private ExecutableValidator validator;

    public DoctorInvokeValidator() {
        this.validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .forExecutables();
    }

    /**
     * 参数验证
     * @param target 目标对象
     * @param method 目标方法
     * @param args 调用参数
     */
    public void validateParams(Object target, Method method, Object[] args) {
        Set<ConstraintViolation<Object>> violations = validator.validateParameters(target, method, args);
        if (violations.size() > 0){
            log.error("failed to validate service({})'s method({})'s params: {}", target, method, violations);
            throw new ServiceException(violations.iterator().next().getMessage());
        }
    }


}
