package io.terminus.doctor.event;

import com.google.common.collect.Maps;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.event.editHandler.DoctorModifyGroupEventHandler;
import io.terminus.doctor.event.editHandler.DoctorModifyPigEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupAntiepidemicEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupChangeEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupCloseEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupDiseaseEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupEventHandlers;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupLiveStockEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupMoveInEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupNewEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupTransFarmEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupTransGroupEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupTurnSeedEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupWeanEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigChgFarmEventV2Handler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigChgFarmInEventV2Handler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigChgLocationEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigConditionEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigDiseaseEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigEntryEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigEventHandlers;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigFarrowEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigFosterByEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigFosterEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigMatingEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigPigletsChgEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigPregCheckEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigRemoveEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigSemenEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigVaccinEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigWeanEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.DoctorGroupEventHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandlers;
import io.terminus.doctor.event.handler.boar.DoctorSemenHandler;
import io.terminus.doctor.event.handler.group.DoctorAntiepidemicGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorChangeGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorCloseGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorDiseaseGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorGroupEventHandlers;
import io.terminus.doctor.event.handler.group.DoctorLiveStockGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorMoveInGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorNewGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransFarmGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTurnSeedGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorWeanGroupEventHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowFarrowingHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowFostersByHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowFostersHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowMatingHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowPigletsChgHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowPregCheckHandler;
import io.terminus.doctor.event.handler.sow.DoctorSowWeanHandler;
import io.terminus.doctor.event.handler.usual.DoctorChgFarmInV2Handler;
import io.terminus.doctor.event.handler.usual.DoctorChgFarmV2Handler;
import io.terminus.doctor.event.handler.usual.DoctorChgLocationHandler;
import io.terminus.doctor.event.handler.usual.DoctorConditionHandler;
import io.terminus.doctor.event.handler.usual.DoctorDiseaseHandler;
import io.terminus.doctor.event.handler.usual.DoctorEntryHandler;
import io.terminus.doctor.event.handler.usual.DoctorRemovalHandler;
import io.terminus.doctor.event.handler.usual.DoctorVaccinationHandler;
import io.terminus.parana.msg.impl.MessageAutoConfig;
import io.terminus.zookeeper.common.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

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
@Import({MessageAutoConfig.class, DoctorCommonConfiguration.class})
@AutoConfigureAfter({MybatisAutoConfiguration.class})
public class  DoctorEventConfiguration {

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
     * 对应event handler
     */
    @Bean
    public DoctorPigEventHandlers doctorPigEventHandlers(
            DoctorEntryHandler doctorEntryHandler,
            DoctorSemenHandler doctorSemenHandler,
            DoctorSowFostersByHandler doctorSowFostersByHandler,
            DoctorSowMatingHandler doctorSowMatingHandler,
            DoctorSowPregCheckHandler doctorSowPregCheckHandler,
            DoctorChgFarmV2Handler doctorChgFarmHandler,
            DoctorChgFarmInV2Handler doctorChgFarmInHandler,
            DoctorChgLocationHandler doctorChgLocationHandler,
            DoctorConditionHandler doctorConditionHandler,
            DoctorDiseaseHandler doctorDiseaseHandler,
            DoctorRemovalHandler doctorRemovalHandler,
            DoctorVaccinationHandler doctorVaccinationHandler,
            DoctorSowWeanHandler doctorSowWeanHandler,
            DoctorSowFostersHandler doctorSowFostersHandler,
            DoctorSowFarrowingHandler doctorSowFarrowingHandler,
            DoctorSowPigletsChgHandler doctorSowPigletsChgHandler
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
        eventHandlerMap.put(PigEvent.CHG_FARM_IN.getKey(), doctorChgFarmInHandler);
        eventHandlerMap.put(PigEvent.CHG_LOCATION.getKey(), doctorChgLocationHandler);
        eventHandlerMap.put(PigEvent.TO_FARROWING.getKey(), doctorChgLocationHandler);
        eventHandlerMap.put(PigEvent.TO_MATING.getKey(), doctorChgLocationHandler);
        eventHandlerMap.put(PigEvent.CONDITION.getKey(), doctorConditionHandler);
        eventHandlerMap.put(PigEvent.DISEASE.getKey(), doctorDiseaseHandler);
        eventHandlerMap.put(PigEvent.REMOVAL.getKey(), doctorRemovalHandler);
        eventHandlerMap.put(PigEvent.VACCINATION.getKey(), doctorVaccinationHandler);

        DoctorPigEventHandlers doctorEventHandlers = new DoctorPigEventHandlers();
        doctorEventHandlers.setEventHandlerMap(eventHandlerMap);
        return doctorEventHandlers;
    }

