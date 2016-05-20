package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowHistoryProcess;
import io.terminus.doctor.workflow.utils.BeanHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 历史流程活动节点公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/16
 */
public class FlowHistoryProcessQueryImpl implements FlowHistoryProcessQuery {

    private FlowHistoryProcess flowHistoryProcess;

    private WorkFlowEngine workFlowEngine;
    private JdbcAccess jdbcAccess;
    private String orderBy = "id"; // 默认id排序
    private String desc;

    public FlowHistoryProcessQueryImpl(WorkFlowEngine workFlowEngine) {
        this.workFlowEngine = workFlowEngine;
        this.jdbcAccess = workFlowEngine.buildJdbcAccess();
        this.flowHistoryProcess = new FlowHistoryProcess();
    }

    @Override
    public FlowHistoryProcessQuery id(Long id) {
        flowHistoryProcess.setId(id);
        return this;
    }

    @Override
    public FlowHistoryProcessQuery flowDefinitionNodeId(Long flowDefinitionNodeId) {
        flowHistoryProcess.setFlowDefinitionNodeId(flowDefinitionNodeId);
        return this;
    }

    @Override
    public FlowHistoryProcessQuery flowInstanceId(Long flowInstanceId) {
        flowHistoryProcess.setFlowInstanceId(flowInstanceId);
        return this;
    }

    @Override
    public FlowHistoryProcessQuery status(Integer status) {
        flowHistoryProcess.setStatus(status);
        return this;
    }

    @Override
    public FlowHistoryProcessQuery assignee(String assignee) {
        flowHistoryProcess.setAssignee(assignee);
        return this;
    }

    @Override
    public FlowHistoryProcessQuery forkNodeId(Long forkNodeId) {
        flowHistoryProcess.setForkNodeId(forkNodeId);
        return this;
    }

    @Override
    public FlowHistoryProcessQuery operatorId(Long operatorId) {
        flowHistoryProcess.setOperatorId(operatorId);
        return this;
    }

    @Override
    public FlowHistoryProcessQuery operatorName(String operatorName) {
        flowHistoryProcess.setOperatorName(operatorName);
        return this;
    }

    @Override
    public FlowHistoryProcessQuery bean(FlowHistoryProcess flowHistoryProcess) {
        this.flowHistoryProcess = flowHistoryProcess;
        return this;
    }

    @Override
    public FlowHistoryProcessQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public FlowHistoryProcessQuery desc() {
        this.desc = "true";
        return this;
    }

    @Override
    public FlowHistoryProcessQuery asc() {
        this.desc = null;
        return this;
    }

    private Map getConditionMap(){
        Map criteria = BeanHelper.bean2Map(this.flowHistoryProcess, true);
        criteria.put("orderBy", orderBy);
        criteria.put("desc", desc);
        return criteria;
    }

    @Override
    public Paging<FlowHistoryProcess> paging(Integer offset, Integer limit) {
        return jdbcAccess.findFlowHistoryProcessesPaging(getConditionMap(), offset, limit);
    }

    @Override
    public FlowHistoryProcess single() {
        return jdbcAccess.findFlowHistoryProcessSingle(getConditionMap());
    }

    @Override
    public List<FlowHistoryProcess> list() {
        return jdbcAccess.findFlowHistoryProcesses(getConditionMap());
    }

    @Override
    public long size() {
        return jdbcAccess.findFlowHistoryProcessesSize(getConditionMap());
    }

    @Override
    public List<FlowHistoryProcess> findFlowHistoryProcesses(FlowHistoryProcess flowHistoryProcess) {
        return jdbcAccess.findFlowHistoryProcesses(flowHistoryProcess);
    }

    @Override
    public List<FlowHistoryProcess> findFlowHistoryProcesses(Map criteria) {
        return jdbcAccess.findFlowHistoryProcesses(criteria);
    }

    @Override
    public FlowHistoryProcess findFlowHistoryProcessSingle(FlowHistoryProcess flowHistoryProcess) {
        return jdbcAccess.findFlowHistoryProcessSingle(flowHistoryProcess);
    }

    @Override
    public FlowHistoryProcess findFlowHistoryProcessSingle(Map criteria) {
        return jdbcAccess.findFlowHistoryProcessSingle(criteria);
    }

    @Override
    public Paging<FlowHistoryProcess> findFlowHistoryProcessesPaging(Map criteria, Integer offset, Integer limit) {
        return jdbcAccess.findFlowHistoryProcessesPaging(criteria, offset, limit);
    }

    @Override
    public long findFlowHistoryProcessesSize(Map criteria) {
        return jdbcAccess.findFlowHistoryProcessesSize(criteria);
    }

    @Override
    public List<FlowHistoryProcess> getHistoryProcess(Long flowInstanceId) {
        return this
                .flowInstanceId(flowInstanceId)
                .list();
    }
}
