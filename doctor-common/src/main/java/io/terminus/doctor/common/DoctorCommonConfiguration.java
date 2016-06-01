package io.terminus.doctor.common;

import io.terminus.doctor.common.validate.DoctorServiceInvokeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/1
 */
@Configuration
public class DoctorCommonConfiguration {

    @Bean
    public DoctorServiceInvokeValidator doctorServiceInvokeValidator() {
        return new DoctorServiceInvokeValidator();
    }
}
