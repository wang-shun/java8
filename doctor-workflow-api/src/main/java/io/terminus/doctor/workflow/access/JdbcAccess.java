package io.terminus.doctor.workflow.access;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowHistoryInstance;
import io.terminus.doctor.workflow.model.FlowHistoryProcess;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.model.FlowProcessTrack;

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
     * 删除一个流程定义对象(逻辑删除)
     * @param flowDefinitionId
     */
    public void deleteFlowDefinition(Long flowDefinitionId);
    public void deleteFlowDefinition(List<Long> flowDefinitionIds);

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

    /**
     * 删除一个流程定义的 节点 对象
     * @param flowDefinitionNodeId
     */
    public void deleteFlowDefinitionNode(Long flowDefinitionNodeId);
    public void deleteFlowDefinitionNode(List<Long> flowDefinitionNodeIds);

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

    /**
     * 删除流程定义节点的 连接事件 对象
     * @param flowDefinitionNodeEventId
     */
    public void deleteFlowDefinitionNodeEvent(Long flowDefinitionNodeEventId);
    public void deleteFlowDefinitionNodeEvent(List<Long> flowDefinitionNodeEventIds);

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

    /**
     * 更新一个流程实例
     * @param flowInstance
     */
    public void updateFlowInstance(FlowInstance flowInstance);

    /**
     * 删除一个流程实例
     * @param flowInstanceId
     */
    public void deleteFlowInstance(Long flowInstanceId);
    public void deleteFlowInstance(List<Long> flowInstanceIds);

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

    /**
     * 更新活动节点
     * @param flowProcess
     */
    public void updateFlowProcess(FlowProcess flowProcess);

    /**
     * 删除当前活动节点
     * @param flowProcessId
     */
    public void deleteFlowProcess(Long flowProcessId);
    public void deleteFlowProcess(List<Long> flowProcessIds);

    /** 以下是流程活动节点的公共查询方法, 与上述流程定义类似 */
    public List<FlowProcess> findFlowProcesses(FlowProcess flowProcess);
    public List<FlowProcess> findFlowProcesses(Map criteria);
    public FlowProcess findFlowProcessSingle(FlowProcess flowProcess);
    public FlowProcess findFlowProcessSingle(Map criteria);
    public Paging<FlowProcess> findFlowProcessesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowProcessesSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程节点跟踪 的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建当前活动节点跟踪
     * @param flowProcessTrack
     */
    public void createFlowProcessTrack(FlowProcessTrack flowProcessTrack);
    /**
     * 删除当前活动节点跟踪
     * @param flowProcessTrackId
     */
    public void deleteFlowProcessTrack(Long flowProcessTrackId);
    public void deleteFlowProcessTrack(List<Long> flowProcessTrackIds);

    /** 活动节点跟踪公共查新接口 */
    public List<FlowProcessTrack> findFlowProcessTracks(FlowProcessTrack flowProcessTrack);
    public List<FlowProcessTrack> findFlowProcessTracks(Map criteria);
    public FlowProcessTrack findFlowProcessTrackSingle(FlowProcessTrack flowProcessTrack);
    public FlowProcessTrack findFlowProcessTrackSingle(Map criteria);
    public Paging<FlowProcessTrack> findFlowProcessTracksPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowProcessTracksSize(Map criteria);


    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程实例历史 的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建一个历史实例
     * @param flowHistoryInstance
     */
    public void createFlowHistoryInstance(FlowHistoryInstance flowHistoryInstance);
    /** 历史实例公共查新接口 */
    public List<FlowHistoryInstance> findFlowHistoryInstances(FlowHistoryInstance flowHistoryInstance);
    public List<FlowHistoryInstance> findFlowHistoryInstances(Map criteria);
    public FlowHistoryInstance findFlowHistoryInstanceSingle(FlowHistoryInstance flowHistoryInstance);
    public FlowHistoryInstance findFlowHistoryInstanceSingle(Map criteria);
    public Paging<FlowHistoryInstance> findFlowHistoryInstancesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowHistoryInstancesSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 历史活动节点 的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建一个历史活动节点
     * @param flowHistoryProcess
     */
    public void createFlowHistoryProcess(FlowHistoryProcess flowHistoryProcess);
    /** 历史活动节点公共查询接口 */
    public List<FlowHistoryProcess> findFlowHistoryProcesses(FlowHistoryProcess flowHistoryProcess);
    public List<FlowHistoryProcess> findFlowHistoryProcesses(Map criteria);
    public FlowHistoryProcess findFlowHistoryProcessSingle(FlowHistoryProcess flowHistoryProcess);
    public FlowHistoryProcess findFlowHistoryProcessSingle(Map criteria);
    public Paging<FlowHistoryProcess> findFlowHistoryProcessesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowHistoryProcessesSize(Map criteria);
}
