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
import io.terminus.parana.auth.core.AuthenticationConfiguration;
import io.terminus.parana.web.msg.config.MsgWebConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
        AuthenticationConfiguration.class,
        DoctorEventConfiguration.class,
        DoctorMsgConfig.class,
        DoctorUserConfiguration.class,
        DoctorCoreWebConfiguration.class,
        OtherSystemServiceConfig.class,
        MsgWebConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class,
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
@AutoConfigureAfter(AuthenticationConfiguration.class)
public class AdminWebConfiguration extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MockLoginInterceptor());
    }

}