    @Bean
    public DoctorGroupEventHandlers doctorGroupEventHandlers(DoctorAntiepidemicGroupEventHandler doctorAntiepidemicGroupEventHandler,
                                                             DoctorChangeGroupEventHandler doctorChangeGroupEventHandler,
                                                             DoctorCloseGroupEventHandler doctorCloseGroupEventHandler,
                                                             DoctorDiseaseGroupEventHandler doctorDiseaseGroupEventHandler,
                                                             DoctorLiveStockGroupEventHandler doctorLiveStockGroupEventHandler,
                                                             DoctorMoveInGroupEventHandler doctorMoveInGroupEventHandler,
                                                             DoctorTransFarmGroupEventHandler doctorTransFarmGroupEventHandler,
                                                             DoctorTransGroupEventHandler doctorTransGroupEventHandler,
                                                             DoctorTurnSeedGroupEventHandler doctorTurnSeedGroupEventHandler,
                                                             DoctorWeanGroupEventHandler doctorWeanGroupEventHandler,
                                                             DoctorNewGroupEventHandler doctorNewGroupEventHandler){
        Map<Integer, DoctorGroupEventHandler> handlerMap = Maps.newHashMap();
        handlerMap.put(GroupEventType.NEW.getValue(), doctorNewGroupEventHandler);
        handlerMap.put(GroupEventType.ANTIEPIDEMIC.getValue(),doctorAntiepidemicGroupEventHandler);
        handlerMap.put(GroupEventType.CHANGE.getValue(),doctorChangeGroupEventHandler);
        handlerMap.put(GroupEventType.CLOSE.getValue(),doctorCloseGroupEventHandler);
        handlerMap.put(GroupEventType.DISEASE.getValue(),doctorDiseaseGroupEventHandler);
        handlerMap.put(GroupEventType.LIVE_STOCK.getValue(), doctorLiveStockGroupEventHandler);
        handlerMap.put(GroupEventType.MOVE_IN.getValue(), doctorMoveInGroupEventHandler);
        handlerMap.put(GroupEventType.TRANS_FARM.getValue(), doctorTransFarmGroupEventHandler);
        handlerMap.put(GroupEventType.TRANS_GROUP.getValue(), doctorTransGroupEventHandler);
        handlerMap.put(GroupEventType.TURN_SEED.getValue(), doctorTurnSeedGroupEventHandler);
        handlerMap.put(GroupEventType.WEAN.getValue(), doctorWeanGroupEventHandler);
        DoctorGroupEventHandlers doctorGroupEventHandlers = new DoctorGroupEventHandlers();
        doctorGroupEventHandlers.setEventHandlerMap(handlerMap);
        return doctorGroupEventHandlers;
    }

