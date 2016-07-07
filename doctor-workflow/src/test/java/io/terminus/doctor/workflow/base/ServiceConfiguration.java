package io.terminus.doctor.workflow.base;

import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Desc: 工作基础测试类配置类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Configuration
@EnableAutoConfiguration(exclude = DubboBaseAutoConfiguration.class)
@ComponentScan({"io.terminus.doctor.workflow.*"})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class ServiceConfiguration {

}
