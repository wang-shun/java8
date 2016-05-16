package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.utils.BeanHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程实例公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public class FlowInstanceQueryImpl implements FlowInstanceQuery {

    private FlowInstance flowInstance;

    private WorkFlowEngine workFlowEngine;
    private JdbcAccess jdbcAccess;
    private String orderBy;
    private String desc;

    public FlowInstanceQueryImpl(WorkFlowEngine workFlowEngine) {
        this.workFlowEngine = workFlowEngine;
        this.jdbcAccess = workFlowEngine.buildJdbcAccess();
        this.flowInstance = new FlowInstance();
    }

    @Override
    public FlowInstanceQuery id(Long id) {
        flowInstance.setId(id);
        return this;
    }

    @Override
    public FlowInstanceQuery name(String name) {
        flowInstance.setName(name);
        return this;
    }

    @Override
    public FlowInstanceQuery flowDefinitionId(Long flowDefinitionId) {
        flowInstance.setFlowDefinitionId(flowDefinitionId);
        return this;
    }

    @Override
    public FlowInstanceQuery flowDefinitionKey(String flowDefinitionKey) {
        flowInstance.setFlowDefinitionKey(flowDefinitionKey);
        return this;
    }

    @Override
    public FlowInstanceQuery businessId(Long businessId) {
        flowInstance.setBusinessId(businessId);
        return this;
    }

    @Override
    public FlowInstanceQuery status(Integer status) {
        flowInstance.setStatus(status);
        return this;
    }

    @Override
    public FlowInstanceQuery type(Integer type) {
        flowInstance.setType(type);
        return this;
    }

    @Override
    public FlowInstanceQuery operatorId(Long operatorId) {
        flowInstance.setOperatorId(operatorId);
        return this;
    }

    @Override
    public FlowInstanceQuery operatorName(String operatorName) {
        flowInstance.setOperatorName(operatorName);
        return this;
    }

    @Override
    public FlowInstanceQuery parentInstanceId(Long parentInstanceId) {
        flowInstance.setParentInstanceId(parentInstanceId);
        return this;
    }

    @Override
    public FlowInstanceQuery bean(FlowInstance flowInstance) {
        this.flowInstance = flowInstance;
        return this;
    }

    @Override
    public FlowInstanceQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public FlowInstanceQuery desc() {
        this.desc = "true";
        return this;
    }

    @Override
    public FlowInstanceQuery asc() {
        this.desc = null;
        return this;
    }

    private Map getConditionMap(){
        Map criteria = BeanHelper.bean2Map(this.flowInstance, true);
        criteria.put("orderBy", orderBy);
        criteria.put("desc", desc);
        return criteria;
    }

    @Override
    public Paging<FlowInstance> paging(Integer offset, Integer limit) {
        return jdbcAccess.findFlowInstancesPaging(getConditionMap(), offset, limit);
    }

    @Override
    public FlowInstance single() {
        return jdbcAccess.findFlowInstanceSingle(getConditionMap());
    }

    @Override
    public List<FlowInstance> list() {
        return jdbcAccess.findFlowInstances(getConditionMap());
    }

    @Override
    public long size() {
        return jdbcAccess.findFlowInstancesSize(getConditionMap());
    }

    @Override
    public List<FlowInstance> findFlowInstances(FlowInstance flowInstance) {
        return jdbcAccess.findFlowInstances(flowInstance);
    }

    @Override
    public List<FlowInstance> findFlowInstances(Map criteria) {
        return jdbcAccess.findFlowInstances(criteria);
    }

    @Override
    public FlowInstance findFlowInstanceSingle(FlowInstance flowInstance) {
        return jdbcAccess.findFlowInstanceSingle(flowInstance);
    }

    @Override
    public FlowInstance findFlowInstanceSingle(Map criteria) {
        return jdbcAccess.findFlowInstanceSingle(criteria);
    }

    @Override
    public Paging<FlowInstance> findFlowInstancesPaging(Map criteria, Integer offset, Integer limit) {
        return jdbcAccess.findFlowInstancesPaging(criteria, offset, limit);
    }

    @Override
    public long findFlowInstancesSize(Map criteria) {
        return jdbcAccess.findFlowInstancesSize(criteria);
    }

    @Override
    public FlowInstance getExistFlowInstance(String flowDefinitionKey, Long businessId) {
        return this
                .flowDefinitionKey(flowDefinitionKey)
                .businessId(businessId)
                .type(FlowInstance.Type.PARENT.value())
                .single();
    }

    @Override
    public List<FlowInstance> getExistChildFlowInstance(String flowDefinitionKey, Long businessId) {
        return this
                .flowDefinitionKey(flowDefinitionKey)
                .businessId(businessId)
                .type(FlowInstance.Type.CHILD.value())
                .list();
    }
}
