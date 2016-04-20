package io.terminus.doctor.config;

import io.terminus.parana.config.impl.ConfigAutoConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-11 12:52 PM  <br>
 * Author: xiao
 */
@Configuration
@Import({ConfigAutoConfig.class})
public class DoctorConfigConfiguration {

}
