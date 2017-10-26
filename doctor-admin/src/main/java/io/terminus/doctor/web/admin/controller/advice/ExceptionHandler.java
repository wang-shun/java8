package io.terminus.doctor.web.admin.controller.advice;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by sunbo@terminus.io on 2017/10/26.
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({JsonResponseException.class, ServiceException.class})
    public Response<String> DoctorExceptionHandle(Exception e) {
        return Response.fail(e.getMessage());
    }

    @ResponseStatus
    @org.springframework.web.bind.annotation.ExceptionHandler(NullPointerException.class)
    public Response<String> nullPointExceptionHandle(NullPointerException e) {
        log.error("", e);
        return Response.fail("null.point.exception");
    }

}
