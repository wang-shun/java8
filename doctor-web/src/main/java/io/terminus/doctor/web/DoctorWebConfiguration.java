/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web;

import com.google.common.eventbus.EventBus;
import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.advices.JsonExceptionResolver;
import io.terminus.parana.web.msg.config.db.DbAppPushConfig;
import io.terminus.parana.web.msg.config.db.DbEmailConfig;
import io.terminus.parana.web.msg.config.db.DbNotifyConfig;
import io.terminus.parana.web.msg.config.db.DbSmsConfig;
import io.terminus.parana.web.msg.config.gatewaybuilder.DbMsgGatewayBuilderConfig;
import io.terminus.parana.web.msg.config.test.TestAppPushWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestEmailWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestNotifyWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestSmsWebServiceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
        DbMsgGatewayBuilderConfig.class,
        DbSmsConfig.class, TestSmsWebServiceConfig.class,
        DbNotifyConfig.class,TestNotifyWebServiceConfig.class,
        DbEmailConfig.class, TestEmailWebServiceConfig.class,
        DbAppPushConfig.class, TestAppPushWebServiceConfig.class
})
public class DoctorWebConfiguration extends WebMvcConfigurerAdapter {

}
