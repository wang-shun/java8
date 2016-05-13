package io.terminus.doctor.workflow.access;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.dao.FlowDefinitionDao;
import io.terminus.doctor.workflow.dao.FlowDefinitionNodeDao;
import io.terminus.doctor.workflow.dao.FlowDefinitionNodeEventDao;
import io.terminus.doctor.workflow.dao.FlowInstanceDao;
import io.terminus.doctor.workflow.dao.FlowProcessDao;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.AssertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Desc: 工作流数据库层统一访问入口, MyBatis实现
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Repository
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

    /******************* 流程定义相关 ********************************************/
    @Override
    public void createFlowDefinition(FlowDefinition flowDefinition) {
        flowDefinitionDao.create(flowDefinition);
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
    public void createFlowInstance(FlowInstance flowInstance) {
        flowInstanceDao.create(flowInstance);
    }

    @Override
    public void deleteFlowInstance(Long flowInstanceId) {
        flowInstanceDao.delete(flowInstanceId);
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
    public void createFlowProcess(FlowProcess flowProcess) {
        flowProcessDao.create(flowProcess);
    }

    @Override
    public void deleteFlowProcess(Long flowProcessId) {
        flowProcessDao.delete(flowProcessId);
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
}
