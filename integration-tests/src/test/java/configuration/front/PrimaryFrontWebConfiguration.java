package configuration.front;

import io.terminus.doctor.interceptor.MockPrimaryLoginInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * 陈增辉 on 16/6/7.
 * 将测试时的用户配置为主账号
 */
public class PrimaryFrontWebConfiguration extends FrontWebConfiguration{
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MockPrimaryLoginInterceptor());
    }
}
