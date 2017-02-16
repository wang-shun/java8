package io.terminus.doctor.web.core.advices;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.common.exception.InvalidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/16
 */
//@ControllerAdvice
public class InvalidExceptionResolver {

    @ExceptionHandler(value = InvalidException.class)
    public void invalidErrorHandler(InvalidException e, HttpServletResponse response) throws JsonResponseException {
        // TODO: 2017/2/16 转换下异常
    }
}
