package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.utils.BeanHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程节点公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public class FlowDefinitionNodeQueryImpl implements FlowDefinitionNodeQuery {

    private FlowDefinitionNode flowDefinitionNode;

    private WorkFlowEngine workFlowEngine;
    private JdbcAccess jdbcAccess;
    private String orderBy;
    private String desc;

    public FlowDefinitionNodeQueryImpl(WorkFlowEngine workFlowEngine) {
        this.workFlowEngine = workFlowEngine;
        this.jdbcAccess = workFlowEngine.buildJdbcAccess();
        this.flowDefinitionNode = new FlowDefinitionNode();
    }

    @Override
    public FlowDefinitionNodeQuery id(Long id) {
        flowDefinitionNode.setId(id);
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery name(String name) {
        flowDefinitionNode.setName(name);
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery flowDefinitionId(Long flowDefinitionId) {
        flowDefinitionNode.setFlowDefinitionId(flowDefinitionId);
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery nodeName(String nodeName) {
        flowDefinitionNode.setNodeName(nodeName);
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery type(Integer type) {
        flowDefinitionNode.setType(type);
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery assignee(String assignee) {
        flowDefinitionNode.setAssignee(assignee);
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery bean(FlowDefinitionNode flowDefinitionNode) {
        this.flowDefinitionNode = flowDefinitionNode;
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery desc() {
        this.desc = "true";
        return this;
    }

    @Override
    public FlowDefinitionNodeQuery asc() {
        this.desc = null;
        return this;
    }

    private Map getConditionMap(){
        Map criteria = BeanHelper.bean2Map(this.flowDefinitionNode, true);
        criteria.put("orderBy", orderBy);
        criteria.put("desc", desc);
        return criteria;
    }

    @Override
    public Paging<FlowDefinitionNode> paging(Integer offset, Integer limit) {
        return jdbcAccess.findFlowDefinitionNodesPaging(getConditionMap(), offset, limit);
    }

    @Override
    public FlowDefinitionNode single() {
        return jdbcAccess.findFlowDefinitionNodeSingle(getConditionMap());
    }

    @Override
    public List<FlowDefinitionNode> list() {
        return jdbcAccess.findFlowDefinitionNodes(getConditionMap());
    }

    @Override
    public long size() {
        return jdbcAccess.findFlowDefinitionNodesSize(getConditionMap());
    }

    @Override
    public List<FlowDefinitionNode> findFlowDefinitionNodes(FlowDefinitionNode flowDefinitionNode) {
        return jdbcAccess.findFlowDefinitionNodes(flowDefinitionNode);
    }

    @Override
    public List<FlowDefinitionNode> findFlowDefinitionNodes(Map criteria) {
        return jdbcAccess.findFlowDefinitionNodes(criteria);
    }

    @Override
    public FlowDefinitionNode findFlowDefinitionNodeSingle(FlowDefinitionNode flowDefinitionNode) {
        return jdbcAccess.findFlowDefinitionNodeSingle(flowDefinitionNode);
    }

    @Override
    public FlowDefinitionNode findFlowDefinitionNodeSingle(Map criteria) {
        return jdbcAccess.findFlowDefinitionNodeSingle(criteria);
    }

    @Override
    public Paging<FlowDefinitionNode> findFlowDefinitionNodesPaging(Map criteria, Integer offset, Integer limit) {
        return jdbcAccess.findFlowDefinitionNodesPaging(criteria, offset, limit);
    }

    @Override
    public long findFlowDefinitionNodesSize(Map criteria) {
        return jdbcAccess.findFlowDefinitionNodesSize(criteria);
    }

    @Override
    public FlowDefinitionNode findDefinitionNodeByType(Long flowDefinitionId, Integer nodeType) {
        return this
                .flowDefinitionId(flowDefinitionId)
                .type(nodeType)
                .single();
    }
}
