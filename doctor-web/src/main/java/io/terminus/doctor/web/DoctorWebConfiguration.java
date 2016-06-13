/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web;

import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.advices.JsonExceptionResolver;
import io.terminus.doctor.web.core.msg.email.CommonEmailServiceConfig;
import io.terminus.doctor.web.core.msg.sms.LuoSiMaoSmsServiceConfig;
import io.terminus.doctor.web.core.service.OtherSystemServiceConfig;
import io.terminus.doctor.web.front.auth.DoctorCustomRoleLoaderConfigurer;
import io.terminus.parana.auth.role.CustomRoleLoaderConfigurer;
import io.terminus.parana.auth.role.CustomRoleLoaderRegistry;
import io.terminus.parana.auth.web.WebAuthenticationConfiguration;
import io.terminus.parana.web.msg.config.MsgWebConfig;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-01
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.web.core.component",
        "io.terminus.doctor.web.core.events",
        "io.terminus.doctor.web.front.component",
        "io.terminus.doctor.web.front.design"
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JsonExceptionResolver.class
        })
})
@EnableWebMvc
@EnableAutoConfiguration
@Import({DoctorCoreWebConfiguration.class,
        OtherSystemServiceConfig.class,
        WebAuthenticationConfiguration.class,
        MsgWebConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class,
})
public class DoctorWebConfiguration extends WebMvcConfigurerAdapter {

    @Configuration
    @ConditionalOnBean(ZKClientFactory.class)
    @Profile("zookeeper")
    public static class ZookeeperConfiguration{

        @Bean
        public Subscriber cacheListenerBean(ZKClientFactory zkClientFactory,
                                            @Value("${zookeeper.zkTopic}") String zkTopic) throws Exception{
            return new Subscriber(zkClientFactory,zkTopic);
        }

        @Bean
        public Publisher cachePublisherBean(ZKClientFactory zkClientFactory,
                                            @Value("${zookeeper.zkTopic}}") String zkTopic) throws Exception{
            return new Publisher(zkClientFactory, zkTopic);
        }
    }

    @Bean
    public CustomRoleLoaderConfigurer customRoleLoaderConfigurer(CustomRoleLoaderRegistry customRoleLoaderRegistry, SubRoleReadService subRoleReadService) {
        CustomRoleLoaderConfigurer configurer = new DoctorCustomRoleLoaderConfigurer(subRoleReadService);
        configurer.configureCustomRoleLoader(customRoleLoaderRegistry);
        return configurer;
    }

}
