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
    public FlowDefinitionNodeQuery id(Long id);
    public FlowDefinitionNodeQuery name(String name);
    public FlowDefinitionNodeQuery flowDefinitionId(Long flowDefinitionId);
    public FlowDefinitionNodeQuery nodeName(String nodeName);
    public FlowDefinitionNodeQuery type(Integer type);
    public FlowDefinitionNodeQuery assignee(String  assignee);
    public FlowDefinitionNodeQuery bean(FlowDefinitionNode flowDefinitionNode);
    public FlowDefinitionNodeQuery orderBy(String orderBy);
    public FlowDefinitionNodeQuery desc();
    public FlowDefinitionNodeQuery asc();
    public Paging<FlowDefinitionNode> paging(Integer offset, Integer limit);   // 分页方法
    public FlowDefinitionNode single();                                        // 唯一值
    public List<FlowDefinitionNode> list();                                    // 值列表
    public long size();                                                        // 数量

    public List<FlowDefinitionNode> findFlowDefinitionNodes(FlowDefinitionNode flowDefinitionNode);
    public List<FlowDefinitionNode> findFlowDefinitionNodes(Map criteria);
    public FlowDefinitionNode findFlowDefinitionNodeSingle(FlowDefinitionNode flowDefinitionNode);
    public FlowDefinitionNode findFlowDefinitionNodeSingle(Map criteria);
    public Paging<FlowDefinitionNode> findFlowDefinitionNodesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowDefinitionNodesSize(Map criteria);

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
    public FlowDefinitionNode findDefinitionNodeByType(Long flowDefinitionId, Integer nodeType);
}
