package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.utils.BeanHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程定义公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/10
 */
public class FlowDefinitionQueryImpl implements FlowDefinitionQuery {

    private FlowDefinition flowDefinition;

    private WorkFlowEngine workFlowEngine;
    private JdbcAccess jdbcAccess;
    private String orderBy = "id"; // 默认id排序
    private String desc;

    public FlowDefinitionQueryImpl(WorkFlowEngine workFlowEngine) {
        this.workFlowEngine = workFlowEngine;
        this.jdbcAccess = workFlowEngine.buildJdbcAccess();
        this.flowDefinition = new FlowDefinition();
    }

    @Override
    public FlowDefinitionQuery id(Long id) {
        flowDefinition.setId(id);
        return this;
    }

    @Override
    public FlowDefinitionQuery key(String key) {
        flowDefinition.setKey(key);
        return this;
    }

    @Override
    public FlowDefinitionQuery version(Long version) {
        flowDefinition.setVersion(version);
        return this;
    }

    @Override
    public FlowDefinitionQuery status(Integer status) {
        flowDefinition.setStatus(status);
        return this;
    }

    @Override
    public FlowDefinitionQuery operatorId(Long operatorId) {
        flowDefinition.setOperatorId(operatorId);
        return this;
    }

    @Override
    public FlowDefinitionQuery operatorName(String operatorName) {
        flowDefinition.setOperatorName(operatorName);
        return this;
    }

    @Override
    public FlowDefinitionQuery bean(FlowDefinition flowDefinition) {
        this.flowDefinition = flowDefinition;
        return this;
    }

    @Override
    public FlowDefinitionQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public FlowDefinitionQuery desc() {
        desc = "true";
        return this;
    }

    @Override
    public FlowDefinitionQuery asc() {
        desc = null;
        return this;
    }

    private Map getConditionMap(){
        Map criteria = BeanHelper.bean2Map(this.flowDefinition, true);
        criteria.put("orderBy", orderBy);
        criteria.put("desc", desc);
        return criteria;
    }

    @Override
    public Paging<FlowDefinition> paging(Integer offset, Integer limit) {
        return jdbcAccess.findFlowDefinitionsPaging(getConditionMap(), offset, limit);
    }

    @Override
    public FlowDefinition single() {
        return jdbcAccess.findFlowDefinitionSingle(getConditionMap());
    }

    @Override
    public List<FlowDefinition> list() {
        return jdbcAccess.findFlowDefinitions(getConditionMap());
    }

    @Override
    public long size() {
        return jdbcAccess.findFlowDefinitionsSize(getConditionMap());
    }

    @Override
    public List<FlowDefinition> findFlowDefinitions(FlowDefinition flowDefinition) {
        return jdbcAccess.findFlowDefinitions(flowDefinition);
    }

    @Override
    public List<FlowDefinition> findFlowDefinitions(Map criteria) {
        return jdbcAccess.findFlowDefinitions(criteria);
    }

    @Override
    public FlowDefinition findFlowDefinitionSingle(FlowDefinition flowDefinition) {
        return jdbcAccess.findFlowDefinitionSingle(flowDefinition);
    }

    @Override
    public FlowDefinition findFlowDefinitionSingle(Map criteria) {
        return jdbcAccess.findFlowDefinitionSingle(criteria);
    }

    @Override
    public Paging<FlowDefinition> findFlowDefinitionsPaging(Map criteria, Integer offset, Integer limit) {
        return jdbcAccess.findFlowDefinitionsPaging(criteria, offset, limit);
    }

    @Override
    public long findFlowDefinitionsSize(Map criteria) {
        return jdbcAccess.findFlowDefinitionsSize(criteria);
    }

    @Override
    public FlowDefinition getLatestDefinitionByKey(String flowDefinitionKey) {
        List<FlowDefinition> list = this
                .key(flowDefinitionKey)
                .status(FlowDefinition.Status.NORMAL.value())
                .orderBy("version")
                .desc()
                .list();
        if(list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public FlowDefinition getDefinitionById(Long id) {
        return this
                .id(id)
                .single();
    }

    @Override
    public List<FlowDefinition> getDefinitions() {
        return this
                .status(FlowDefinition.Status.NORMAL.value())
                .list();
    }

    @Override
    public List<FlowDefinition> getDefinitionsByKey(String flowDefinitionKey) {
        return this
                .key(flowDefinitionKey)
                .status(FlowDefinition.Status.NORMAL.value())
                .list();
    }
}
