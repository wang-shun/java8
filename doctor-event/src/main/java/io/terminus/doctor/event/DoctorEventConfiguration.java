package io.terminus.doctor.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.DoctorEntryHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandlers;
import io.terminus.doctor.event.handler.boar.DoctorSemenHandler;
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
import io.terminus.doctor.event.handler.sow.DoctorSowFarrowingHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowFostersByHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowFostersHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowMatingHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowPigletsChgHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowPregCheckHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowWeanHandler;
import io.terminus.doctor.event.handler.usual.DoctorChgFarmHandler;
import io.terminus.doctor.event.handler.usual.DoctorChgLocationHandler;
import io.terminus.doctor.event.handler.usual.DoctorConditionHandler;
import io.terminus.doctor.event.handler.usual.DoctorDiseaseHandler;
import io.terminus.doctor.event.handler.usual.DoctorRemovalHandler;
import io.terminus.doctor.event.handler.usual.DoctorVaccinationHandler;
import io.terminus.doctor.workflow.DoctorWorkflowConfiguration;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-04-22
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Configuration
@ComponentScan(basePackages = {
        "io.terminus.doctor.event",
})
@Import({DoctorWorkflowConfiguration.class, DoctorCommonConfiguration.class})
@AutoConfigureAfter({MybatisAutoConfiguration.class})
public class  DoctorEventConfiguration {

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
     * 对应event handler
     */
    @Bean
    public DoctorPigEventHandlers doctorPigEventHandlers(
            DoctorEntryHandler doctorEntryHandler,
            DoctorSemenHandler doctorSemenHandler, DoctorSowFarrowingHandler doctorSowFarrowingHandler,
            DoctorSowFostersByHandler doctorSowFostersByHandler, DoctorSowFostersHandler doctorSowFostersHandler,
            DoctorSowMatingHandler doctorSowMatingHandler, DoctorSowPigletsChgHandler doctorSowPigletsChgHandler,
            DoctorSowPregCheckHandler doctorSowPregCheckHandler, DoctorSowWeanHandler doctorSowWeanHandler,
            DoctorChgFarmHandler doctorChgFarmHandler, DoctorChgLocationHandler doctorChgLocationHandler,
            DoctorConditionHandler doctorConditionHandler, DoctorDiseaseHandler doctorDiseaseHandler,
            DoctorRemovalHandler doctorRemovalHandler, DoctorVaccinationHandler doctorVaccinationHandler
    ) {
        Map<Integer, DoctorPigEventHandler> eventHandlerMap = Maps.newHashMap();
        eventHandlerMap.put(PigEvent.ENTRY.getKey(), doctorEntryHandler);
        eventHandlerMap.put(PigEvent.SEMEN.getKey(), doctorSemenHandler);
        eventHandlerMap.put(PigEvent.FARROWING.getKey(), doctorSowFarrowingHandler);
        eventHandlerMap.put(PigEvent.FOSTERS_BY.getKey(), doctorSowFostersByHandler);
        eventHandlerMap.put(PigEvent.FOSTERS.getKey(), doctorSowFostersHandler);
        eventHandlerMap.put(PigEvent.MATING.getKey(), doctorSowMatingHandler);
        eventHandlerMap.put(PigEvent.PIGLETS_CHG.getKey(), doctorSowPigletsChgHandler);
        eventHandlerMap.put(PigEvent.PREG_CHECK.getKey(), doctorSowPregCheckHandler);
        eventHandlerMap.put(PigEvent.WEAN.getKey(), doctorSowWeanHandler);
        eventHandlerMap.put(PigEvent.CHG_FARM.getKey(), doctorChgFarmHandler);
        eventHandlerMap.put(PigEvent.CHG_LOCATION.getKey(), doctorChgLocationHandler);
        eventHandlerMap.put(PigEvent.CONDITION.getKey(), doctorConditionHandler);
        eventHandlerMap.put(PigEvent.DISEASE.getKey(), doctorDiseaseHandler);
        eventHandlerMap.put(PigEvent.REMOVAL.getKey(), doctorRemovalHandler);
        eventHandlerMap.put(PigEvent.VACCINATION.getKey(), doctorVaccinationHandler);

        DoctorPigEventHandlers doctorEventHandlers = new DoctorPigEventHandlers();
        doctorEventHandlers.setEventHandlerMap(eventHandlerMap);
        return doctorEventHandlers;
    }

    @Configuration
    public static class ZookeeperConfiguration{

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
}
