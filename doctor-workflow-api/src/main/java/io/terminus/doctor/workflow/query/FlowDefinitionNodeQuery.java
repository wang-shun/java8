package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程定义节点公共查询方法
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public interface FlowDefinitionNodeQuery {

    ///////////////////////////////////////////////////////////////
    ///// 流程节点 query 公共查询方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////
    FlowDefinitionNodeQuery id(Long id);
    FlowDefinitionNodeQuery name(String name);
    FlowDefinitionNodeQuery flowDefinitionId(Long flowDefinitionId);
    FlowDefinitionNodeQuery nodeName(String nodeName);
    FlowDefinitionNodeQuery type(Integer type);
    FlowDefinitionNodeQuery assignee(String assignee);
    FlowDefinitionNodeQuery bean(FlowDefinitionNode flowDefinitionNode);
    FlowDefinitionNodeQuery orderBy(String orderBy);
    FlowDefinitionNodeQuery desc();
    FlowDefinitionNodeQuery asc();
    Paging<FlowDefinitionNode> paging(Integer offset, Integer limit);   // 分页方法
    FlowDefinitionNode single();                                        // 唯一值
    List<FlowDefinitionNode> list();                                    // 值列表
    long size();                                                        // 数量

    List<FlowDefinitionNode> findFlowDefinitionNodes(FlowDefinitionNode flowDefinitionNode);
    List<FlowDefinitionNode> findFlowDefinitionNodes(Map criteria);
    FlowDefinitionNode findFlowDefinitionNodeSingle(FlowDefinitionNode flowDefinitionNode);
    FlowDefinitionNode findFlowDefinitionNodeSingle(Map criteria);
    Paging<FlowDefinitionNode> findFlowDefinitionNodesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowDefinitionNodesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程节点 query 其他查询方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 根据流程定义id和节点类型, 查询当前流程定义的流程节点
     *
     * @param flowDefinitionId  流程定义id
     * @param nodeType          流程节点类型
     *      @see io.terminus.doctor.workflow.model.FlowDefinitionNode.Type
     * @return
     */
    FlowDefinitionNode findDefinitionNodeByType(Long flowDefinitionId, Integer nodeType);
}
