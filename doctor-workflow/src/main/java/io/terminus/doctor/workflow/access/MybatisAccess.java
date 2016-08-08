package io.terminus.doctor.workflow.access;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.dao.FlowDefinitionDao;
import io.terminus.doctor.workflow.dao.FlowDefinitionNodeDao;
import io.terminus.doctor.workflow.dao.FlowDefinitionNodeEventDao;
import io.terminus.doctor.workflow.dao.FlowHistoryInstanceDao;
import io.terminus.doctor.workflow.dao.FlowHistoryProcessDao;
import io.terminus.doctor.workflow.dao.FlowInstanceDao;
import io.terminus.doctor.workflow.dao.FlowProcessDao;
import io.terminus.doctor.workflow.dao.FlowProcessTrackDao;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowHistoryInstance;
import io.terminus.doctor.workflow.model.FlowHistoryProcess;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.model.FlowProcessTrack;
import io.terminus.doctor.workflow.utils.AssertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc: 工作流数据库层统一访问入口, MyBatis实现
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Service
@RpcProvider
public class MybatisAccess implements JdbcAccess {

    @Autowired
    private FlowDefinitionDao flowDefinitionDao;

    @Autowired
    private FlowDefinitionNodeDao flowDefinitionNodeDao;

    @Autowired
    private FlowDefinitionNodeEventDao flowDefinitionNodeEventDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowProcessDao flowProcessDao;

    @Autowired
    private FlowProcessTrackDao flowProcessTrackDao;

    @Autowired
    private FlowHistoryInstanceDao flowHistoryInstanceDao;

    @Autowired
    private FlowHistoryProcessDao flowHistoryProcessDao;

    /******************* 流程定义相关 ********************************************/
    @Override
    public void createFlowDefinition(FlowDefinition flowDefinition) {
        flowDefinitionDao.create(flowDefinition);
    }

    @Override
    public void deleteFlowDefinition(Long flowDefinitionId) {
        FlowDefinition flowDefinition = flowDefinitionDao.findById(flowDefinitionId);
        if(flowDefinition != null) {
            flowDefinition.setStatus(FlowDefinition.Status.DELETE.value());
            flowDefinitionDao.update(flowDefinition);
        }
    }

    @Override
    public void deleteFlowDefinition(List<Long> flowDefinitionIds) {
        flowDefinitionDao.deletes(flowDefinitionIds);
    }

    @Override
    public List<FlowDefinition> findFlowDefinitions(FlowDefinition flowDefinition) {
        return flowDefinitionDao.list(flowDefinition);
    }

    @Override
    public List<FlowDefinition> findFlowDefinitions(Map criteria) {
        return flowDefinitionDao.list(criteria);
    }

    @Override
    public FlowDefinition findFlowDefinitionSingle(FlowDefinition flowDefinition) {
        List<FlowDefinition> flowDefinitions = findFlowDefinitions(flowDefinition);
        if(flowDefinitions != null && flowDefinitions.size() > 1) {
            AssertHelper.throwException("查询唯一流程定义的数量大于 1, 当前数量为:{}", flowDefinitions.size());
        }
        if(flowDefinitions != null && flowDefinitions.size() == 1) {
            return flowDefinitions.get(0);
        }
        return null;
    }

    @Override
    public FlowDefinition findFlowDefinitionSingle(Map criteria) {
        List<FlowDefinition> flowDefinitions = findFlowDefinitions(criteria);
        if(flowDefinitions != null && flowDefinitions.size() > 1) {
            AssertHelper.throwException("查询唯一流程定义的数量大于 1, 当前数量为:{}", flowDefinitions.size());
        }
        if(flowDefinitions != null && flowDefinitions.size() == 1) {
            return flowDefinitions.get(0);
        }
        return null;
    }

    @Override
    public Paging<FlowDefinition> findFlowDefinitionsPaging(Map criteria, Integer offset, Integer limit) {
        return flowDefinitionDao.paging(offset, limit, criteria);
    }

    @Override
    public long findFlowDefinitionsSize(Map criteria) {
        return flowDefinitionDao.count(criteria);
    }