    @Bean
    public DoctorModifyPigEventHandlers doctorModifyPigEventHandlers(
            DoctorModifyPigChgFarmEventV2Handler modifyPigChgFarmEventHandler,
            DoctorModifyPigChgFarmInEventV2Handler modifyPigChgFarmInEventHandler,
            DoctorModifyPigChgLocationEventHandler modifyPigChgLocationEventHandler,
            DoctorModifyPigConditionEventHandler modifyPigConditionEventHandler,
            DoctorModifyPigDiseaseEventHandler modifyPigDiseaseEventHandler,
            DoctorModifyPigEntryEventHandler modifyPigEntryEventHandler,
            DoctorModifyPigFarrowEventHandler modifyFarrowEventHandler,
            DoctorModifyPigFosterByEventHandler modifyPigFosterByEventHandler,
            DoctorModifyPigFosterEventHandler modifyPigFosterEventHandler,
            DoctorModifyPigMatingEventHandler modifyPigMatingEventHandler,
            DoctorModifyPigPigletsChgEventHandler modifyPigPigletsChgHandler,
            DoctorModifyPigPregCheckEventHandler modifyPigPregCheckEventHandler,
            DoctorModifyPigRemoveEventHandler modifyPigRemoveEventHandler,
            DoctorModifyPigSemenEventHandler modifyPigSemenEventHandler,
            DoctorModifyPigVaccinEventHandler modifyPigVaccinEventHandler,
            DoctorModifyPigWeanEventHandler modifyPigWeanEventHandler) {
        Map<Integer, DoctorModifyPigEventHandler> modifyPigEventHandlerMap = Maps.newHashMap();
        modifyPigEventHandlerMap.put(PigEvent.CHG_FARM.getKey(), modifyPigChgFarmEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.CHG_FARM_IN.getKey(), modifyPigChgFarmInEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.CHG_LOCATION.getKey(), modifyPigChgLocationEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.TO_FARROWING.getKey(), modifyPigChgLocationEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.TO_MATING.getKey(), modifyPigChgLocationEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.CONDITION.getKey(), modifyPigConditionEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.DISEASE.getKey(), modifyPigDiseaseEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.ENTRY.getKey(), modifyPigEntryEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.FARROWING.getKey(), modifyFarrowEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.FOSTERS_BY.getKey(), modifyPigFosterByEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.FOSTERS.getKey(), modifyPigFosterEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.MATING.getKey(), modifyPigMatingEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.PIGLETS_CHG.getKey(), modifyPigPigletsChgHandler);
        modifyPigEventHandlerMap.put(PigEvent.PREG_CHECK.getKey(), modifyPigPregCheckEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.REMOVAL.getKey(), modifyPigRemoveEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.SEMEN.getKey(), modifyPigSemenEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.VACCINATION.getKey(), modifyPigVaccinEventHandler);
        modifyPigEventHandlerMap.put(PigEvent.WEAN.getKey(), modifyPigWeanEventHandler);
        DoctorModifyPigEventHandlers modifyPigEventHandlers = new DoctorModifyPigEventHandlers();
        modifyPigEventHandlers.setModifyPigEventHandlerMap(modifyPigEventHandlerMap);
        return modifyPigEventHandlers;
    }

    @Bean
    public DoctorModifyGroupEventHandlers doctorModifyGroupEventHandlers(
            DoctorModifyGroupAntiepidemicEventHandler modifyGroupAntiepidemicEventHandler,
            DoctorModifyGroupChangeEventHandler modifyGroupChangeEventHandler,
            DoctorModifyGroupCloseEventHandler modifyGroupCloseEventHandler,
            DoctorModifyGroupDiseaseEventHandler modifyGroupDiseaseEventHandler,
            DoctorModifyGroupLiveStockEventHandler modifyGroupLiveStockEventHandler,
            DoctorModifyGroupMoveInEventHandler modifyMoveInEventHandler,
            DoctorModifyGroupNewEventHandler modifyGroupNewEventHandler,
            DoctorModifyGroupTransFarmEventHandler modifyGroupTransFarmEventHandler,
            DoctorModifyGroupTransGroupEventHandler modifyGroupTransGroupEventHandler,
            DoctorModifyGroupTurnSeedEventHandler modifyGroupTurnSeedEventHandler,
            DoctorModifyGroupWeanEventHandler modifyGroupWeanEventHandler) {
        Map<Integer, DoctorModifyGroupEventHandler> modifyGroupEventHandlerMap = Maps.newHashMap();
        modifyGroupEventHandlerMap.put(GroupEventType.ANTIEPIDEMIC.getValue(), modifyGroupAntiepidemicEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.CHANGE.getValue(), modifyGroupChangeEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.CLOSE.getValue(), modifyGroupCloseEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.DISEASE.getValue(), modifyGroupDiseaseEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.LIVE_STOCK.getValue(), modifyGroupLiveStockEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.MOVE_IN.getValue(), modifyMoveInEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.NEW.getValue(), modifyGroupNewEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.TRANS_FARM.getValue(), modifyGroupTransFarmEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.TRANS_GROUP.getValue(), modifyGroupTransGroupEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.TURN_SEED.getValue(), modifyGroupTurnSeedEventHandler);
        modifyGroupEventHandlerMap.put(GroupEventType.WEAN.getValue(), modifyGroupWeanEventHandler);
        DoctorModifyGroupEventHandlers modifyGroupEventHandlers = new DoctorModifyGroupEventHandlers();
        modifyGroupEventHandlers.setModifyGroupEventHandlerMap(modifyGroupEventHandlerMap);
        return modifyGroupEventHandlers;
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
