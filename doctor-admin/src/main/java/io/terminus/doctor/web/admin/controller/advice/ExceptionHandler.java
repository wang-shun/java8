package io.terminus.doctor.web.admin.controller.advice;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by sunbo@terminus.io on 2017/10/26.
 */
@ControllerAdvice
@ResponseBody
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({JsonResponseException.class, ServiceException.class})
    public Response<String> DoctorExceptionHandle(Exception e) {
        return Response.fail(e.getMessage());
    }

}
