package configuration.admin;

import io.terminus.doctor.interceptor.MockOperatorLoginInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

public class OperatorAdminWebConfiguration extends AdminWebConfiguration {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MockOperatorLoginInterceptor());
    }

}
