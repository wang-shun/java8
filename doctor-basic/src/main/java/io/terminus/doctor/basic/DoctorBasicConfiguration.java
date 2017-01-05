package io.terminus.doctor.basic;

import io.terminus.doctor.common.DoctorCommonConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by yaoqijun.
 * Date:2016-04-22
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Configuration
@ComponentScan({"io.terminus.doctor.basic"})
@Import({DoctorCommonConfiguration.class})
public class DoctorBasicConfiguration {
}
