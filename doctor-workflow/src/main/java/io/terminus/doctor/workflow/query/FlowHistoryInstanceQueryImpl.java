package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowHistoryInstance;
import io.terminus.doctor.workflow.utils.BeanHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程实例历史公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/16
 */
public class FlowHistoryInstanceQueryImpl implements FlowHistoryInstanceQuery {

    private FlowHistoryInstance flowHistoryInstance;

    private WorkFlowEngine workFlowEngine;
    private JdbcAccess jdbcAccess;
    private String orderBy = "id"; // 默认id排序
    private String desc;

    public FlowHistoryInstanceQueryImpl(WorkFlowEngine workFlowEngine) {
        this.workFlowEngine = workFlowEngine;
        this.jdbcAccess = workFlowEngine.buildJdbcAccess();
        this.flowHistoryInstance = new FlowHistoryInstance();
    }

    @Override
    public FlowHistoryInstanceQuery id(Long id) {
        flowHistoryInstance.setId(id);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery name(String name) {
        flowHistoryInstance.setName(name);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery flowDefinitionId(Long flowDefinitionId) {
        flowHistoryInstance.setFlowDefinitionId(flowDefinitionId);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery flowDefinitionKey(String flowDefinitionKey) {
        flowHistoryInstance.setFlowDefinitionKey(flowDefinitionKey);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery businessId(Long businessId) {
        flowHistoryInstance.setBusinessId(businessId);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery status(Integer status) {
        flowHistoryInstance.setStatus(status);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery type(Integer type) {
        flowHistoryInstance.setType(type);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery operatorId(Long operatorId) {
        flowHistoryInstance.setOperatorId(operatorId);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery operatorName(String operatorName) {
        flowHistoryInstance.setOperatorName(operatorName);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery parentInstanceId(Long parentInstanceId) {
        flowHistoryInstance.setParentInstanceId(parentInstanceId);
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery bean(FlowHistoryInstance flowHistoryInstance) {
        this.flowHistoryInstance = flowHistoryInstance;
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery desc() {
        this.desc = "true";
        return this;
    }

    @Override
    public FlowHistoryInstanceQuery asc() {
        this.desc = null;
        return this;
    }

    private Map getConditionMap(){
        Map criteria = BeanHelper.bean2Map(this.flowHistoryInstance, true);
        criteria.put("orderBy", orderBy);
        criteria.put("desc", desc);
        return criteria;
    }

    @Override
    public Paging<FlowHistoryInstance> paging(Integer offset, Integer limit) {
        return jdbcAccess.findFlowHistoryInstancesPaging(getConditionMap(), offset, limit);
    }

    @Override
    public FlowHistoryInstance single() {
        return jdbcAccess.findFlowHistoryInstanceSingle(getConditionMap());
    }

    @Override
    public List<FlowHistoryInstance> list() {
        return jdbcAccess.findFlowHistoryInstances(getConditionMap());
    }

    @Override
    public long size() {
        return jdbcAccess.findFlowHistoryInstancesSize(getConditionMap());
    }

    @Override
    public List<FlowHistoryInstance> findFlowHistoryInstances(FlowHistoryInstance flowHistoryInstance) {
        return jdbcAccess.findFlowHistoryInstances(flowHistoryInstance);
    }

    @Override
    public List<FlowHistoryInstance> findFlowHistoryInstances(Map criteria) {
        return jdbcAccess.findFlowHistoryInstances(criteria);
    }

    @Override
    public FlowHistoryInstance findFlowHistoryInstanceSingle(FlowHistoryInstance flowHistoryInstance) {
        return jdbcAccess.findFlowHistoryInstanceSingle(flowHistoryInstance);
    }

    @Override
    public FlowHistoryInstance findFlowHistoryInstanceSingle(Map criteria) {
        return jdbcAccess.findFlowHistoryInstanceSingle(criteria);
    }

    @Override
    public Paging<FlowHistoryInstance> findFlowHistoryInstancesPaging(Map criteria, Integer offset, Integer limit) {
        return jdbcAccess.findFlowHistoryInstancesPaging(criteria, offset, limit);
    }

    @Override
    public long findFlowHistoryInstancesSize(Map criteria) {
        return jdbcAccess.findFlowHistoryInstancesSize(criteria);
    }
}
