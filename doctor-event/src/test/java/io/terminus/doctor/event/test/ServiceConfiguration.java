package io.terminus.doctor.event.test;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import io.terminus.boot.mybatis.autoconfigure.MybatisAutoConfiguration;
import io.terminus.boot.rpc.dubbo.config.DubboBaseAutoConfiguration;
import io.terminus.boot.search.autoconfigure.ESSearchAutoConfiguration;
import io.terminus.doctor.common.DoctorCommonConfiguration;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.handler.DoctorEntryHandler;
import io.terminus.doctor.event.handler.DoctorEventCreateHandler;
import io.terminus.doctor.event.handler.DoctorEventHandlerChain;
import io.terminus.doctor.event.handler.boar.DoctorSemenHandler;
import io.terminus.doctor.event.handler.rollback.DoctorRollbackHandlerChain;
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
import io.terminus.doctor.event.handler.usual.DoctorChgFarmHandler;
import io.terminus.doctor.event.handler.usual.DoctorChgLocationHandler;
import io.terminus.doctor.event.handler.usual.DoctorConditionHandler;
import io.terminus.doctor.event.handler.usual.DoctorDiseaseHandler;
import io.terminus.doctor.event.handler.usual.DoctorRemovalHandler;
import io.terminus.doctor.event.handler.usual.DoctorVaccinationHandler;
import io.terminus.doctor.event.report.DoctorDailyPigCountChain;
import io.terminus.doctor.event.report.count.DoctorDailyEntryEventCount;
import io.terminus.doctor.event.report.count.DoctorDailyFarrowingEventCount;
import io.terminus.doctor.event.report.count.DoctorDailyMatingEventCount;
import io.terminus.doctor.event.report.count.DoctorDailyPregEventCount;
import io.terminus.doctor.event.report.count.DoctorDailyRemovalEventCount;
import io.terminus.doctor.event.report.count.DoctorDailyWeanEventCount;
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
import io.terminus.doctor.workflow.DoctorWorkflowConfiguration;
import io.terminus.search.core.ESClient;
import io.terminus.zookeeper.ZKClientFactory;
import io.terminus.zookeeper.pubsub.Publisher;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.Executors;

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
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ServiceConfiguration {

    /**
     * 事件回滚拦截器链
     */
    @Bean
    public DoctorRollbackHandlerChain doctorRollbackHandlerChain(
            DoctorRollbackGroupChangeHandler rollbackGroupChangeEventHandler,
            DoctorRollbackGroupDiseaseHandler rollbackGroupDiseaseHandler,
            DoctorRollbackGroupLiveStockHandler rollbackGroupLiveStockHandler,
            DoctorRollbackGroupMoveInHandler rollbackGroupMoveInEventHandler,
            DoctorRollbackGroupNewHandler rollbackGroupNewEventHandler,
            DoctorRollbackGroupTransFarmHandler rollbackGroupTransFarmHandler,
            DoctorRollbackGroupTransHandler rollbackGroupTransHandler,
            DoctorRollbackGroupTurnSeedHandler rollbackGroupTurnSeedHandler,
            DoctorRollbackGroupVaccinHandler rollbackGroupVaccinHandler,
            DoctorRollbackBoarVaccinationEventHandler doctorRollbackBoarVaccinationEventHandler
    ) {
        DoctorRollbackHandlerChain chain = new DoctorRollbackHandlerChain();
        chain.setRollbackGroupEventHandlers(Lists.newArrayList(
                rollbackGroupChangeEventHandler,
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
                doctorRollbackBoarVaccinationEventHandler
        ));
        return chain;
    }

    /**
     * 对应handler chain
     * @return
     */
    @Bean
    public DoctorEventHandlerChain doctorEventHandlerChain(
            DoctorSemenHandler doctorSemenHandler,DoctorEntryHandler doctorEntryHandler,
            DoctorChgFarmHandler doctorChgFarmHandler, DoctorChgLocationHandler doctorChgLocationHandler,
            DoctorConditionHandler doctorConditionHandler, DoctorDiseaseHandler doctorDiseaseHandler,
            DoctorRemovalHandler doctorRemovalHandler, DoctorVaccinationHandler doctorVaccinationHandler){
        DoctorEventHandlerChain chain = new DoctorEventHandlerChain();
        List<DoctorEventCreateHandler> list = Lists.newArrayList(
                doctorSemenHandler,doctorEntryHandler,
                doctorChgFarmHandler, doctorChgLocationHandler,
                doctorConditionHandler, doctorDiseaseHandler,
                doctorRemovalHandler, doctorVaccinationHandler);
        chain.setDoctorEventCreateHandlers(Lists.newArrayList(list));
        return chain;
    }

    @Bean
    public DoctorDailyPigCountChain doctorDailyPigCountChain(DoctorDailyEntryEventCount doctorDailyEntryEventCount,
                                                             DoctorDailyFarrowingEventCount doctorDailyFarrowingEventCount,
                                                             DoctorDailyMatingEventCount doctorDailyMatingEventCount,
                                                             DoctorDailyPregEventCount doctorDailyPregEventCount,
                                                             DoctorDailyRemovalEventCount doctorDailyRemovalEventCount,
                                                             DoctorDailyWeanEventCount doctorDailyWeanEventCount){
        List<DoctorDailyEventCount> doctorDailyEventCounts = Lists.newArrayList(
                doctorDailyEntryEventCount, doctorDailyFarrowingEventCount, doctorDailyMatingEventCount,
                doctorDailyPregEventCount,doctorDailyRemovalEventCount, doctorDailyWeanEventCount);
        return new DoctorDailyPigCountChain(doctorDailyEventCounts);
    }

    @Bean
    public EventBus eventBus(){
        return new AsyncEventBus(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    @Configuration
    @ConditionalOnBean(ZKClientFactory.class)
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
