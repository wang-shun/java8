package io.terminus.doctor.msg;

import io.terminus.boot.dubbo.autoconfigure.DubboAutoConfiguration;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.parana.msg.impl.MessageAutoConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Desc: service configuration
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
@Configuration
@EnableAutoConfiguration(exclude = DubboAutoConfiguration.class)
@ComponentScan({
        "io.terminus.doctor.msg.*"
})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
@Import(MessageAutoConfig.class)
public class ServiceConfiguration {

}
