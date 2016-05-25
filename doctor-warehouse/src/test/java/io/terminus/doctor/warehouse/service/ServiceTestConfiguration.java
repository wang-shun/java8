package io.terminus.doctor.warehouse.service;

import io.terminus.boot.dubbo.autoconfigure.DubboAutoConfiguration;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: Service 信息配置工具类
 */
@Configuration
@EnableAutoConfiguration(exclude = {DubboAutoConfiguration.class})
@ComponentScan("io.terminus.doctor.warehouse.*")
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class ServiceTestConfiguration {

}
