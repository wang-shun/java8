/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.advices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.terminus.common.utils.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Just catch all npe, log it , for devops
 *
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-21
 */
@ControllerAdvice
@Slf4j
public class NPEResolver {
    private ObjectMapper objectMapper = JsonMapper.nonEmptyMapper().getMapper();

    @ExceptionHandler(value = {NullPointerException.class})
    public void OPErrorHandler(NullPointerException e, HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        try(PrintWriter out = response.getWriter()){
            log.error("oops, NPE {} caught", Throwables.getStackTraceAsString(e));
            out.write(objectMapper.writeValueAsString(e));
        }
    }
}
