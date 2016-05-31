package configuration.admin;

import io.terminus.doctor.event.DoctorEventConfiguration;
import io.terminus.doctor.interceptor.MockLoginInterceptor;
import io.terminus.doctor.msg.DoctorMsgConfig;
import io.terminus.doctor.user.DoctorUserConfiguration;
import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.component.DoctorHbsHelpers;
import io.terminus.doctor.web.core.component.ParanaHbsHelpers;
import io.terminus.doctor.web.core.msg.email.CommonEmailServiceConfig;
import io.terminus.doctor.web.core.msg.sms.LuoSiMaoSmsServiceConfig;
import io.terminus.doctor.web.core.service.OtherSystemServiceConfig;
import io.terminus.parana.web.msg.config.db.DbAppPushConfig;
import io.terminus.parana.web.msg.config.db.DbEmailConfig;
import io.terminus.parana.web.msg.config.db.DbNotifyConfig;
import io.terminus.parana.web.msg.config.db.DbSmsConfig;
import io.terminus.parana.web.msg.config.gatewaybuilder.SimpleMsgGatewayBuilderConfig;
import io.terminus.parana.web.msg.config.test.TestAppPushWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestEmailWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestNotifyWebServiceConfig;
import io.terminus.parana.web.msg.config.test.TestSmsWebServiceConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Desc: 管理端配置
 * Mail: houly@terminus.io
 * Data: 下午4:23 16/5/31
 * Author: houly
 */
@Configuration
@Import({
        DoctorEventConfiguration.class,
        DoctorMsgConfig.class,
        DoctorUserConfiguration.class,
        DoctorCoreWebConfiguration.class,
        OtherSystemServiceConfig.class,
        SimpleMsgGatewayBuilderConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class,
        DbSmsConfig.class, TestSmsWebServiceConfig.class,
        DbNotifyConfig.class,TestNotifyWebServiceConfig.class,
        DbEmailConfig.class, TestEmailWebServiceConfig.class,
        DbAppPushConfig.class, TestAppPushWebServiceConfig.class
})
@ComponentScan(value = {
        "io.terminus.doctor.web.core.component",
        "io.terminus.doctor.web.admin.jobs",
        "io.terminus.doctor.web.admin.article",
        "io.terminus.doctor.web.admin.role",
        "io.terminus.doctor.web.admin.user"
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                DoctorHbsHelpers.class,
                ParanaHbsHelpers.class,
        })
})
@EnableWebMvc
@EnableAutoConfiguration
public class AdminWebConfiguration extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MockLoginInterceptor());
    }

}
