package configuration.front;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import io.terminus.doctor.basic.DoctorBasicConfiguration;
import io.terminus.doctor.converter.JsonMessageConverter;
import io.terminus.doctor.event.DoctorEventConfiguration;
import io.terminus.doctor.interceptor.MockLoginInterceptor;
import io.terminus.doctor.msg.DoctorMsgConfig;
import io.terminus.doctor.user.DoctorUserConfiguration;
import io.terminus.doctor.warehouse.DoctorWarehouseConfiguration;
import io.terminus.doctor.web.core.DoctorCoreWebConfiguration;
import io.terminus.doctor.web.core.component.DoctorHbsHelpers;
import io.terminus.doctor.web.core.component.ParanaHbsHelpers;
import io.terminus.doctor.web.core.msg.email.CommonEmailServiceConfig;
import io.terminus.doctor.web.core.msg.sms.LuoSiMaoSmsServiceConfig;
import io.terminus.doctor.web.core.service.OtherSystemServiceConfig;
import io.terminus.pampas.openplatform.core.Gateway;
import io.terminus.parana.auth.core.AuthenticationConfiguration;
import io.terminus.parana.auth.web.WebAuthenticationConfiguration;
import io.terminus.parana.web.msg.config.MsgWebConfig;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.Filter;
import java.util.List;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-30 11:12 AM  <br>
 * Author: xiao
 */
@Configuration
@Import({
        DoctorEventConfiguration.class,
        DoctorBasicConfiguration.class,
        DoctorMsgConfig.class,
        DoctorUserConfiguration.class,
        DoctorWarehouseConfiguration.class,
        DoctorCoreWebConfiguration.class,
        OtherSystemServiceConfig.class,
        WebAuthenticationConfiguration.class,
        MsgWebConfig.class,
        LuoSiMaoSmsServiceConfig.class,
        CommonEmailServiceConfig.class
})
@ComponentScan(value = {
        "io.terminus.doctor.web.core.component",
        "io.terminus.doctor.web.front",
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                DoctorHbsHelpers.class,
                ParanaHbsHelpers.class,
                Gateway.class
        }),
        @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = {"io.terminus.pampas.openplatform.*",
        "io.terminus.pampas.openplatform..*", "io.terminus.doctor.open..*"})

})
@EnableWebMvc
@EnableAutoConfiguration(excludeName = {"io.terminus.pampas.openplatform.core.OpenPlatformAutoConfiguration"})
public class FrontWebConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MockLoginInterceptor());
    }

    @Bean
    public Filter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
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
        converters.add(new JsonMessageConverter());
    }
}
