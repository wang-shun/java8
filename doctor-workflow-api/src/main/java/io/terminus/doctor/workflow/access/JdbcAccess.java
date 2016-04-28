package io.terminus.doctor.workflow.access;

import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowInstance;

import java.util.List;

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
     * @param flowDefinition
     */
    public void createFlowDefinition(FlowDefinition flowDefinition);

    /**
     * 根据id查询流程定义对象
     * @param id
     * @return
     */
    public FlowDefinition findFlowDefinitionById(Long id);

    /**
     * 根据流程定义的key值获取最新版本的流程定义
     * @param key
     * @return
     */
    public FlowDefinition findLatestDefinitionByKey(String key);


    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程定义 节点 的方法 //////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建一个流程定义的 节点 对象
     * @param flowDefinitionNode
     */
    public void createFlowDefinitionNode(FlowDefinitionNode flowDefinitionNode);


    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程定义节点 连接事件 的方法 ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 创建流程定义节点的 连接事件 对象
     * @param flowDefinitionNodeEvent
     */
    public void createFlowDefinitionNodeEvent(FlowDefinitionNodeEvent flowDefinitionNodeEvent);


    /////////////////////////////////////////////////////////////////////////////////////
    //////// 流程实例 的方法 //////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /**
     * 根据流程定义key和业务id查询流程实例
     * @param flowDefinitionKey 流程定义key
     * @param businessId        业务id
     * @return
     */
    public List<FlowInstance> findExistFlowInstance(String flowDefinitionKey, Long businessId);
}
