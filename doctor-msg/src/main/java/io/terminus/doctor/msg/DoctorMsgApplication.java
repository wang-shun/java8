package io.terminus.doctor.msg;

import io.terminus.doctor.common.banner.DoctorBanner;
import io.terminus.parana.common.banner.ParanaBanner;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by zhanghecheng on 16/3/8.
 */
@SpringBootApplication
public class DoctorMsgApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DoctorMsgApplication.class,
                "classpath:/spring/doctor-msg-dubbo-provider.xml");
        YamlPropertiesFactoryBean yml = new YamlPropertiesFactoryBean();
        yml.setResources(new ClassPathResource("env/default.yml"));
        application.setDefaultProperties(yml.getObject());
        application.setBanner(new DoctorBanner());
        application.run(args);
    }
}