    /******************** 流程定义 节点 相关 *************************************/
    @Override
    public void createFlowDefinitionNode(FlowDefinitionNode flowDefinitionNode) {
        flowDefinitionNodeDao.create(flowDefinitionNode);
    }

    @Override
    public void deleteFlowDefinitionNode(Long flowDefinitionNodeId) {
        flowDefinitionNodeDao.delete(flowDefinitionNodeId);
    }

    @Override
    public void deleteFlowDefinitionNode(List<Long> flowDefinitionNodeIds) {
        flowDefinitionNodeDao.deletes(flowDefinitionNodeIds);
    }

    @Override
    public List<FlowDefinitionNode> findFlowDefinitionNodes(FlowDefinitionNode flowDefinitionNode) {
        return flowDefinitionNodeDao.list(flowDefinitionNode);
    }

    @Override
    public List<FlowDefinitionNode> findFlowDefinitionNodes(Map criteria) {
        return flowDefinitionNodeDao.list(criteria);
    }

    @Override
    public FlowDefinitionNode findFlowDefinitionNodeSingle(FlowDefinitionNode flowDefinitionNode) {
        List<FlowDefinitionNode> flowDefinitionNodes = findFlowDefinitionNodes(flowDefinitionNode);
        if(flowDefinitionNodes != null && flowDefinitionNodes.size() > 1) {
            AssertHelper.throwException("查询唯一流程节点的数量大于 1, 当前数量为:{}", flowDefinitionNodes.size());
        }
        if(flowDefinitionNodes != null && flowDefinitionNodes.size() == 1) {
            return flowDefinitionNodes.get(0);
        }
        return null;
    }

    @Override
    public FlowDefinitionNode findFlowDefinitionNodeSingle(Map criteria) {
        List<FlowDefinitionNode> flowDefinitionNodes = findFlowDefinitionNodes(criteria);
        if(flowDefinitionNodes != null && flowDefinitionNodes.size() > 1) {
            AssertHelper.throwException("查询唯一流程节点的数量大于 1, 当前数量为:{}", flowDefinitionNodes.size());
        }
        if(flowDefinitionNodes != null && flowDefinitionNodes.size() == 1) {
            return flowDefinitionNodes.get(0);
        }
        return null;
    }

    @Override
    public Paging<FlowDefinitionNode> findFlowDefinitionNodesPaging(Map criteria, Integer offset, Integer limit) {
        return flowDefinitionNodeDao.paging(offset, limit, criteria);
    }

    @Override
    public long findFlowDefinitionNodesSize(Map criteria) {
        return flowDefinitionNodeDao.count(criteria);
    }


    /******************** 流程定义节点 连接事件 相关 *******************************/
    @Override
    public void createFlowDefinitionNodeEvent(FlowDefinitionNodeEvent flowDefinitionNodeEvent) {
        flowDefinitionNodeEventDao.create(flowDefinitionNodeEvent);
    }

    @Override
    public void deleteFlowDefinitionNodeEvent(Long flowDefinitionNodeEventId) {
        flowDefinitionNodeEventDao.delete(flowDefinitionNodeEventId);
    }

    @Override
    public void deleteFlowDefinitionNodeEvent(List<Long> flowDefinitionNodeEventIds) {
        flowDefinitionNodeEventDao.deletes(flowDefinitionNodeEventIds);
    }

    @Override
    public List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(FlowDefinitionNodeEvent flowDefinitionNodeEvent) {
        return flowDefinitionNodeEventDao.list(flowDefinitionNodeEvent);
    }

    @Override
    public List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(Map criteria) {
        return flowDefinitionNodeEventDao.list(criteria);
    }

    @Override
    public FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(FlowDefinitionNodeEvent flowDefinitionNodeEvent) {
        List<FlowDefinitionNodeEvent> flowDefinitionNodeEvents = findFlowDefinitionNodeEvents(flowDefinitionNodeEvent);
        if(flowDefinitionNodeEvents != null && flowDefinitionNodeEvents.size() > 1) {
            AssertHelper.throwException("查询唯一流程节点事件连线的数量大于 1, 当前数量为:{}", flowDefinitionNodeEvents.size());
        }
        if(flowDefinitionNodeEvents != null && flowDefinitionNodeEvents.size() == 1) {
            return flowDefinitionNodeEvents.get(0);
        }
        return null;
    }

