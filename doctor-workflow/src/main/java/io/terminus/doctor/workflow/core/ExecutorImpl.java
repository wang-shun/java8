package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.NodeHelper;
import io.terminus.doctor.workflow.utils.StringHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 执行器, 用来执行节点的操作
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/13
 */
public class ExecutorImpl implements Executor {

    private WorkFlowEngine workFlowEngine;
    /**
     * 流程定义key
     */
    private String flowDefinitionKey;
    /**
     * 业务id
     */
    private Long businessId;
    /**
     * 节点执行者
     */
    private String assignee;

    public ExecutorImpl(WorkFlowEngine workFlowEngine, String flowDefinitionKey, Long businessId) {
        this.workFlowEngine = workFlowEngine;
        this.flowDefinitionKey = flowDefinitionKey;
        this.businessId = businessId;
    }

    public ExecutorImpl(WorkFlowEngine workFlowEngine, String flowDefinitionKey, Long businessId, String assignee) {
        this(workFlowEngine, flowDefinitionKey, businessId);
        this.assignee = assignee;
    }

    @Override
    public void execute() {
        execute(null, null, null, null);
    }

    @Override
    public void execute(Map expression) {
        execute(expression, null, null, null);
    }

    @Override
    public void execute(String flowData) {
        execute(null, flowData, null, null);
    }

    @Override
    public void execute(Map expression, String flowData) {
        execute(expression, flowData, null, null);
    }

    @Override
    public void execute(Long operatorId, String operatorName) {
        execute(null, null, operatorId, operatorName);
    }

    @Override
    public void execute(Map expression, Long operatorId, String operatorName) {
        execute(expression, null, operatorId, operatorName);
    }

    @Override
    public void execute(Map expression, String flowData, Long operatorId, String operatorName) {
        // TODO 如果存在子流程, 先执行子流程

        FlowInstance flowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .getExistFlowInstance(flowDefinitionKey, businessId);
        AssertHelper.isNull(flowInstance,
                "执行节点的流程实例不存在, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);

        // 获取当前活动的任务节点
        List<FlowProcess> currentProcesses = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                .getCurrentProcesses(flowInstance.getId());
        if (currentProcesses == null || currentProcesses.size() == 0) {
            AssertHelper.throwException("当前没有可以执行的任务, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
        }
        // 如果是fork-join情况
        if (currentProcesses != null && currentProcesses.size() > 1) {
            AssertHelper.isBlank(assignee,
                    "当前存在多个任务, 请指定任务处理者, 以便于判断执行哪一个任务, 流程定义key为: {}, 业务id为: {}",
                    flowDefinitionKey, businessId);
            FlowProcess assigneeProcess = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                    .getCurrentProcess(flowInstance.getId(), assignee);
            AssertHelper.isNull(assigneeProcess,
                    "当前不存在指定任务处理人的任务, assignee为: {}, 流程定义key为: {}, 业务id为: {}",
                    assignee, flowDefinitionKey, businessId);
            NodeHelper.buildTaskNode().execute(workFlowEngine.buildExecution(assigneeProcess, expression, flowData, operatorId, operatorName));
        }
        // 一般情况任务执行
        if (currentProcesses != null) {
            if(StringHelper.isBlank(flowData)) {
                flowData = currentProcesses.get(0).getFlowData();
            }
            NodeHelper.buildTaskNode().execute(workFlowEngine.buildExecution(currentProcesses.get(0), expression, flowData, operatorId, operatorName));
        }
    }
}
