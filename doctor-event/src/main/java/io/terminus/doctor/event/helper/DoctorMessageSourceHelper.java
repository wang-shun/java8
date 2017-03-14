package io.terminus.doctor.event.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Created by xjn on 17/3/14.
 * 消息国际化
 */
@Component
public class DoctorMessageSourceHelper {
    @Autowired
    private MessageSource messageSource;

    /**
     * 消息转换
     * @param originMessage 源数据
     * @param params 参数列表
     * @return 转换后消息
     */
    public String getMessage(String originMessage, Object... params) {
        return messageSource.getMessage(originMessage, params, Locale.CHINA);
    }
}
