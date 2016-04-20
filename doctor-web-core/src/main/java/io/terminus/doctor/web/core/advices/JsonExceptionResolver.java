/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.advices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.JsonMapper;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-20
 */
//@ControllerAdvice
public class JsonExceptionResolver {

    private ObjectMapper objectMapper = JsonMapper.nonEmptyMapper().getMapper();

    @ExceptionHandler(value = {JsonResponseException.class})
    public void OPErrorHandler(JsonResponseException e, HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.JSON_UTF_8.toString());
        response.setStatus(e.getStatus());
        try(PrintWriter out = response.getWriter()){
            //todo: add i18n here
            out.print(objectMapper.writeValueAsString(e.getMessage()));
        }
    }
}
