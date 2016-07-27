package io.terminus.doctor.move;

import io.terminus.doctor.basic.DoctorBasicConfiguration;
import io.terminus.doctor.event.DoctorEventConfiguration;
import io.terminus.doctor.user.DoctorUserConfiguration;
import io.terminus.doctor.warehouse.DoctorWarehouseConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Desc: 单例模式启动 move-data 配置
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Configuration
@ComponentScan(basePackages = {"io.terminus.doctor.move"})
@EnableWebMvc
@EnableAutoConfiguration
@Import({DoctorBasicConfiguration.class,
        DoctorEventConfiguration.class,
        DoctorWarehouseConfiguration.class, DoctorUserConfiguration.class
})
public class DoctorMoveDataConfiguation extends WebMvcConfigurerAdapter {
}
