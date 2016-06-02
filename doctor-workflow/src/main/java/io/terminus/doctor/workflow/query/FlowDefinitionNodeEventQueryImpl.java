package io.terminus.doctor.workflow.query;

import com.google.common.collect.Lists;
import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.BeanHelper;
import io.terminus.doctor.workflow.utils.StringHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程定义事件连线公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public class FlowDefinitionNodeEventQueryImpl implements FlowDefinitionNodeEventQuery {

    private FlowDefinitionNodeEvent flowDefinitionNodeEvent;

    private WorkFlowEngine workFlowEngine;
    private JdbcAccess jdbcAccess;
    private String orderBy = "id"; // 默认id排序
    private String desc;

    public FlowDefinitionNodeEventQueryImpl(WorkFlowEngine workFlowEngine) {
        this.workFlowEngine = workFlowEngine;
        this.jdbcAccess = workFlowEngine.buildJdbcAccess();
        this.flowDefinitionNodeEvent = new FlowDefinitionNodeEvent();
    }

    @Override
    public FlowDefinitionNodeEventQuery id(Long id) {
        flowDefinitionNodeEvent.setId(id);
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery name(String name) {
        flowDefinitionNodeEvent.setName(name);
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery value(String value) {
        flowDefinitionNodeEvent.setValue(value);
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery flowDefinitionId(Long flowDefinitionId) {
        flowDefinitionNodeEvent.setFlowDefinitionId(flowDefinitionId);
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery sourceNodeId(Long sourceNodeId) {
        flowDefinitionNodeEvent.setSourceNodeId(sourceNodeId);
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery targetNodeId(Long targetNodeId) {
        flowDefinitionNodeEvent.setTargetNodeId(targetNodeId);
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery bean(FlowDefinitionNodeEvent flowDefinitionNodeEvent) {
        this.flowDefinitionNodeEvent = flowDefinitionNodeEvent;
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery desc() {
        this.desc = "true";
        return this;
    }

    @Override
    public FlowDefinitionNodeEventQuery asc() {
        this.desc = null;
        return this;
    }

    private Map getConditionMap() {
        Map criteria = BeanHelper.bean2Map(this.flowDefinitionNodeEvent, true);
        criteria.put("orderBy", orderBy);
        criteria.put("desc", desc);
        return criteria;
    }

    @Override
    public Paging<FlowDefinitionNodeEvent> paging(Integer offset, Integer limit) {
        return jdbcAccess.findFlowDefinitionNodeEventsPaging(getConditionMap(), offset, limit);
    }

    @Override
    public FlowDefinitionNodeEvent single() {
        return jdbcAccess.findFlowDefinitionNodeEventSingle(getConditionMap());
    }

    @Override
    public List<FlowDefinitionNodeEvent> list() {
        return jdbcAccess.findFlowDefinitionNodeEvents(getConditionMap());
    }

    @Override
    public long size() {
        return jdbcAccess.findFlowDefinitionNodeEventsSize(getConditionMap());
    }

    @Override
    public List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(FlowDefinitionNodeEvent flowDefinitionNodeEvent) {
        return jdbcAccess.findFlowDefinitionNodeEvents(flowDefinitionNodeEvent);
    }

    @Override
    public List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(Map criteria) {
        return jdbcAccess.findFlowDefinitionNodeEvents(criteria);
    }

    @Override
    public FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(FlowDefinitionNodeEvent flowDefinitionNodeEvent) {
        return jdbcAccess.findFlowDefinitionNodeEventSingle(flowDefinitionNodeEvent);
    }

    @Override
    public FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(Map criteria) {
        return jdbcAccess.findFlowDefinitionNodeEventSingle(criteria);
    }

    @Override
    public Paging<FlowDefinitionNodeEvent> findFlowDefinitionNodeEventsPaging(Map criteria, Integer offset, Integer limit) {
        return jdbcAccess.findFlowDefinitionNodeEventsPaging(criteria, offset, limit);
    }

    @Override
    public long findFlowDefinitionNodeEventsSize(Map criteria) {
        return jdbcAccess.findFlowDefinitionNodeEventsSize(criteria);
    }

    @Override
    public List<FlowDefinitionNodeEvent> getNodeEvents(Long flowDefinitionId) {
        return this
                .flowDefinitionId(flowDefinitionId)
                .list();
    }

    @Override
    public List<FlowDefinitionNodeEvent> getNodeEventsBySourceId(Long flowDefinitionId, Long sourceId) {
        return this
                .flowDefinitionId(flowDefinitionId)
                .sourceNodeId(sourceId)
                .list();
    }

    @Override
    public FlowDefinitionNodeEvent getNodeEventByST(Long flowDefinitionId, Long sourceId, Long targetId) {
        return this
                .flowDefinitionId(flowDefinitionId)
                .sourceNodeId(sourceId)
                .targetNodeId(targetId)
                .single();
    }

    @Override
    public List<FlowDefinitionNodeEvent> getNextTaskNodeEvents(String flowDefinitionKey, Long businessId) {
        List<FlowDefinitionNodeEvent> events = Lists.newArrayList();
        // 1. 获取正常的流程实例
        List<FlowInstance> flowInstances = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .getFlowInstances(flowDefinitionKey, businessId);
        if (flowInstances == null || flowInstances.size() == 0) {
            AssertHelper.throwException(
                    "当前不存在可运行的流程实例, 流程key为{}, 业务id为{}", flowDefinitionKey, businessId);
        }
        flowInstances.forEach(instance -> {
            // 2. 获取正在执行的流程
            List<FlowProcess> processes = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                    .getCurrentProcesses(instance.getId());

            processes.forEach(process -> {
                List<FlowDefinitionNodeEvent> nodeEvents = getNodeEventsBySourceId(instance.getFlowDefinitionId(), process.getFlowDefinitionNodeId());
                nodeEvents.forEach(nodeEvent -> {
                    // 如果存在事件, 直接返回
                    if (StringHelper.isNotBlank(nodeEvent.getHandler())) {
                        events.add(nodeEvent);
                    }else{
                        getTaskEvents(events, instance, nodeEvent);
                    }
                });
            });
        });
        return events;
    }

    private void getTaskEvents(List<FlowDefinitionNodeEvent> events, FlowInstance instance, FlowDefinitionNodeEvent nodeEvent) {

        FlowDefinitionNode nextNode = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                .id(nodeEvent.getTargetNodeId())
                .single();
        switch (FlowDefinitionNode.Type.from(nextNode.getType())) {
            case TASK:
                events.add(nodeEvent);
                break;
            case DECISION:
                List<FlowDefinitionNodeEvent> eventsDecision = getNodeEventsBySourceId(instance.getFlowDefinitionId(), nextNode.getId());
                eventsDecision.forEach(nextEvent -> getTaskEvents(events, instance, nextEvent));
                break;
            case FORK:
                List<FlowDefinitionNodeEvent> eventsFork = getNodeEventsBySourceId(instance.getFlowDefinitionId(), nextNode.getId());
                eventsFork.forEach(nextEvent -> getTaskEvents(events, instance, nextEvent));
                break;
            case JOIN:
                events.add(nodeEvent);
                break;
            case END:
                events.add(nodeEvent);
                break;
            // TODO : 子流程
            default:
                break;
        }
    }

}
