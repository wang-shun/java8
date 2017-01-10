package io.terminus.doctor.move;

import io.terminus.doctor.basic.DoctorBasicConfiguration;
import io.terminus.doctor.event.DoctorEventConfiguration;
import io.terminus.doctor.move.sql.DoctorSqlFactory;
import io.terminus.doctor.msg.DoctorMsgConfig;
import io.terminus.doctor.user.DoctorUserConfiguration;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;

/**
 * Desc: 单例模式启动 move-data 配置
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Configuration
@ComponentScan(basePackages = {"io.terminus.doctor.move"})
@EnableWebMvc
@EnableAutoConfiguration
@Import({DoctorBasicConfiguration.class,
        DoctorEventConfiguration.class,
        DoctorUserConfiguration.class,
        DoctorMsgConfig.class
})
public class DoctorMoveDataConfiguation extends WebMvcConfigurerAdapter {

    @Bean
    public DoctorSqlFactory doctorSqlFactory() throws IOException {
        String path = "classpath*:/hbs/**/*.hbs";
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
            return new DoctorSqlFactory(resources);
        } catch (Exception e) {
            return new DoctorSqlFactory(null);
        }
    }
    @Configuration
    public static class ZookeeperConfiguration{

        @Bean
        public Subscriber cacheListenerBean(ZKClientFactory zkClientFactory,
                                            @Value("${zookeeper.zkTopic}") String cacheTopic) throws Exception{
            return new Subscriber(zkClientFactory,cacheTopic);
        }

        @Bean
        public Publisher cachePublisherBean(ZKClientFactory zkClientFactory,
                                            @Value("${zookeeper.zkTopic}") String cacheTopic) throws Exception{
            return new Publisher(zkClientFactory, cacheTopic);
        }
    }
}
