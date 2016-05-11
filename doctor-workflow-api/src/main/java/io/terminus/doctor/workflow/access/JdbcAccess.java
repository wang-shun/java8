package io.terminus.doctor.workflow.access;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;

import java.util.List;
import java.util.Map;

/**
 * Desc: 工作流数据库层统一访问接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
public interface JdbcAccess {

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程定义相关的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建一个流程定义对象
     * @param flowDefinition    流程定义对象
     */
    public void createFlowDefinition(FlowDefinition flowDefinition);

    /**
     * 根据流程定义的bean查询流程定义
     * @param flowDefinition    流程定义的bean
     * @return
     */
    public List<FlowDefinition> findFlowDefinitions(FlowDefinition flowDefinition);

    /**
     * 根据Map查询条件查询流程定义
     * @param criteria  查询条件map
     * @return
     */
    public List<FlowDefinition> findFlowDefinitions(Map criteria);

    /**
     * 根据流程定义的bean查询流程定义唯一值, 若存在多个值则抛出异常
     * @param flowDefinition    流程定义bean
     * @return
     */
    public FlowDefinition findFlowDefinitionSingle(FlowDefinition flowDefinition);

    /**
     * 根据Map查询条件查询流程定义唯一值, 若存在多个值则抛出异常
     * @param criteria  查询条件map
     * @return
     */
    public FlowDefinition findFlowDefinitionSingle(Map criteria);

    /**
     * 分页查询流程定义
     * @param criteria  查询条件map
     * @param offset    列表首位置
     * @param limit     查询数量
     * @return
     */
    public Paging<FlowDefinition> findFlowDefinitionsPaging(Map criteria, Integer offset, Integer limit);

    /**
     * 根据查询条件查询流程定义表数量
     * @param criteria  查询条件map
     * @return
     */
    public long findFlowDefinitionsSize(Map criteria);


    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程定义 节点 的方法 //////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建一个流程定义的 节点 对象
     * @param flowDefinitionNode
     */
    public void createFlowDefinitionNode(FlowDefinitionNode flowDefinitionNode);

    /** 以下是流程节点的公共查询方法, 与上述流程定义类似 */
    public List<FlowDefinitionNode> findFlowDefinitionNodes(FlowDefinitionNode flowDefinitionNode);
    public List<FlowDefinitionNode> findFlowDefinitionNodes(Map criteria);
    public FlowDefinitionNode findFlowDefinitionNodeSingle(FlowDefinitionNode flowDefinitionNode);
    public FlowDefinitionNode findFlowDefinitionNodeSingle(Map criteria);
    public Paging<FlowDefinitionNode> findFlowDefinitionNodesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowDefinitionNodesSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程定义节点 连接事件 的方法 ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建流程定义节点的 连接事件 对象
     * @param flowDefinitionNodeEvent
     */
    public void createFlowDefinitionNodeEvent(FlowDefinitionNodeEvent flowDefinitionNodeEvent);

    /** 以下是流程节点事件连接的公共查询方法, 与上述流程定义类似 */
    public List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    public List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(Map criteria);
    public FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    public FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(Map criteria);
    public Paging<FlowDefinitionNodeEvent> findFlowDefinitionNodeEventsPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowDefinitionNodeEventsSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程实例 的方法 //////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建一个流程实例
     * @param flowInstance
     */
    public void createFlowInstance(FlowInstance flowInstance);

    /** 以下是流程实例的公共查询方法, 与上述流程定义类似 */
    public List<FlowInstance> findFlowInstances(FlowInstance flowInstance);
    public List<FlowInstance> findFlowInstances(Map criteria);
    public FlowInstance findFlowInstanceSingle(FlowInstance flowInstance);
    public FlowInstance findFlowInstanceSingle(Map criteria);
    public Paging<FlowInstance> findFlowInstancesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowInstancesSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程活动节点 的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建当前活动节点
     * @param flowProcess
     */
    public void createFlowProcess(FlowProcess flowProcess);

    /** 以下是流程活动节点的公共查询方法, 与上述流程定义类似 */
    public List<FlowProcess> findFlowProcesses(FlowProcess flowProcess);
    public List<FlowProcess> findFlowProcesses(Map criteria);
    public FlowProcess findFlowProcessSingle(FlowProcess flowProcess);
    public FlowProcess findFlowProcessSingle(Map criteria);
    public Paging<FlowProcess> findFlowProcessesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowProcessesSize(Map criteria);
}
