package io.terminus.doctor.web.core.msg.email;

import io.terminus.lib.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Desc: 普通的email服务配置
 * Mail: houly@terminus.io
 * Data: 下午4:44 16/5/30
 * Author: houly
 */
@Configuration
public class CommonEmailServiceConfig {

    @Bean
    public EmailService commonEmailService(@Value("${msg.email.host:default}") String host,
                                           @Value("${msg.email.port:default}")Integer port,
                                           @Value("${msg.email.account:default}")String account,
                                           @Value("${msg.email.password:default}")String password,
                                           @Value("${msg.email.protocol:default}")Integer protocol){
        return new CommonEmailService(host, port, account, password, protocol);
    }
}
