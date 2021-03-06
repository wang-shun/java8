package io.terminus.doctor.user;

import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.parana.article.impl.ArticleAutoConfig;
import io.terminus.parana.file.FileAutoConfig;
import io.terminus.parana.user.ExtraUserAutoConfig;
import io.terminus.parana.user.UserAutoConfig;
import io.terminus.zookeeper.common.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Effet
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.user"
})
@Import({UserAutoConfig.class, ExtraUserAutoConfig.class, FileAutoConfig.class, ArticleAutoConfig.class,
        DoctorCommonConfiguration.class})
public class DoctorUserConfiguration {

    @Configuration
    public static class ZookeeperConfiguration{

        @Bean
        public Subscriber cacheListenerBean(ZKClientFactory zkClientFactory,
                                            @Value("${zookeeper.cacheTopic}") String cacheTopic) throws Exception{
            return new Subscriber(zkClientFactory,cacheTopic);
        }

        @Bean
        public Publisher cachePublisherBean(ZKClientFactory zkClientFactory,
                                            @Value("${zookeeper.cacheTopic}") String cacheTopic) throws Exception{
            return new Publisher(zkClientFactory, cacheTopic);
        }
    }
}
