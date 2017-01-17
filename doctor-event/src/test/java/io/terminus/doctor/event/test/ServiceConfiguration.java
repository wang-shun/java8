package io.terminus.doctor.event.test;

import com.google.common.collect.Lists;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.event.handler.rollback.DoctorRollbackHandlerChain;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarChgFarmEventHandler;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarChgLocationEventHandler;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarConditionEventHandler;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarDiseaseEventHandler;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarEntryEventHandler;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarRemovalEventHandler;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarSemenEventHandler;
import io.terminus.doctor.event.handler.rollback.boar.DoctorRollbackBoarVaccinationEventHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupChangeHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupDiseaseHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupLiveStockHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupMoveInHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupNewHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupTransFarmHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupTransHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupTurnSeedHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupVaccinHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowChgFarmEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowChgLocationEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowConditionEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowDiseaseEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowEntryEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowFarrowHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowFosterByHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowFosterHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowMatingEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowPigletChangeEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowPregCheckEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowRemovalEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowToChgLocationEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowVaccinationEventHandler;
import io.terminus.doctor.event.handler.rollback.sow.DoctorRollbackSowWeanHandler;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

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

    /**
     * 事件回滚拦截器链
     */
    @Bean
    public DoctorRollbackHandlerChain doctorRollbackHandlerChain(
            DoctorRollbackSowPigletChangeEventHandler rollbackSowPigletChangeEventHandler,
            DoctorRollbackSowToChgLocationEventHandler rollbackSowToChgLocationEventHandler,
            DoctorRollbackGroupChangeHandler rollbackGroupChangeEventHandler,
            DoctorRollbackGroupDiseaseHandler rollbackGroupDiseaseHandler,
            DoctorRollbackGroupLiveStockHandler rollbackGroupLiveStockHandler,
            DoctorRollbackGroupMoveInHandler rollbackGroupMoveInEventHandler,
            DoctorRollbackGroupNewHandler rollbackGroupNewEventHandler,
            DoctorRollbackGroupTransFarmHandler rollbackGroupTransFarmHandler,
            DoctorRollbackGroupTransHandler rollbackGroupTransHandler,
            DoctorRollbackGroupTurnSeedHandler rollbackGroupTurnSeedHandler,
            DoctorRollbackGroupVaccinHandler rollbackGroupVaccinHandler,
            DoctorRollbackBoarChgFarmEventHandler rollbackBoarChgFarmEventHandler,
            DoctorRollbackBoarChgLocationEventHandler rollbackBoarChgLocationEventHandler,
            DoctorRollbackBoarConditionEventHandler rollbackBoarConditionEventHandler,
            DoctorRollbackBoarDiseaseEventHandler rollbackBoarDiseaseEventHandler,
            DoctorRollbackBoarEntryEventHandler rollbackBoarEntryEventHandler,
            DoctorRollbackBoarRemovalEventHandler rollbackBoarRemovalEventHandler,
            DoctorRollbackBoarSemenEventHandler rollbackBoarSemenEventHandler,
            DoctorRollbackBoarVaccinationEventHandler rollbackBoarVaccinationEventHandler,
            DoctorRollbackSowChgFarmEventHandler rollbackSowChgFarmEventHandler,
            DoctorRollbackSowChgLocationEventHandler rollbackSowChgLocationEventHandler,
            DoctorRollbackSowConditionEventHandler rollbackSowConditionEventHandler,
            DoctorRollbackSowDiseaseEventHandler rollbackSowDiseaseEventHandler,
            DoctorRollbackSowEntryEventHandler rollbackSowEntryEventHandler,
            DoctorRollbackSowFarrowHandler rollbackSowFarrowHandler,
            DoctorRollbackSowFosterByHandler rollbackSowFosterByHandler,
            DoctorRollbackSowFosterHandler rollbackSowFosterHandler,
            DoctorRollbackSowMatingEventHandler rollbackSowMatingEventHandler,
            DoctorRollbackSowRemovalEventHandler rollbackSowRemovalEventHandler,
            DoctorRollbackSowVaccinationEventHandler rollbackSowVaccinationEventHandler,
            DoctorRollbackSowWeanHandler rollbackSowWeanHandler,
            DoctorRollbackSowPregCheckEventHandler rollbackSowPregCheckEventHandler
    ) {
        DoctorRollbackHandlerChain chain = new DoctorRollbackHandlerChain();
        chain.setRollbackGroupEventHandlers(Lists.newArrayList(rollbackGroupChangeEventHandler,
                rollbackGroupDiseaseHandler,
                rollbackGroupLiveStockHandler,
                rollbackGroupMoveInEventHandler,
                rollbackGroupNewEventHandler,
                rollbackGroupTransFarmHandler,
                rollbackGroupTransHandler,
                rollbackGroupTurnSeedHandler,
                rollbackGroupVaccinHandler
        ));

        chain.setRollbackPigEventHandlers(Lists.newArrayList(
                rollbackSowPigletChangeEventHandler,
                rollbackBoarChgFarmEventHandler,
                rollbackBoarChgLocationEventHandler,
                rollbackBoarConditionEventHandler,
                rollbackBoarDiseaseEventHandler,
                rollbackBoarEntryEventHandler,
                rollbackBoarRemovalEventHandler,
                rollbackBoarSemenEventHandler,
                rollbackBoarVaccinationEventHandler,
                rollbackSowChgFarmEventHandler,
                rollbackSowChgLocationEventHandler,
                rollbackSowConditionEventHandler,
                rollbackSowDiseaseEventHandler,
                rollbackSowEntryEventHandler,
                rollbackSowFarrowHandler,
                rollbackSowFosterByHandler,
                rollbackSowFosterHandler,
                rollbackSowMatingEventHandler,
                rollbackSowRemovalEventHandler,
                rollbackSowVaccinationEventHandler,
                rollbackSowWeanHandler,
                rollbackSowToChgLocationEventHandler,
                rollbackSowPregCheckEventHandler
        ));
        return chain;
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
