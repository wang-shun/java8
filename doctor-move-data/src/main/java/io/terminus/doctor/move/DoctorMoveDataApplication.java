package io.terminus.doctor.move;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/26
 */
@SpringBootApplication
public class DoctorMoveDataApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DoctorMoveDataApplication.class);
        application.run(args);
    }
}
