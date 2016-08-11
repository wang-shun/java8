package io.terminus.doctor;

import io.terminus.doctor.basic.DoctorBasicConfiguration;
import io.terminus.doctor.common.banner.DoctorBanner;
import io.terminus.doctor.config.DoctorConfigConfiguration;
import io.terminus.doctor.event.DoctorEventConfiguration;
import io.terminus.doctor.msg.DoctorMsgConfig;
import io.terminus.doctor.schedule.DoctorScheduleConfiguration;
import io.terminus.doctor.user.DoctorUserConfiguration;
import io.terminus.doctor.warehouse.DoctorWarehouseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * @author houly
 */
@ComponentScan({
        "io.terminus.doctor.schedule"
})
@Import({
        DoctorConfigConfiguration.class,
        DoctorEventConfiguration.class,
        DoctorWarehouseConfiguration.class,
        DoctorBasicConfiguration.class,
        DoctorMsgConfig.class,
        DoctorUserConfiguration.class,
        DoctorScheduleConfiguration.class
})
@SpringBootApplication
public class DoctorStandaloneScheduleApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DoctorStandaloneScheduleApplication.class);
        application.setBanner(new DoctorBanner());
        application.run(args);
    }
}
