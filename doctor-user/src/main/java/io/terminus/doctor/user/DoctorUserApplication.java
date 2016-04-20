package io.terminus.doctor.user;

import io.terminus.doctor.common.banner.DoctorBanner;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Effet
 */
@SpringBootApplication
public class DoctorUserApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DoctorUserApplication.class,
                "classpath:/spring/doctor-user-dubbo-provider.xml");
        YamlPropertiesFactoryBean yml = new YamlPropertiesFactoryBean();
        yml.setResources(new ClassPathResource("env/default.yml"));
        application.setDefaultProperties(yml.getObject());
        application.setBanner(new DoctorBanner());
        application.run(args);
    }
}
