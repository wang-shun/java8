package io.terminus.doctor.config;


import io.terminus.doctor.common.banner.DoctorBanner;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;


/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-11 12:52 PM  <br>
 * Author: xiao
 */

@SpringBootApplication
public class DoctorConfigApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DoctorConfigApplication.class,
                "classpath:/spring/doctor-config-dubbo-provider.xml");
        YamlPropertiesFactoryBean yml = new YamlPropertiesFactoryBean();
        yml.setResources(new ClassPathResource("env/default.yml"));
        application.setDefaultProperties(yml.getObject());
        application.setBanner(new DoctorBanner());
        application.run(args);
    }
}