    @Override
    public FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(Map criteria) {
        List<FlowDefinitionNodeEvent> flowDefinitionNodeEvents = findFlowDefinitionNodeEvents(criteria);
        if(flowDefinitionNodeEvents != null && flowDefinitionNodeEvents.size() > 1) {
            AssertHelper.throwException("查询唯一流程节点事件连线的数量大于 1, 当前数量为:{}", flowDefinitionNodeEvents.size());
        }
        if(flowDefinitionNodeEvents != null && flowDefinitionNodeEvents.size() == 1) {
            return flowDefinitionNodeEvents.get(0);
        }
        return null;
    }

    @Override
    public Paging<FlowDefinitionNodeEvent> findFlowDefinitionNodeEventsPaging(Map criteria, Integer offset, Integer limit) {
        return flowDefinitionNodeEventDao.paging(offset, limit, criteria);
    }

    @Override
    public long findFlowDefinitionNodeEventsSize(Map criteria) {
        return flowDefinitionNodeEventDao.count(criteria);
    }


    /******************** 流程实例 相关 *******************************/
    @Override
    public Long createFlowInstance(FlowInstance flowInstance) {
        flowInstanceDao.create(flowInstance);
        return flowInstance.getId();
    }

    @Override
    public void updateFlowInstance(FlowInstance flowInstance) {
        flowInstanceDao.update(flowInstance);
    }

    @Override
    public void deleteFlowInstance(Long flowInstanceId) {
        flowInstanceDao.delete(flowInstanceId);
    }

    @Override
    public void deleteFlowInstance(List<Long> flowInstanceIds) {
        flowInstanceDao.deletes(flowInstanceIds);
    }

    @Override
    public List<FlowInstance> findFlowInstances(FlowInstance flowInstance) {
        return flowInstanceDao.list(flowInstance);
    }

    @Override
    public List<FlowInstance> findFlowInstances(Map criteria) {
        return flowInstanceDao.list(criteria);
    }

    @Override
    public FlowInstance findFlowInstanceSingle(FlowInstance flowInstance) {
        List<FlowInstance> flowInstances = findFlowInstances(flowInstance);
        if(flowInstances != null && flowInstances.size() > 1) {
            AssertHelper.throwException("查询唯一流程实例的数量大于 1, 当前数量为:{}", flowInstances.size());
        }
        if(flowInstances != null && flowInstances.size() == 1) {
            return flowInstances.get(0);
        }
        return null;
    }

    @Override
    public FlowInstance findFlowInstanceSingle(Map criteria) {
        List<FlowInstance> flowInstances = findFlowInstances(criteria);
        if(flowInstances != null && flowInstances.size() > 1) {
            AssertHelper.throwException("查询唯一流程实例的数量大于 1, 当前数量为:{}", flowInstances.size());
        }
        if(flowInstances != null && flowInstances.size() == 1) {
            return flowInstances.get(0);
        }
        return null;
    }

    @Override
    public Paging<FlowInstance> findFlowInstancesPaging(Map criteria, Integer offset, Integer limit) {
        return flowInstanceDao.paging(offset, limit, criteria);
    }

    @Override
    public long findFlowInstancesSize(Map criteria) {
        return flowInstanceDao.count(criteria);
    }

    /******************** 流程活动节点 相关 *******************************/
    @Override
    public Long createFlowProcess(FlowProcess flowProcess) {
        flowProcessDao.create(flowProcess);
        return flowProcess.getId();
    }

    @Override
    public void updateFlowProcess(FlowProcess flowProcess) {
        flowProcessDao.update(flowProcess);
    }

    @Override
    public void deleteFlowProcess(Long flowProcessId) {
        flowProcessDao.delete(flowProcessId);
    }

    @Override
    public void deleteFlowProcess(List<Long> flowProcessIds) {
        flowProcessDao.deletes(flowProcessIds);
    }

    @Override
    public List<FlowProcess> findFlowProcesses(FlowProcess flowProcess) {
        return flowProcessDao.list(flowProcess);
    }

    @Override
    public List<FlowProcess> findFlowProcesses(Map criteria) {
        return flowProcessDao.list(criteria);
    }

