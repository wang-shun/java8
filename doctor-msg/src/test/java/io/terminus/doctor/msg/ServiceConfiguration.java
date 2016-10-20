package io.terminus.doctor.msg;

import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.parana.msg.impl.MessageAutoConfig;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Desc: service configuration
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
@Configuration
@EnableAutoConfiguration(exclude = DubboBaseAutoConfiguration.class)
@ComponentScan({
        "io.terminus.doctor.msg.*"
})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
@Import({MessageAutoConfig.class, DoctorCommonConfiguration.class})
public class ServiceConfiguration {

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
