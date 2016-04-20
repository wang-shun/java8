/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.schedule;

import io.terminus.doctor.common.banner.DoctorBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-01
 */
@SpringBootApplication
public class DoctorScheduleApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DoctorScheduleApplication.class,
                "classpath:/spring/doctor-schedule-dubbo-consumer.xml");
        application.setBanner(new DoctorBanner());
        application.run(args);
    }
}
