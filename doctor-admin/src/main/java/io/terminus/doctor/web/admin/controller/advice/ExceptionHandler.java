package io.terminus.doctor.web.admin.controller.advice;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.web.admin.controller.DoctorGodController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by sunbo@terminus.io on 2017/10/26.
 */
@Slf4j
@ResponseBody
@ControllerAdvice(assignableTypes = DoctorGodController.class)
public class ExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ResponseStatus()
    @org.springframework.web.bind.annotation.ExceptionHandler({JsonResponseException.class, ServiceException.class})
    public String DoctorExceptionHandle(Exception e) {
        return messageSource.getMessage(e.getMessage(), new Object[0], Locale.getDefault());
    }


    @ResponseStatus
    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidException.class)
    public String invalidExceptionHandle(InvalidException e) {
        return messageSource.getMessage(e.getError(), e.getParams(), Locale.getDefault());
    }

    @ResponseStatus
    @org.springframework.web.bind.annotation.ExceptionHandler(NullPointerException.class)
    public String nullPointExceptionHandle(NullPointerException e) {
        log.error("", e);
        return messageSource.getMessage(("null.point.exception"), new Object[0], Locale.getDefault());
    }

}
