package io.terminus.doctor.web.core.advices;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.exception.InvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/16
 */
//@ControllerAdvice
public class InvalidExceptionResolver {

    @Autowired
    private MessageSource messageSource;

    private ObjectMapper objectMapper = JsonMapper.nonEmptyMapper().getMapper();

    @ExceptionHandler(value = InvalidException.class)
    public void invalidErrorHandler(InvalidException e) throws Exception{
        System.out.println("--------------");
//        response.setContentType(MediaType.JSON_UTF_8.toString());
//        response.setStatus(e.getStatus());
//        try(PrintWriter out = response.getWriter()){
//            //todo: add i18n here
//            messageSource.getMessage(e.getMessage(), e.getParams(), Locale.CHINA);
//            out.print(objectMapper.writeValueAsString(e.getMessage()));
//        }
    }
}
