/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web;

import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.advices.JsonExceptionResolver;
import io.terminus.doctor.web.core.msg.email.CommonEmailServiceConfig;
import io.terminus.doctor.web.core.msg.sms.LuoSiMaoSmsServiceConfig;
import io.terminus.doctor.web.core.service.OtherSystemService;
import io.terminus.doctor.web.core.service.impl.OtherSystemServiceImpl;
import io.terminus.doctor.web.front.auth.DoctorCustomRoleLoaderConfigurer;
import io.terminus.parana.auth.role.CustomRoleLoaderConfigurer;
import io.terminus.parana.auth.web.WebAuthenticationConfiguration;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.parana.web.msg.config.db.DbAppPushConfig;
import io.terminus.parana.web.msg.config.db.DbEmailConfig;
import io.terminus.parana.web.msg.config.db.DbNotifyConfig;
import io.terminus.parana.web.msg.config.db.DbSmsConfig;
import io.terminus.parana.web.msg.config.gatewaybuilder.SimpleMsgGatewayBuilderConfig;
import io.terminus.parana.web.msg.config.test.TestAppPushWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestEmailWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestNotifyWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestSmsWebServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-01
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.web.core.advices",
        "io.terminus.doctor.web.core.component",
        "io.terminus.doctor.web.core.events",
        "io.terminus.doctor.web.core.exceptions",
        "io.terminus.doctor.web.front.component",
        "io.terminus.doctor.web.front.design",
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JsonExceptionResolver.class
        })
})
@EnableWebMvc
@Import({DoctorCoreWebConfiguration.class,
        WebAuthenticationConfiguration.class,
        SimpleMsgGatewayBuilderConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class,
        DbSmsConfig.class, TestSmsWebServiceConfig.class,
        DbNotifyConfig.class,TestNotifyWebServiceConfig.class,
        DbEmailConfig.class, TestEmailWebServiceConfig.class,
        DbAppPushConfig.class, TestAppPushWebServiceConfig.class
})
public class DoctorWebConfiguration extends WebMvcConfigurerAdapter {

    @Bean
    public CustomRoleLoaderConfigurer customRoleLoaderConfigurer(SubRoleReadService subRoleReadService) {
          return new DoctorCustomRoleLoaderConfigurer(subRoleReadService);
    }

    @Bean
    public OtherSystemService otherSystemServiceConfigurer(ConfigCenter configCenter) {
        return new OtherSystemServiceImpl(configCenter);
    }
}
