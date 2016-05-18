package io.terminus.doctor.web.core.configs;

import io.terminus.doctor.web.core.service.OtherSystemService;
import io.terminus.doctor.web.core.service.impl.OtherSystemDbServiceImpl;
import io.terminus.parana.config.ConfigCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 陈增辉 16/5/18.
 */

@Configuration
public class OtherSystemDbConfig {

    @Bean
    @ConditionalOnMissingBean(OtherSystemService.class)
//    @ConditionalOnBean(ConfigCenter.class)
    public OtherSystemService otherSystemService(ConfigCenter configCenter){
        return new OtherSystemDbServiceImpl(configCenter);
    }
}
