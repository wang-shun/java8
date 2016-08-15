package io.terminus.doctor.event.dao;

import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 12:19 16/8/11
 */

@Configuration
@EnableAutoConfiguration(exclude = DubboBaseAutoConfiguration.class)
@ComponentScan({"io.terminus.doctor.event.dao"})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class DaoConfiguration {
}
