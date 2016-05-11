package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.BeanHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程活动节点公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public class FlowProcessQueryImpl implements FlowProcessQuery {

    private FlowProcess flowProcess;

    private WorkFlowEngine workFlowEngine;
    private JdbcAccess jdbcAccess;
    private String orderBy;
    private String desc;

    public FlowProcessQueryImpl(WorkFlowEngine workFlowEngine) {
        this.workFlowEngine = workFlowEngine;
        this.jdbcAccess = workFlowEngine.buildJdbcAccess();
        this.flowProcess = new FlowProcess();
    }

    @Override
    public FlowProcessQuery id(Long id) {
        flowProcess.setId(id);
        return this;
    }

    @Override
    public FlowProcessQuery flowDefinitionNodeId(Long flowDefinitionNodeId) {
        flowProcess.setFlowDefinitionNodeId(flowDefinitionNodeId);
        return this;
    }

    @Override
    public FlowProcessQuery flowInstanceId(Long flowInstanceId) {
        flowProcess.setFlowInstanceId(flowInstanceId);
        return this;
    }

    @Override
    public FlowProcessQuery status(Integer status) {
        flowProcess.setStatus(status);
        return this;
    }

    @Override
    public FlowProcessQuery assignee(String assignee) {
        flowProcess.setAssignee(assignee);
        return this;
    }

    @Override
    public FlowProcessQuery bean(FlowProcess flowProcess) {
        this.flowProcess = flowProcess;
        return this;
    }

    @Override
    public FlowProcessQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public FlowProcessQuery desc() {
        this.desc = "true";
        return this;
    }

    @Override
    public FlowProcessQuery asc() {
        this.desc = null;
        return this;
    }

    private Map getConditionMap(){
        Map criteria = BeanHelper.bean2Map(this.flowProcess, true);
        criteria.put("orderBy", orderBy);
        criteria.put("desc", desc);
        return criteria;
    }

    @Override
    public Paging<FlowProcess> paging(Integer offset, Integer limit) {
        return jdbcAccess.findFlowProcessesPaging(getConditionMap(), offset, limit);
    }

    @Override
    public FlowProcess single() {
        return jdbcAccess.findFlowProcessSingle(getConditionMap());
    }

    @Override
    public List<FlowProcess> list() {
        return jdbcAccess.findFlowProcesses(getConditionMap());
    }

    @Override
    public long size() {
        return jdbcAccess.findFlowProcessesSize(getConditionMap());
    }

    @Override
    public List<FlowProcess> findFlowProcesses(FlowProcess flowProcess) {
        return jdbcAccess.findFlowProcesses(flowProcess);
    }

    @Override
    public List<FlowProcess> findFlowProcesses(Map criteria) {
        return jdbcAccess.findFlowProcesses(criteria);
    }

    @Override
    public FlowProcess findFlowProcessSingle(FlowProcess flowProcess) {
        return jdbcAccess.findFlowProcessSingle(flowProcess);
    }

    @Override
    public FlowProcess findFlowProcessSingle(Map criteria) {
        return jdbcAccess.findFlowProcessSingle(criteria);
    }

    @Override
    public Paging<FlowProcess> findFlowProcessesPaging(Map criteria, Integer offset, Integer limit) {
        return jdbcAccess.findFlowProcessesPaging(criteria, offset, limit);
    }

    @Override
    public long findFlowProcessesSize(Map criteria) {
        return jdbcAccess.findFlowProcessesSize(criteria);
    }

    @Override
    public List<FlowProcess> findCurrentProcesses(String flowDefinitionKey, Long businessId) {
        // 1. 获取当前的流程实例
        FlowInstance flowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .findExistFlowInstance(flowDefinitionKey, businessId);
        // 2. 获取当前活动节点
        return this
                .flowInstanceId(flowInstance.getId())
                .list();
    }
}
