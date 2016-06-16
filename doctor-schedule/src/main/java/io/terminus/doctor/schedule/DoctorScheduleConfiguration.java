package io.terminus.doctor.schedule;

import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.msg.email.CommonEmailServiceConfig;
import io.terminus.doctor.web.core.msg.sms.LuoSiMaoSmsServiceConfig;
import io.terminus.doctor.workflow.WorkFlowJobConfiguration;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.parana.web.msg.config.MsgWebConfig;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
@EnableScheduling
@EnableWebMvc
@EnableAutoConfiguration
@ComponentScan(value = {"io.terminus.doctor.schedule"})
@Import({WorkFlowJobConfiguration.class,
        DoctorCoreWebConfiguration.class,
        MsgWebConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class
})
public class DoctorScheduleConfiguration extends WebMvcConfigurerAdapter {

    @Bean(autowire = Autowire.BY_NAME)
    public ConfigCenter configCenter() {
        return new ConfigCenter();
    }

}
