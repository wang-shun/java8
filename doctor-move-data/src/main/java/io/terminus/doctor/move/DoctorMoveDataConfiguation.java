package io.terminus.doctor.move;

import io.terminus.doctor.basic.DoctorBasicConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

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
@Import({DoctorBasicConfiguration.class})
public class DoctorMoveDataConfiguation {

}
