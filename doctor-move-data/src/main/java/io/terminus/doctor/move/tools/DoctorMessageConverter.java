package io.terminus.doctor.move.tools;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.common.exception.InvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Created by xjn on 17/8/28.
 * 错误消息转换器
 */
@Component
public class DoctorMessageConverter {
    @Autowired
    private MessageSource messageSource;

    public JsonResponseException convert(InvalidException e) {
        String errorMessage = messageSource.getMessage(e.getError(), e.getParams(), Locale.CHINA);
        return new JsonResponseException(errorMessage.concat(",行号:").concat(e.getAttach()));
    }
}
