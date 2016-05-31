/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.admin;

import io.terminus.doctor.user.service.OperatorRoleReadService;
import io.terminus.doctor.web.admin.auth.DoctorCustomRoleLoaderConfigurer;
import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.msg.email.CommonEmailServiceConfig;
import io.terminus.doctor.web.core.msg.sms.LuoSiMaoSmsServiceConfig;
import io.terminus.doctor.web.core.service.OtherSystemService;
import io.terminus.doctor.web.core.service.impl.OtherSystemServiceImpl;
import io.terminus.parana.auth.role.CustomRoleLoaderConfigurer;
import io.terminus.parana.auth.web.WebAuthenticationConfiguration;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.doctor.web.core.advices.JsonExceptionResolver;
import io.terminus.parana.web.msg.config.MsgAdminWebConfig;
import io.terminus.parana.web.msg.config.db.DbNotifyConfig;
import io.terminus.parana.web.msg.config.gatewaybuilder.SimpleMsgGatewayBuilderConfig;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Author  : panxin
 * Date    : 6:17 PM 2/29/16
 * Mail    : panxin@terminus.io
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.web.core.advices",
        "io.terminus.doctor.web.core.component",
        "io.terminus.doctor.web.core.events",
        "io.terminus.doctor.web.core.exceptions",
        "io.terminus.doctor.web.admin.jobs",
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JsonExceptionResolver.class,
        })
})
@EnableWebMvc
@EnableScheduling
@Import({DoctorCoreWebConfiguration.class,
        WebAuthenticationConfiguration.class,
        SimpleMsgGatewayBuilderConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class,
        MsgAdminWebConfig.class, DbNotifyConfig.class})
public class DoctorAdminConfiguration extends WebMvcConfigurerAdapter {

    @Bean(autowire = Autowire.BY_NAME)
    public ConfigCenter configCenter() {
        return new ConfigCenter();
    }

    @Bean
    public CustomRoleLoaderConfigurer customRoleLoaderConfigurer(OperatorRoleReadService operatorRoleReadService) {
        return new DoctorCustomRoleLoaderConfigurer(operatorRoleReadService);
    }

    @Bean
    public OtherSystemService otherSystemServiceConfigurer(ConfigCenter configCenter) {
        return new OtherSystemServiceImpl(configCenter);
    }
}
