package io.terminus.doctor.workflow.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.doctor.workflow.dao.FlowDefinitionDao;
import io.terminus.doctor.workflow.dao.FlowDefinitionNodeDao;
import io.terminus.doctor.workflow.dao.FlowDefinitionNodeEventDao;
import io.terminus.doctor.workflow.dao.FlowHistoryInstanceDao;
import io.terminus.doctor.workflow.dao.FlowHistoryProcessDao;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowHistoryInstance;
import io.terminus.doctor.workflow.model.FlowHistoryProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by xiao on 16/8/23.
 */
@Component
@Slf4j
public class WorkflowCacheCenter {

    private FlowDefinitionDao flowDefinitionDao;
    private FlowDefinitionNodeDao flowDefinitionNodeDao;
    private FlowDefinitionNodeEventDao flowDefinitionNodeEventDao;
    private FlowHistoryProcessDao flowHistoryProcessDao;
    private FlowHistoryInstanceDao flowHistoryInstanceDao;

    @Autowired
    public WorkflowCacheCenter(FlowDefinitionDao flowDefinitionDao, FlowDefinitionNodeDao flowDefinitionNodeDao, FlowDefinitionNodeEventDao flowDefinitionNodeEventDao, FlowHistoryProcessDao flowHistoryProcessDao, FlowHistoryInstanceDao flowHistoryInstanceDao) {
        this.flowDefinitionDao = flowDefinitionDao;
        this.flowDefinitionNodeDao = flowDefinitionNodeDao;
        this.flowDefinitionNodeEventDao = flowDefinitionNodeEventDao;
        this.flowHistoryProcessDao = flowHistoryProcessDao;
        this.flowHistoryInstanceDao = flowHistoryInstanceDao;
    }

    private LoadingCache<Long, FlowDefinition> flowDefinitionLoadingCache;
    private LoadingCache<Long, FlowDefinitionNode> flowDefinitionNodeLoadingCache;
    private LoadingCache<Long, FlowDefinitionNodeEvent> flowDefinitionNodeEventLoadingCache;
    private LoadingCache<Long, FlowHistoryProcess> flowHistoryProcessLoadingCache;
    private LoadingCache<Long, FlowHistoryInstance> flowHistoryInstanceLoadingCache;

    @PostConstruct
    public void initCache(){
        flowDefinitionLoadingCache = CacheBuilder.newBuilder().build(new CacheLoader<Long, FlowDefinition>() {
            @Override
            public FlowDefinition load(Long flowDefinitionId) throws Exception {
                return flowDefinitionDao.findById(flowDefinitionId);
            }
        });

        flowDefinitionNodeLoadingCache  = CacheBuilder.newBuilder().build(new CacheLoader<Long, FlowDefinitionNode>() {
            @Override
            public FlowDefinitionNode load(Long flowDefinitionNodeId) throws Exception {
                return flowDefinitionNodeDao.findById(flowDefinitionNodeId);
            }
        });

        flowDefinitionNodeEventLoadingCache = CacheBuilder.newBuilder().build(new CacheLoader<Long, FlowDefinitionNodeEvent>() {
            @Override
            public FlowDefinitionNodeEvent load(Long flowDefinitionNodeEventId) throws Exception {
                return flowDefinitionNodeEventDao.findById(flowDefinitionNodeEventId);
            }
        });

        flowHistoryInstanceLoadingCache = CacheBuilder.newBuilder().build(new CacheLoader<Long, FlowHistoryInstance>() {
            @Override
            public FlowHistoryInstance load(Long flowHistoryInstanceId) throws Exception {
                return flowHistoryInstanceDao.findById(flowHistoryInstanceId);
            }
        });

        flowHistoryProcessLoadingCache = CacheBuilder.newBuilder().build(new CacheLoader<Long, FlowHistoryProcess>() {
            @Override
            public FlowHistoryProcess load(Long flowHistoryProcessId) throws Exception {
                return flowHistoryProcessDao.findById(flowHistoryProcessId);
            }
        });
    }


}
