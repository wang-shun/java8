package io.terminus.doctor.move.tools;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.common.exception.InvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;

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
        if (notNull(e.getAttach())) {
            errorMessage = errorMessage.concat(e.getAttach());
        }
        return new JsonResponseException(errorMessage);
    }

    public JsonResponseException convert(RuntimeException e) {
        if (e instanceof InvalidException) {
            return convert((InvalidException) e);
        }
        String errorMessage = messageSource.getMessage(e.getMessage(), new Object[]{}, Locale.CHINA);
        return new JsonResponseException(errorMessage);
    }

    public static String assembleErrorAttach(String attach, String sheetName) {
        return isNull(attach) ? sheetName : attach.concat(",页名:" + sheetName);
    }

    public static String assembleErrorAttach(String  attach, Integer lineNumber) {
        String line = isNull(lineNumber) ? "" : ",行号:".concat(lineNumber.toString());
        return isNull(attach) ? line : attach.concat(line);
    }
}
