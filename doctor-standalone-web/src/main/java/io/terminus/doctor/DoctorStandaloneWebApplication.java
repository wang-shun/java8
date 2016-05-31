package io.terminus.doctor;

import io.terminus.doctor.common.banner.DoctorBanner;
import io.terminus.parana.common.banner.ParanaBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author houly
 */
@SpringBootApplication
public class DoctorStandaloneWebApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DoctorStandaloneWebApplication.class);
        application.setBanner(new DoctorBanner());
        application.run(args);
    }
}