    @Override
    public FlowProcess findFlowProcessSingle(FlowProcess flowProcess) {
        List<FlowProcess> flowProcesses = findFlowProcesses(flowProcess);
        if(flowProcesses != null && flowProcesses.size() > 1) {
            AssertHelper.throwException("查询唯一流程活动节点的数量大于 1, 当前数量为:{}", flowProcesses.size());
        }
        if(flowProcesses != null && flowProcesses.size() == 1) {
            return flowProcesses.get(0);
        }
        return null;
    }

    @Override
    public FlowProcess findFlowProcessSingle(Map criteria) {
        List<FlowProcess> flowProcesses = findFlowProcesses(criteria);
        if(flowProcesses != null && flowProcesses.size() > 1) {
            AssertHelper.throwException("查询唯一流程活动节点的数量大于 1, 当前数量为:{}", flowProcesses.size());
        }
        if(flowProcesses != null && flowProcesses.size() == 1) {
            return flowProcesses.get(0);
        }
        return null;
    }

    @Override
    public Paging<FlowProcess> findFlowProcessesPaging(Map criteria, Integer offset, Integer limit) {
        return flowProcessDao.paging(offset, limit, criteria);
    }

    @Override
    public long findFlowProcessesSize(Map criteria) {
        return flowProcessDao.count(criteria);
    }


    /******************** 流程活动节点追踪 相关 *******************************/
    @Override
    public Long createFlowProcessTrack(FlowProcessTrack flowProcessTrack) {
        flowProcessTrackDao.create(flowProcessTrack);
        return flowProcessTrack.getId();
    }

    @Override
    public void deleteFlowProcessTrack(Long flowProcessTrackId) {
        flowProcessTrackDao.delete(flowProcessTrackId);
    }

    @Override
    public void deleteFlowProcessTrack(List<Long> flowProcessTrackIds) {
        flowProcessTrackDao.deletes(flowProcessTrackIds);
    }

    @Override
    public void updateFlowProcessTrack(FlowProcessTrack flowProcessTrack) { flowProcessTrackDao.update(flowProcessTrack); }

    @Override
    public List<FlowProcessTrack> findFlowProcessTracks(FlowProcessTrack flowProcessTrack) {
        return flowProcessTrackDao.list(flowProcessTrack);
    }

    @Override
    public List<FlowProcessTrack> findFlowProcessTracks(Map criteria) {
        return flowProcessTrackDao.list(criteria);
    }

    @Override
    public FlowProcessTrack findFlowProcessTrackSingle(FlowProcessTrack flowProcessTrack) {
        List<FlowProcessTrack> flowProcessTracks = findFlowProcessTracks(flowProcessTrack);
        if(flowProcessTracks != null && flowProcessTracks.size() > 1) {
            AssertHelper.throwException("查询唯一流程活动节点跟踪的数量大于 1, 当前数量为:{}", flowProcessTracks.size());
        }
        if(flowProcessTracks != null && flowProcessTracks.size() == 1) {
            return flowProcessTracks.get(0);
        }
        return null;
    }

    @Override
    public FlowProcessTrack findFlowProcessTrackSingle(Map criteria) {
        List<FlowProcessTrack> flowProcessTracks = findFlowProcessTracks(criteria);
        if(flowProcessTracks != null && flowProcessTracks.size() > 1) {
            AssertHelper.throwException("查询唯一流程活动节点跟踪的数量大于 1, 当前数量为:{}", flowProcessTracks.size());
        }
        if(flowProcessTracks != null && flowProcessTracks.size() == 1) {
            return flowProcessTracks.get(0);
        }
        return null;
    }

    @Override
    public Paging<FlowProcessTrack> findFlowProcessTracksPaging(Map criteria, Integer offset, Integer limit) {
        return flowProcessTrackDao.paging(offset, limit, criteria);
    }

    @Override
    public long findFlowProcessTracksSize(Map criteria) {
        return flowProcessTrackDao.count(criteria);
    }


    /******************** 流程实例历史 相关 *******************************/
    @Override
    public void createFlowHistoryInstance(FlowHistoryInstance flowHistoryInstance) {
        flowHistoryInstanceDao.create(flowHistoryInstance);
    }

