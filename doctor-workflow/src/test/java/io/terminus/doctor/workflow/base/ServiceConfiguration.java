package io.terminus.doctor.workflow.base;

import io.terminus.boot.dubbo.autoconfigure.DubboAutoConfiguration;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Configuration
@EnableAutoConfiguration(exclude = DubboAutoConfiguration.class)
@ComponentScan({"io.terminus.doctor.workflow.service"})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class ServiceConfiguration {

}
