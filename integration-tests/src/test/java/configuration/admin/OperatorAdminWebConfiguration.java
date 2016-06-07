package configuration.admin;

import io.terminus.doctor.event.DoctorEventConfiguration;
import io.terminus.doctor.interceptor.MockLoginInterceptor;
import io.terminus.doctor.interceptor.MockOperatorLoginInterceptor;
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

public class OperatorAdminWebConfiguration extends AdminWebConfiguration {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MockOperatorLoginInterceptor());
    }

}
