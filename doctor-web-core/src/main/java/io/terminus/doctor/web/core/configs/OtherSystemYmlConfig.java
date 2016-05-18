package io.terminus.doctor.web.core.configs;

import io.terminus.doctor.web.core.service.OtherSystemService;
import io.terminus.doctor.web.core.service.impl.OtherSystemYmlServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 陈增辉 16/5/18.
 */

@Configuration
public class OtherSystemYmlConfig {

    @Bean
    @ConditionalOnMissingBean(OtherSystemService.class)
    public OtherSystemService otherSystemService(){
        return new OtherSystemYmlServiceImpl();
    }
}
