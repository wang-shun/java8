/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.admin;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import io.terminus.doctor.user.service.OperatorRoleReadService;
import io.terminus.doctor.web.admin.auth.DoctorCustomRoleLoaderConfigurer;
import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.advices.JsonExceptionResolver;
import io.terminus.doctor.web.core.msg.email.CommonEmailServiceConfig;
import io.terminus.doctor.web.core.msg.sms.LuoSiMaoSmsServiceConfig;
import io.terminus.doctor.web.core.service.OtherSystemServiceConfig;
import io.terminus.parana.auth.role.CustomRoleLoaderConfigurer;
import io.terminus.parana.auth.role.CustomRoleLoaderRegistry;
import io.terminus.parana.auth.web.WebAuthenticationConfiguration;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.parana.web.msg.config.MsgWebConfig;
import io.terminus.zookeeper.common.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Author  : panxin
 * Date    : 6:17 PM 2/29/16
 * Mail    : panxin@terminus.io
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.web.core.component",
        "io.terminus.doctor.web.core.events",
        "io.terminus.doctor.web.admin",
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JsonExceptionResolver.class,
        })
})
@EnableWebMvc
@EnableScheduling
@EnableAutoConfiguration
@Import({DoctorCoreWebConfiguration.class,
        OtherSystemServiceConfig.class,
        WebAuthenticationConfiguration.class,
        MsgWebConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class
})
public class DoctorAdminConfiguration extends WebMvcConfigurerAdapter {

    @Bean(autowire = Autowire.BY_NAME)
    public ConfigCenter configCenter() {
        return new ConfigCenter();
    }

    @Bean
    public CustomRoleLoaderConfigurer customRoleLoaderConfigurer(CustomRoleLoaderRegistry customRoleLoaderRegistry,
                                                                 OperatorRoleReadService operatorRoleReadService) {
        CustomRoleLoaderConfigurer configurer = new DoctorCustomRoleLoaderConfigurer(operatorRoleReadService);
        configurer.configureCustomRoleLoader(customRoleLoaderRegistry);
        return configurer;
    }

    @Bean
    public Subscriber cacheListenerBean(ZKClientFactory zkClientFactory,
                                        @Value("${zookeeper.zkTopic}") String cacheTopic) throws Exception {
        return new Subscriber(zkClientFactory, cacheTopic);
    }

    @Bean
    public Publisher cachePublisherBean(ZKClientFactory zkClientFactory,
                                        @Value("${zookeeper.zkTopic}") String cacheTopic) throws Exception {
        return new Publisher(zkClientFactory, cacheTopic);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 移除默认的 StringHttpMessageConverter 避免因为优先级的问题出现编码异常
        Iterators.removeIf(converters.iterator(), new Predicate<HttpMessageConverter<?>>() {
            @Override
            public boolean apply(HttpMessageConverter<?> input) {
                return input instanceof StringHttpMessageConverter;
            }
        });

        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(Charsets.UTF_8);
        stringHttpMessageConverter.setSupportedMediaTypes(Lists.newArrayList(MediaType.TEXT_PLAIN, MediaType.ALL));
        converters.add(1, stringHttpMessageConverter);
    }
}
