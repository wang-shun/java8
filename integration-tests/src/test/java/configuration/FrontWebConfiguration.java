package configuration;

import io.terminus.doctor.interceptor.MockLoginInterceptor;
import io.terminus.parana.user.UserAutoConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-30 11:12 AM  <br>
 * Author: xiao
 */
@Configuration
@Import({
        UserAutoConfig.class
})

@ComponentScan({
        "io.terminus.doctor.web.core.trade"
})
public class FrontWebConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MockLoginInterceptor());
    }
}
