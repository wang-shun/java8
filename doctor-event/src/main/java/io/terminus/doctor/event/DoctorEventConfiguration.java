package io.terminus.doctor.event;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.event.editHandler.DoctorModifyGroupEventHandler;
import io.terminus.doctor.event.editHandler.DoctorModifyPigEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupEventHandlers;
import io.terminus.doctor.event.editHandler.group.DoctorModifyMoveInEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyFarrowEventHandler;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigEventHandlers;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.handler.DoctorGroupEventHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandler;
import io.terminus.doctor.event.handler.DoctorPigEventHandlers;
import io.terminus.doctor.event.handler.DoctorRollbackGroupEventHandler;
import io.terminus.doctor.event.handler.DoctorRollbackPigEventHandler;
import io.terminus.doctor.event.handler.boar.DoctorSemenHandler;
import io.terminus.doctor.event.handler.group.*;
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
import io.terminus.doctor.event.handler.usual.DoctorEntryHandler;
import io.terminus.doctor.event.handler.usual.DoctorRemovalHandler;
import io.terminus.doctor.event.handler.usual.DoctorVaccinationHandler;
import io.terminus.doctor.event.model.DoctorPig;
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
        //猪群事件回滚
        Map<Integer, DoctorRollbackGroupEventHandler> rollbackGroupEventHandlerMap = Maps.newHashMap();
        rollbackGroupEventHandlerMap.put(GroupEventType.CHANGE.getValue(), rollbackGroupChangeEventHandler);
        rollbackGroupEventHandlerMap.put(GroupEventType.DISEASE.getValue(), rollbackGroupDiseaseHandler);
        rollbackGroupEventHandlerMap.put(GroupEventType.LIVE_STOCK.getValue(), rollbackGroupLiveStockHandler);
        rollbackGroupEventHandlerMap.put(GroupEventType.MOVE_IN.getValue(), rollbackGroupMoveInEventHandler);
        rollbackGroupEventHandlerMap.put(GroupEventType.NEW.getValue(), rollbackGroupNewEventHandler);
        rollbackGroupEventHandlerMap.put(GroupEventType.TRANS_FARM.getValue(), rollbackGroupTransFarmHandler);
        rollbackGroupEventHandlerMap.put(GroupEventType.TRANS_GROUP.getValue(), rollbackGroupTransHandler);
        rollbackGroupEventHandlerMap.put(GroupEventType.TURN_SEED.getValue(), rollbackGroupTurnSeedHandler);
        rollbackGroupEventHandlerMap.put(GroupEventType.ANTIEPIDEMIC.getValue(), rollbackGroupVaccinHandler);
        //猪事件回滚
        Table<Integer, Integer, DoctorRollbackPigEventHandler> rollbackPigEventHandlerTable = HashBasedTable.create();
        rollbackPigEventHandlerTable.put(PigEvent.PIGLETS_CHG.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowPigletChangeEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.CHG_FARM.getKey(), DoctorPig.PigSex.BOAR.getKey(), rollbackBoarChgFarmEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.CHG_LOCATION.getKey(), DoctorPig.PigSex.BOAR.getKey(), rollbackBoarChgLocationEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.CONDITION.getKey(), DoctorPig.PigSex.BOAR.getKey(), rollbackBoarConditionEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.DISEASE.getKey(), DoctorPig.PigSex.BOAR.getKey(), rollbackBoarDiseaseEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.ENTRY.getKey(), DoctorPig.PigSex.BOAR.getKey(), rollbackBoarEntryEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.REMOVAL.getKey(), DoctorPig.PigSex.BOAR.getKey(), rollbackBoarRemovalEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.SEMEN.getKey(), DoctorPig.PigSex.BOAR.getKey(), rollbackBoarSemenEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.VACCINATION.getKey(), DoctorPig.PigSex.BOAR.getKey(), rollbackBoarVaccinationEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.CHG_FARM.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowChgFarmEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.CHG_LOCATION.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowChgLocationEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.CONDITION.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowConditionEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.DISEASE.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowDiseaseEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.ENTRY.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowEntryEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.FARROWING.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowFarrowHandler);
        rollbackPigEventHandlerTable.put(PigEvent.FOSTERS_BY.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowFosterByHandler);
        rollbackPigEventHandlerTable.put(PigEvent.FOSTERS.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowFosterHandler);
        rollbackPigEventHandlerTable.put(PigEvent.MATING.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowMatingEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.REMOVAL.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowRemovalEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.VACCINATION.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowVaccinationEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.WEAN.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowWeanHandler);
        rollbackPigEventHandlerTable.put(PigEvent.TO_MATING.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowToChgLocationEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.TO_FARROWING.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowToChgLocationEventHandler);
        rollbackPigEventHandlerTable.put(PigEvent.PREG_CHECK.getKey(), DoctorPig.PigSex.SOW.getKey(), rollbackSowPregCheckEventHandler);

        return new DoctorRollbackHandlerChain(rollbackGroupEventHandlerMap, rollbackPigEventHandlerTable);
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
            DoctorChgFarmHandler doctorChgFarmHandler,
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
    public DoctorModifyPigEventHandlers doctorModifyPigEventHandlers(DoctorModifyFarrowEventHandler modifyFarrowEventHandler) {
        Map<Integer, DoctorModifyPigEventHandler> modifyPigEventHandlerMap = Maps.newHashMap();
        modifyPigEventHandlerMap.put(PigEvent.FARROWING.getKey(), modifyFarrowEventHandler);
        DoctorModifyPigEventHandlers modifyPigEventHandlers = new DoctorModifyPigEventHandlers();
        modifyPigEventHandlers.setModifyPigEventHandlerMap(modifyPigEventHandlerMap);
        return modifyPigEventHandlers;
    }

    @Bean
    public DoctorModifyGroupEventHandlers doctorModifyGroupEventHandlers(DoctorModifyMoveInEventHandler modifyMoveInEventHandler) {
        Map<Integer, DoctorModifyGroupEventHandler> modifyGroupEventHandlerMap = Maps.newHashMap();
        modifyGroupEventHandlerMap.put(GroupEventType.MOVE_IN.getValue(), modifyMoveInEventHandler);
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
