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
    void createFlowDefinition(FlowDefinition flowDefinition);

    /**
     * 删除一个流程定义对象(逻辑删除)
     * @param flowDefinitionId
     */
    void deleteFlowDefinition(Long flowDefinitionId);
    void deleteFlowDefinition(List<Long> flowDefinitionIds);

    /**
     * 根据流程定义的bean查询流程定义
     * @param flowDefinition    流程定义的bean
     * @return
     */
    List<FlowDefinition> findFlowDefinitions(FlowDefinition flowDefinition);

    /**
     * 根据Map查询条件查询流程定义
     * @param criteria  查询条件map
     * @return
     */
    List<FlowDefinition> findFlowDefinitions(Map criteria);

    /**
     * 根据流程定义的bean查询流程定义唯一值, 若存在多个值则抛出异常
     * @param flowDefinition    流程定义bean
     * @return
     */
    FlowDefinition findFlowDefinitionSingle(FlowDefinition flowDefinition);

    /**
     * 根据Map查询条件查询流程定义唯一值, 若存在多个值则抛出异常
     * @param criteria  查询条件map
     * @return
     */
    FlowDefinition findFlowDefinitionSingle(Map criteria);

    /**
     * 分页查询流程定义
     * @param criteria  查询条件map
     * @param offset    列表首位置
     * @param limit     查询数量
     * @return
     */
    Paging<FlowDefinition> findFlowDefinitionsPaging(Map criteria, Integer offset, Integer limit);

    /**
     * 根据查询条件查询流程定义表数量
     * @param criteria  查询条件map
     * @return
     */
    long findFlowDefinitionsSize(Map criteria);


    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程定义 节点 的方法 //////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建一个流程定义的 节点 对象
     * @param flowDefinitionNode
     */
    void createFlowDefinitionNode(FlowDefinitionNode flowDefinitionNode);

    /**
     * 删除一个流程定义的 节点 对象
     * @param flowDefinitionNodeId
     */
    void deleteFlowDefinitionNode(Long flowDefinitionNodeId);
    void deleteFlowDefinitionNode(List<Long> flowDefinitionNodeIds);

    /** 以下是流程节点的公共查询方法, 与上述流程定义类似 */
    List<FlowDefinitionNode> findFlowDefinitionNodes(FlowDefinitionNode flowDefinitionNode);
    List<FlowDefinitionNode> findFlowDefinitionNodes(Map criteria);
    FlowDefinitionNode findFlowDefinitionNodeSingle(FlowDefinitionNode flowDefinitionNode);
    FlowDefinitionNode findFlowDefinitionNodeSingle(Map criteria);
    Paging<FlowDefinitionNode> findFlowDefinitionNodesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowDefinitionNodesSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程定义节点 连接事件 的方法 ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建流程定义节点的 连接事件 对象
     * @param flowDefinitionNodeEvent
     */
    void createFlowDefinitionNodeEvent(FlowDefinitionNodeEvent flowDefinitionNodeEvent);

    /**
     * 删除流程定义节点的 连接事件 对象
     * @param flowDefinitionNodeEventId
     */
    void deleteFlowDefinitionNodeEvent(Long flowDefinitionNodeEventId);
    void deleteFlowDefinitionNodeEvent(List<Long> flowDefinitionNodeEventIds);

    /** 以下是流程节点事件连接的公共查询方法, 与上述流程定义类似 */
    List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(Map criteria);
    FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(Map criteria);
    Paging<FlowDefinitionNodeEvent> findFlowDefinitionNodeEventsPaging(Map criteria, Integer offset, Integer limit);
    long findFlowDefinitionNodeEventsSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程实例 的方法 //////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建一个流程实例
     * @param flowInstance
     */
    Long createFlowInstance(FlowInstance flowInstance);

    /**
     * 更新一个流程实例
     * @param flowInstance
     */
    void updateFlowInstance(FlowInstance flowInstance);

    /**
     * 删除一个流程实例
     * @param flowInstanceId
     */
    void deleteFlowInstance(Long flowInstanceId);
    void deleteFlowInstance(List<Long> flowInstanceIds);

    /** 以下是流程实例的公共查询方法, 与上述流程定义类似 */
    List<FlowInstance> findFlowInstances(FlowInstance flowInstance);
    List<FlowInstance> findFlowInstances(Map criteria);
    FlowInstance findFlowInstanceSingle(FlowInstance flowInstance);
    FlowInstance findFlowInstanceSingle(Map criteria);
    Paging<FlowInstance> findFlowInstancesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowInstancesSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程活动节点 的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建当前活动节点
     * @param flowProcess
     */
    Long createFlowProcess(FlowProcess flowProcess);

    /**
     * 更新活动节点
     * @param flowProcess
     */
    void updateFlowProcess(FlowProcess flowProcess);

    /**
     * 删除当前活动节点
     * @param flowProcessId
     */
    void deleteFlowProcess(Long flowProcessId);
    void deleteFlowProcess(List<Long> flowProcessIds);

    /** 以下是流程活动节点的公共查询方法, 与上述流程定义类似 */
    List<FlowProcess> findFlowProcesses(FlowProcess flowProcess);
    List<FlowProcess> findFlowProcesses(Map criteria);
    FlowProcess findFlowProcessSingle(FlowProcess flowProcess);
    FlowProcess findFlowProcessSingle(Map criteria);
    Paging<FlowProcess> findFlowProcessesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowProcessesSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程节点跟踪 的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建当前活动节点跟踪
     * @param flowProcessTrack
     */
    Long createFlowProcessTrack(FlowProcessTrack flowProcessTrack);
    /**
     * 删除当前活动节点跟踪
     * @param flowProcessTrackId
     */
    void deleteFlowProcessTrack(Long flowProcessTrackId);
    void deleteFlowProcessTrack(List<Long> flowProcessTrackIds);
    void updateFlowProcessTrack(FlowProcessTrack flowProcessTrack);

    /** 活动节点跟踪公共查新接口 */
    List<FlowProcessTrack> findFlowProcessTracks(FlowProcessTrack flowProcessTrack);
    List<FlowProcessTrack> findFlowProcessTracks(Map criteria);
    FlowProcessTrack findFlowProcessTrackSingle(FlowProcessTrack flowProcessTrack);
    FlowProcessTrack findFlowProcessTrackSingle(Map criteria);
    Paging<FlowProcessTrack> findFlowProcessTracksPaging(Map criteria, Integer offset, Integer limit);
    long findFlowProcessTracksSize(Map criteria);


    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程实例历史 的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建一个历史实例
     * @param flowHistoryInstance
     */
    void createFlowHistoryInstance(FlowHistoryInstance flowHistoryInstance);
    /** 历史实例公共查新接口 */
    List<FlowHistoryInstance> findFlowHistoryInstances(FlowHistoryInstance flowHistoryInstance);
    List<FlowHistoryInstance> findFlowHistoryInstances(Map criteria);
    FlowHistoryInstance findFlowHistoryInstanceSingle(FlowHistoryInstance flowHistoryInstance);
    FlowHistoryInstance findFlowHistoryInstanceSingle(Map criteria);
    Paging<FlowHistoryInstance> findFlowHistoryInstancesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowHistoryInstancesSize(Map criteria);

    /////////////////////////////////////////////////////////////////////////////////////
    //////// 历史活动节点 的方法 ///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 创建一个历史活动节点
     * @param flowHistoryProcess
     */
    Long createFlowHistoryProcess(FlowHistoryProcess flowHistoryProcess);
    /** 历史活动节点公共查询接口 */
    List<FlowHistoryProcess> findFlowHistoryProcesses(FlowHistoryProcess flowHistoryProcess);
    List<FlowHistoryProcess> findFlowHistoryProcesses(Map criteria);
    FlowHistoryProcess findFlowHistoryProcessSingle(FlowHistoryProcess flowHistoryProcess);
    FlowHistoryProcess findFlowHistoryProcessSingle(Map criteria);
    Paging<FlowHistoryProcess> findFlowHistoryProcessesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowHistoryProcessesSize(Map criteria);
}
