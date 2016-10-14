package io.terminus.doctor.msg;

import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.parana.msg.impl.MessageAutoConfig;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by zhanghecheng on 16/3/8.
 */
@Configuration
@ComponentScan({"io.terminus.doctor.msg"})
@Import({MessageAutoConfig.class, DoctorCommonConfiguration.class})
public class DoctorMsgConfig {

    @Bean
    public Subscriber cacheListenerBean(ZKClientFactory zkClientFactory,
                                        @Value("${zookeeper.zkTopic}") String zkTopic) throws Exception{
        return new Subscriber(zkClientFactory,zkTopic);
    }

    @Bean
    public Publisher cachePublisherBean(ZKClientFactory zkClientFactory,
                                        @Value("${zookeeper.zkTopic}") String zkTopic) throws Exception{
        return new Publisher(zkClientFactory, zkTopic);
    }
}
