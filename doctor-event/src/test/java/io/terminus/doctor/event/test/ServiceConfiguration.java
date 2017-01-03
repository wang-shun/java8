package io.terminus.doctor.event.test;

import com.google.common.collect.Lists;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import io.terminus.boot.search.autoconfigure.ESSearchAutoConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.handler.DoctorEntryHandler;
import io.terminus.doctor.event.handler.DoctorEventHandlerChain;
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
import io.terminus.doctor.event.handler.usual.DoctorChgFarmHandler;
import io.terminus.doctor.event.handler.usual.DoctorChgLocationHandler;
import io.terminus.doctor.event.handler.usual.DoctorConditionHandler;
import io.terminus.doctor.event.handler.usual.DoctorDiseaseHandler;
import io.terminus.doctor.event.handler.usual.DoctorRemovalHandler;
import io.terminus.doctor.event.handler.usual.DoctorVaccinationHandler;
import io.terminus.doctor.event.search.barn.BarnSearchProperties;
import io.terminus.doctor.event.search.barn.BaseBarnQueryBuilder;
import io.terminus.doctor.event.search.barn.DefaultBarnQueryBuilder;
import io.terminus.doctor.event.search.barn.DefaultIndexedBarnFactory;
import io.terminus.doctor.event.search.barn.IndexedBarn;
import io.terminus.doctor.event.search.barn.IndexedBarnFactory;
import io.terminus.doctor.event.search.group.BaseGroupQueryBuilder;
import io.terminus.doctor.event.search.group.DefaultGroupQueryBuilder;
import io.terminus.doctor.event.search.group.DefaultIndexedGroupFactory;
import io.terminus.doctor.event.search.group.GroupSearchProperties;
import io.terminus.doctor.event.search.group.IndexedGroup;
import io.terminus.doctor.event.search.group.IndexedGroupFactory;
import io.terminus.doctor.event.search.pig.BasePigQueryBuilder;
import io.terminus.doctor.event.search.pig.DefaultIndexedPigFactory;
import io.terminus.doctor.event.search.pig.DefaultPigQueryBuilder;
import io.terminus.doctor.event.search.pig.IndexedPig;
import io.terminus.doctor.event.search.pig.IndexedPigFactory;
import io.terminus.doctor.event.search.pig.PigSearchProperties;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.search.core.ESClient;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Desc: 工作基础测试类配置类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Configuration
@EnableAutoConfiguration(exclude = {DubboBaseAutoConfiguration.class, ESSearchAutoConfiguration.class})
@Import({DoctorCommonConfiguration.class})
@ComponentScan({"io.terminus.doctor.event.*","io.terminus.doctor.workflow.*"})
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
    @Bean
    public DoctorEventHandlerChain doctorEventHandlerChain(
            DoctorSemenHandler doctorSemenHandler,DoctorEntryHandler doctorEntryHandler,
            DoctorChgFarmHandler doctorChgFarmHandler, DoctorChgLocationHandler doctorChgLocationHandler,
            DoctorConditionHandler doctorConditionHandler, DoctorDiseaseHandler doctorDiseaseHandler,
            DoctorRemovalHandler doctorRemovalHandler, DoctorVaccinationHandler doctorVaccinationHandler){
        DoctorEventHandlerChain chain = new DoctorEventHandlerChain();
        chain.setDoctorEventCreateHandlers(Lists.newArrayList(
                doctorSemenHandler,doctorEntryHandler,
                doctorChgFarmHandler, doctorChgLocationHandler,
                doctorConditionHandler, doctorDiseaseHandler,
                doctorRemovalHandler, doctorVaccinationHandler));
        return chain;
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

    @Configuration
    @ConditionalOnClass(ESClient.class)
    @ComponentScan({"io.terminus.search.api"})
    public static class SearchConfiguration {

        @Bean
        public ESClient esClient(@Value("${search.host:localhost}") String host,
                                 @Value("${search.port:9200}") Integer port) {
            return new ESClient(host, port);
        }

        @Configuration
        @EnableConfigurationProperties(PigSearchProperties.class)
        protected static class PigSearchConfiguration {
            @Bean
            @ConditionalOnMissingBean(IndexedPigFactory.class)
            public IndexedPigFactory<? extends IndexedPig> indexedPigFactory(DoctorPigDao doctorPigDao) {
                return new DefaultIndexedPigFactory(doctorPigDao);
            }

            @Bean
            @ConditionalOnMissingBean(BasePigQueryBuilder.class)
            public BasePigQueryBuilder pigQueryBuilder() {
                return new DefaultPigQueryBuilder();
            }
        }

        @Configuration
        @EnableConfigurationProperties(GroupSearchProperties.class)
        protected static class GroupSearchConfiguration {
            @Bean
            @ConditionalOnMissingBean(IndexedGroupFactory.class)
            public IndexedGroupFactory<? extends IndexedGroup> indexedGroupFactory() {
                return new DefaultIndexedGroupFactory();
            }

            @Bean
            @ConditionalOnMissingBean(BaseGroupQueryBuilder.class)
            public BaseGroupQueryBuilder groupQueryBuilder() {
                return new DefaultGroupQueryBuilder();
            }
        }

        @Configuration
        @EnableConfigurationProperties(BarnSearchProperties.class)
        protected static class BarnSearchConfiguration {
            @Bean
            @ConditionalOnMissingBean(IndexedBarnFactory.class)
            public IndexedBarnFactory<? extends IndexedBarn> indexedBarnFactory(DoctorBarnReadService doctorBarnReadService) {
                return new DefaultIndexedBarnFactory(doctorBarnReadService);
            }

            @Bean
            @ConditionalOnMissingBean(BaseBarnQueryBuilder.class)
            public BaseBarnQueryBuilder BarnQueryBuilder() {
                return new DefaultBarnQueryBuilder();
            }
        }
    }
}
