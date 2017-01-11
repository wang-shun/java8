package configuration.open;

import io.terminus.doctor.basic.DoctorBasicConfiguration;
import io.terminus.doctor.event.DoctorEventConfiguration;
import io.terminus.doctor.msg.DoctorMsgConfig;
import io.terminus.doctor.open.DoctorOPConfiguration;
import io.terminus.doctor.user.DoctorUserConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Desc: open api 模块配置
 * Mail: houly@terminus.io
 * Data: 下午4:36 16/5/31
 * Author: houly
 */
@Configuration
@EnableWebMvc
@EnableAutoConfiguration
@Import({
        DoctorEventConfiguration.class,
        DoctorBasicConfiguration.class,
        DoctorMsgConfig.class,
        DoctorUserConfiguration.class,
        DoctorOPConfiguration.class
})
public class OpenWebConfiguration {

}
