package io.terminus.doctor.web.core.msg;

import io.terminus.lib.sms.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Desc:
 * Mail: houly@terminus.io
 * Data: 下午5:53 16/5/26
 * Author: houly
 */
@Configuration
public class LuoSiMaoSmsServiceConfig {
    @Bean
    public SmsService luoSiMaoSmsService(@Value("${msg.luosimao.sendUrl:http://sms-api.luosimao.com/v1/http_get/send/json}")String sendUrl,
                                         @Value("${msg.luosimao.statusUrl:http://sms-api.luosimao.com/v1/http_get/status/json}")String statusUrl,
                                         @Value("${msg.luosimao.apiKey:default}")String apiKey,
                                         @Value("${msg.luosimao.companyName:default}")String companyName){
        return new LuoSiMaoSmsService(sendUrl,statusUrl,apiKey,companyName);
    }
}
