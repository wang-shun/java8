package io.terminus.doctor.warehouse;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.warehouse.handler.DoctorWareHouseHandlerChain;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.handler.consume.DoctorConsumerEventHandler;
import io.terminus.doctor.warehouse.handler.consume.DoctorInWareHouseConsumeHandler;
import io.terminus.doctor.warehouse.handler.consume.DoctorMaterialAvgConsumerHandler;
import io.terminus.doctor.warehouse.handler.consume.DoctorWareHouseTrackConsumeHandler;
import io.terminus.doctor.warehouse.handler.consume.DoctorWareHouseTypeConsumerHandler;
import io.terminus.doctor.warehouse.handler.provider.DoctorInWareHouseProviderHandler;
import io.terminus.doctor.warehouse.handler.provider.DoctorProviderEventHandler;
import io.terminus.doctor.warehouse.handler.provider.DoctorTrackProviderHandler;
import io.terminus.doctor.warehouse.handler.provider.DoctorTypeProviderHandler;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-04-22
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.warehouse"
})
@Import({DoctorCommonConfiguration.class})
public class DoctorWarehouseConfiguration {

    @Configuration
    @Profile("zookeeper")
    public static class ZookeeperConfiguration{

        @Bean
        public Subscriber cacheListenerBean(ZKClientFactory zkClientFactory,
                                            @Value("${zookeeper.zkTopic}") String zkTopic) throws Exception{
            return new Subscriber(zkClientFactory,zkTopic);
        }

        @Bean
        public Publisher cachePublisherBean(ZKClientFactory zkClientFactory,
                                            @Value("${zookeeper.zkTopic}}") String zkTopic) throws Exception{
            return new Publisher(zkClientFactory, zkTopic);
        }
    }

    @Bean
    public DoctorWareHouseHandlerChain doctorWareHouseHandlerChain(
            DoctorConsumerEventHandler doctorConsumerEventHandler,DoctorInWareHouseConsumeHandler doctorInWareHouseConsumeHandler,
            DoctorMaterialAvgConsumerHandler doctorMaterialAvgConsumerHandler, DoctorWareHouseTrackConsumeHandler doctorWareHouseTrackConsumeHandler,
            DoctorWareHouseTypeConsumerHandler doctorWareHouseTypeConsumerHandler,
            DoctorInWareHouseProviderHandler doctorInWareHouseProviderHandler, DoctorProviderEventHandler doctorProviderEventHandler,
            DoctorTrackProviderHandler doctorTrackProviderHandler, DoctorTypeProviderHandler doctorTypeProviderHandler){
        List<IHandler> iHandlers = Lists.newArrayList();

        // consumer
        iHandlers.add(doctorConsumerEventHandler);
        iHandlers.add(doctorInWareHouseConsumeHandler);
        iHandlers.add(doctorMaterialAvgConsumerHandler);
        iHandlers.add(doctorWareHouseTrackConsumeHandler);
        iHandlers.add(doctorWareHouseTypeConsumerHandler);

        // provider
        iHandlers.add(doctorProviderEventHandler);
        iHandlers.add(doctorInWareHouseProviderHandler);
        iHandlers.add(doctorTrackProviderHandler);
        iHandlers.add(doctorTypeProviderHandler);

        return new DoctorWareHouseHandlerChain(iHandlers);
    }

}
