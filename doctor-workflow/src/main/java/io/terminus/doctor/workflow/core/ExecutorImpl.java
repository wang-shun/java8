package io.terminus.doctor.workflow.core;

import com.google.common.base.Throwables;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowHistoryInstance;
import io.terminus.doctor.workflow.model.FlowHistoryProcess;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.model.FlowProcessTrack;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.BeanHelper;
import io.terminus.doctor.workflow.utils.NodeHelper;
import io.terminus.doctor.workflow.utils.StringHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Desc: 执行器, 用来执行节点的操作
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/13
 */
@Slf4j
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
        try{
            // 获取正在执行的流程实例
            FlowInstance flowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                    .flowDefinitionKey(flowDefinitionKey)
                    .businessId(businessId)
                    .status(FlowInstance.Status.NORMAL.value())
                    .single();
            AssertHelper.isNull(flowInstance,
                    "执行节点的流程实例不存在, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);

            // 获取当前正在执行的节点
            FlowProcess currentProcess = getCurrentProcess(flowInstance.getId());
            if(StringHelper.isBlank(flowData)) {
                flowData = currentProcess.getFlowData();
            }
            NodeHelper.buildTaskNode().execute(workFlowEngine.buildExecution(currentProcess, expression, flowData, operatorId, operatorName));

        }catch (IllegalStateException e){
           // log.error("[Flow Process Execute] -> 流程任务执行异常, cause by {}", Throwables.getStackTraceAsString(e));
            throw e;
        }catch (Exception e) {
            log.error("[Flow Process Execute] -> 流程任务执行异常, cause by {}", Throwables.getStackTraceAsString(e));
            AssertHelper.throwException("[Flow Process Execute] -> 流程任务执行异常, cause by {}", Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public FlowInstance startSubFlowInstance() {
        return startSubFlowInstance(null, null, null, null);
    }

    @Override
    public FlowInstance startSubFlowInstance(String flowData) {
        return startSubFlowInstance(flowData, null, null, null);
    }

    @Override
    public FlowInstance startSubFlowInstance(Map expression) {
        return startSubFlowInstance(null, expression, null, null);
    }

    @Override
    public FlowInstance startSubFlowInstance(String flowData, Map expression) {
        return startSubFlowInstance(flowData, expression, null, null);
    }

    @Override
    public FlowInstance startSubFlowInstance(String flowData, Map expression, Long operatorId, String operatorName) {
        try{
            // 1. 查看当前处于正常状态的流程实例, 找出子流程的开始节点
            FlowInstance normalInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                    .flowDefinitionKey(flowDefinitionKey)
                    .businessId(businessId)
                    .status(FlowInstance.Status.NORMAL.value())
                    .single();
            FlowProcess currentProcess = getCurrentProcess(normalInstance.getId());
            List<FlowDefinitionNodeEvent> transitions = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeEventQuery()
                    .getNodeEventsBySourceId(
                            normalInstance.getFlowDefinitionId(), currentProcess.getFlowDefinitionNodeId());
            FlowDefinitionNode subStartNode = null;
            for (int i = 0; transitions != null && i < transitions.size(); i++) {
                FlowDefinitionNode targetNode = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                        .id(transitions.get(i).getTargetNodeId())
                        .single();
                if (FlowDefinitionNode.Type.SUBSTART.value() == targetNode.getType()) {
                    subStartNode = targetNode;
                    break;
                }
            }
            if (subStartNode == null) {
                AssertHelper.throwException(
                        "当前节点不存在子流程开始节点, , 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
            }

            // 2. 将当前流程实例挂起, 并创建子流程实例
            normalInstance.setStatus(FlowInstance.Status.STOPED.value());
            workFlowEngine.buildJdbcAccess().updateFlowInstance(normalInstance);
            FlowInstance subInstance = FlowInstance.builder().build();
            BeanHelper.copy(subInstance, normalInstance);
            subInstance.setParentInstanceId(normalInstance.getId());
            subInstance.setStatus(FlowInstance.Status.NORMAL.value());
            subInstance.setType(FlowInstance.Type.CHILD.value());
            workFlowEngine.buildJdbcAccess().createFlowInstance(subInstance);

            // 3. 创建并执行子流程开始节点
            FlowProcess subStartProcess = FlowProcess.builder()
                    .flowDefinitionNodeId(subStartNode.getId())
                    .preFlowDefinitionNodeId(currentProcess.getFlowDefinitionNodeId() + "")
                    .flowInstanceId(subInstance.getId())
                    .flowData(flowData)
                    .status(FlowProcess.Status.NORMAL.value())
                    .assignee(subStartNode.getAssignee())
                    .build();
            workFlowEngine.buildJdbcAccess().createFlowProcess(subStartProcess);
            NodeHelper.buildSubStartNode().execute(workFlowEngine.buildExecution(subStartProcess, expression, flowData, operatorId, operatorName));

            return subInstance;
        } catch (Exception e) {
            log.error("[Sub Flow Instance] -> 子流程启动异常, cause by {}", Throwables.getStackTraceAsString(e));
            AssertHelper.throwException("[Sub Flow Instance] -> 子流程启动异常, cause by {}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 获取当前正在执行的节点
     */
    private FlowProcess getCurrentProcess(Long flowInstanceId) {

        // 获取当前活动的任务节点
        List<FlowProcess> currentProcesses = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                .getCurrentProcesses(flowInstanceId);
        if (currentProcesses == null || currentProcesses.size() == 0) {
            AssertHelper.throwException("当前没有可以执行的任务, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
        }
        // 如果是fork-join情况
        if (currentProcesses != null && currentProcesses.size() > 1) {
            AssertHelper.isBlank(assignee,
                    "当前存在多个任务, 请指定任务处理者, 以便于判断执行哪一个任务, 流程定义key为: {}, 业务id为: {}",
                    flowDefinitionKey, businessId);
            FlowProcess assigneeProcess = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                    .getCurrentProcess(flowInstanceId, assignee);
            AssertHelper.isNull(assigneeProcess,
                    "当前不存在指定任务处理人的任务, assignee为: {}, 流程定义key为: {}, 业务id为: {}",
                    assignee, flowDefinitionKey, businessId);
            return assigneeProcess;
        }
        // 一般情况任务执行
        if (currentProcesses != null && currentProcesses.size() == 1) {
            return currentProcesses.get(0);
        }
        return null;
    }

    @Override
    public void endSubFlowInstance(FlowProcess flowProcess) {
        endSubFlowInstance(flowProcess, null, null);
    }

    @Override
    public void endSubFlowInstance(FlowProcess flowProcess, Long operatorId, String operatorName) {
        // 获取当前正在执行的流程实例
        FlowInstance currentInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .id(flowProcess.getFlowInstanceId())
                .single();
        FlowDefinitionNode processNode = workFlowEngine.buildFlowQueryService()
                .getFlowDefinitionNodeQuery()
                .id(flowProcess.getFlowDefinitionNodeId())
                .single();
        if (FlowDefinitionNode.Type.SUBEND.value() != processNode.getType()) {
            AssertHelper.throwException(
                    "当前存在正在执行的任务节点, 不能结束当前子流程流程, 流程定义key为: {}, 业务id为: {}",
                    flowDefinitionKey,
                    businessId);
        }
        workFlowEngine.buildJdbcAccess().deleteFlowProcess(flowProcess.getId());
        workFlowEngine.buildJdbcAccess().deleteFlowInstance(currentInstance.getId());

        // 记录历史活动节点
        FlowHistoryProcess flowHistoryProcess = FlowHistoryProcess.builder().build();
        BeanHelper.copy(flowHistoryProcess, flowProcess);
        flowHistoryProcess.setDescribe("[子流程结束节点] -> 正常结束");
        flowHistoryProcess.setOperatorId(operatorId);
        flowHistoryProcess.setOperatorName(operatorName);
        flowHistoryProcess.setStatus(FlowProcess.Status.END.value());
        workFlowEngine.buildJdbcAccess().createFlowHistoryProcess(flowHistoryProcess);
        // 记录track
        FlowProcessTrack flowProcessTrack = FlowProcessTrack.builder().build();
        BeanHelper.copy(flowProcessTrack, flowProcess);
        flowProcessTrack.setOperatorId(operatorId);
        flowProcessTrack.setOperatorName(operatorName);
        workFlowEngine.buildJdbcAccess().createFlowProcessTrack(flowProcessTrack);
        // 记录历史流程实例
        FlowHistoryInstance flowHistoryInstance = FlowHistoryInstance.builder()
                .describe("[子流程实例] -> 正常结束")
                .build();
        BeanHelper.copy(flowHistoryInstance, currentInstance);
        flowHistoryInstance.setParentInstanceId(null);
        flowHistoryInstance.setStatus(FlowInstance.Status.END.value());
        flowHistoryInstance.setExternalHistoryId(currentInstance.getId());
        workFlowEngine.buildJdbcAccess().createFlowHistoryInstance(flowHistoryInstance);

        // 将父流程恢复
        FlowInstance parentInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .id(currentInstance.getParentInstanceId())
                .single();
        if (parentInstance != null) {
            parentInstance.setStatus(FlowInstance.Status.NORMAL.value());
            workFlowEngine.buildJdbcAccess().updateFlowInstance(parentInstance);
        }
    }
}
