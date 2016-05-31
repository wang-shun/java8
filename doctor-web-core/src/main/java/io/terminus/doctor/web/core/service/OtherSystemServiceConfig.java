package io.terminus.doctor.web.core.service;

import io.terminus.doctor.web.core.service.impl.OtherSystemServiceImpl;
import io.terminus.parana.config.ConfigCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Desc:
 * Mail: houly@terminus.io
 * Data: 下午12:42 16/5/31
 * Author: houly
 */
@Configuration
public class OtherSystemServiceConfig {
    @Bean
    @ConditionalOnMissingBean(OtherSystemService.class)
    public OtherSystemService otherSystemServiceConfigurer(ConfigCenter configCenter) {
        return new OtherSystemServiceImpl(configCenter);
    }
}
