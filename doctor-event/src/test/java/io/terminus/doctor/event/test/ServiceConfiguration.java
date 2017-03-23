package io.terminus.doctor.event.test;

import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.zookeeper.common.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Desc: 工作基础测试类配置类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Configuration
@EnableAutoConfiguration(exclude = {DubboBaseAutoConfiguration.class})
@Import({DoctorCommonConfiguration.class})
@ComponentScan({"io.terminus.doctor.event.*"})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class ServiceConfiguration {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setCacheSeconds(3600);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }



    /**
     * 对应handler chain
     */
//    @Bean
//    public DoctorEventHandlerChain doctorEventHandlerChain(
//            DoctorSemenHandler doctorSemenHandler, DoctorEntryHandler doctorEntryHandler,
//            DoctorChgFarmHandler doctorChgFarmHandler, DoctorChgLocationHandler doctorChgLocationHandler,
//            DoctorConditionHandler doctorConditionHandler, DoctorDiseaseHandler doctorDiseaseHandler,
//            DoctorRemovalHandler doctorRemovalHandler, DoctorVaccinationHandler doctorVaccinationHandler){
//        DoctorEventHandlerChain chain = new DoctorEventHandlerChain();
//        chain.setDoctorEventCreateHandlers(Lists.newArrayList(
//                doctorSemenHandler,doctorEntryHandler,
//                doctorChgFarmHandler, doctorChgLocationHandler,
//                doctorConditionHandler, doctorDiseaseHandler,
//                doctorRemovalHandler, doctorVaccinationHandler));
//        return chain;
//    }
//    @Bean
//    public DoctorPigEventHandlers doctorEventHandlerChain(
//            DoctorSemenHandler doctorSemenHandler,DoctorEntryHandler doctorEntryHandler,
//            DoctorChgFarmHandler doctorChgFarmHandler, DoctorChgLocationHandler doctorChgLocationHandler,
//            DoctorConditionHandler doctorConditionHandler, DoctorDiseaseHandler doctorDiseaseHandler,
//            DoctorRemovalHandler doctorRemovalHandler, DoctorVaccinationHandler doctorVaccinationHandler){
//        DoctorPigEventHandlers chain = new DoctorPigEventHandlers();
//        List<DoctorPigEventHandler> list = Lists.newArrayList(
//                doctorSemenHandler,doctorEntryHandler,
//                doctorChgFarmHandler, doctorChgLocationHandler,
//                doctorConditionHandler, doctorDiseaseHandler,
//                doctorRemovalHandler, doctorVaccinationHandler);
//        chain.setEventHandlerMap(Lists.newArrayList(list));
//        return chain;
//    }

        @ConditionalOnBean(ZKClientFactory.class)
        @Profile("zookeeper")
        public static class ZookeeperConfiguration {

            @Bean
            public Subscriber cacheListenerBean(ZKClientFactory zkClientFactory,
                                                @Value("${zookeeper.zkTopic}") String zkTopic) throws Exception {
                return new Subscriber(zkClientFactory, zkTopic);
            }

            @Bean
            public Publisher cachePublisherBean(ZKClientFactory zkClientFactory,
                                                @Value("${zookeeper.zkTopic}}") String zkTopic) throws Exception {
                return new Publisher(zkClientFactory, zkTopic);
            }
        }

}