    @Override
    public List<FlowHistoryInstance> findFlowHistoryInstances(FlowHistoryInstance flowHistoryInstance) {
        return flowHistoryInstanceDao.list(flowHistoryInstance);
    }

    @Override
    public List<FlowHistoryInstance> findFlowHistoryInstances(Map criteria) {
        return flowHistoryInstanceDao.list(criteria);
    }

    @Override
    public FlowHistoryInstance findFlowHistoryInstanceSingle(FlowHistoryInstance flowHistoryInstance) {
        List<FlowHistoryInstance> flowHistoryInstances = findFlowHistoryInstances(flowHistoryInstance);
        if(flowHistoryInstances != null && flowHistoryInstances.size() > 1) {
            AssertHelper.throwException("查询唯一历史流程实例的数量大于 1, 当前数量为:{}", flowHistoryInstances.size());
        }
        if(flowHistoryInstances != null && flowHistoryInstances.size() == 1) {
            return flowHistoryInstances.get(0);
        }
        return null;
    }

    @Override
    public FlowHistoryInstance findFlowHistoryInstanceSingle(Map criteria) {
        List<FlowHistoryInstance> flowHistoryInstances = findFlowHistoryInstances(criteria);
        if(flowHistoryInstances != null && flowHistoryInstances.size() > 1) {
            AssertHelper.throwException("查询唯一历史流程实例的数量大于 1, 当前数量为:{}", flowHistoryInstances.size());
        }
        if(flowHistoryInstances != null && flowHistoryInstances.size() == 1) {
            return flowHistoryInstances.get(0);
        }
        return null;
    }

    @Override
    public Paging<FlowHistoryInstance> findFlowHistoryInstancesPaging(Map criteria, Integer offset, Integer limit) {
        return flowHistoryInstanceDao.paging(offset, limit, criteria);
    }

    @Override
    public long findFlowHistoryInstancesSize(Map criteria) {
        return flowHistoryInstanceDao.count(criteria);
    }


    /******************** 流程活动节点历史 相关 *******************************/
    @Override
    public Long createFlowHistoryProcess(FlowHistoryProcess flowHistoryProcess) {
        flowHistoryProcessDao.create(flowHistoryProcess);
        return flowHistoryProcess.getId();
    }

    @Override
    public List<FlowHistoryProcess> findFlowHistoryProcesses(FlowHistoryProcess flowHistoryProcess) {
        return flowHistoryProcessDao.list(flowHistoryProcess);
    }

    @Override
    public List<FlowHistoryProcess> findFlowHistoryProcesses(Map criteria) {
        return flowHistoryProcessDao.list(criteria);
    }

    @Override
    public FlowHistoryProcess findFlowHistoryProcessSingle(FlowHistoryProcess flowHistoryProcess) {
        List<FlowHistoryProcess> flowHistoryProcesses = findFlowHistoryProcesses(flowHistoryProcess);
        if(flowHistoryProcesses != null && flowHistoryProcesses.size() > 1) {
            AssertHelper.throwException("查询唯一历史流程活动节点的数量大于 1, 当前数量为:{}", flowHistoryProcesses.size());
        }
        if(flowHistoryProcesses != null && flowHistoryProcesses.size() == 1) {
            return flowHistoryProcesses.get(0);
        }
        return null;
    }

    @Override
    public FlowHistoryProcess findFlowHistoryProcessSingle(Map criteria) {
        List<FlowHistoryProcess> flowHistoryProcesses = findFlowHistoryProcesses(criteria);
        if(flowHistoryProcesses != null && flowHistoryProcesses.size() > 1) {
            AssertHelper.throwException("查询唯一历史流程活动节点的数量大于 1, 当前数量为:{}", flowHistoryProcesses.size());
        }
        if(flowHistoryProcesses != null && flowHistoryProcesses.size() == 1) {
            return flowHistoryProcesses.get(0);
        }
        return null;
    }

    @Override
    public Paging<FlowHistoryProcess> findFlowHistoryProcessesPaging(Map criteria, Integer offset, Integer limit) {
        return flowHistoryProcessDao.paging(offset, limit, criteria);
    }

    @Override
    public long findFlowHistoryProcessesSize(Map criteria) {
        return flowHistoryProcessDao.count(criteria);
    }
}
