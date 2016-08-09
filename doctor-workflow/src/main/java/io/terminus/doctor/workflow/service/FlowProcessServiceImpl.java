package io.terminus.doctor.workflow.service;

import com.google.common.base.Throwables;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.core.Executor;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.event.IHandler;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程流程相关的实现类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
@Slf4j
@Service
public class FlowProcessServiceImpl implements FlowProcessService {

    @Autowired
    private WorkFlowEngine workFlowEngine;

    /******************* 启动流程 相关 ********************************************/
    @Override
    public FlowInstance startFlowInstance(String flowDefinitionKey, Long businessId) {
        return startFlowInstance(flowDefinitionKey, businessId, null);
    }

    @Override
    public FlowInstance startFlowInstance(String flowDefinitionKey, Long businessId, String businessData) {
        return startFlowInstance(flowDefinitionKey, businessId, businessData, null);
    }

    @Override
    public FlowInstance startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData) {
        return startFlowInstance(flowDefinitionKey, businessId, businessData, flowData, null);
    }

    @Override
    public FlowInstance startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData, Map expression) {
        return startFlowInstance(flowDefinitionKey, businessId, businessData, flowData, expression, null, null);
    }

    @Override
    public FlowInstance startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData, Map expression, Long operatorId, String operatorName) {
        try {
            // 1. 校验当前 businessId 是否存在流程实例
            FlowInstance existFlowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                    .getExistFlowInstance(flowDefinitionKey, businessId);
            AssertHelper.notNull(existFlowInstance,
                    "当前流程定义已经存在流程实例, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);

            // 2. 获取最新的流程实例, 并创建流程实例
            FlowDefinition flowDefinition = workFlowEngine.buildFlowQueryService().
                    getFlowDefinitionQuery().getLatestDefinitionByKey(flowDefinitionKey);
            AssertHelper.isNull(flowDefinition,
                    "启动流程实例错误, 当前不存在key为: {} 的流程定义", flowDefinitionKey);
            AssertHelper.isNull(businessId,
                    "启动流程实例的业务id不能为空, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
            FlowInstance flowInstance = FlowInstance.builder()
                    .name(flowDefinition.getName() + System.currentTimeMillis())
                    .flowDefinitionId(flowDefinition.getId())
                    .flowDefinitionKey(flowDefinition.getKey())
                    .flowDefinitionKey(flowDefinition.getKey())
                    .businessId(businessId)
                    .businessData(businessData)
                    .status(FlowInstance.Status.NORMAL.value())
                    .type(FlowInstance.Type.PARENT.value())
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .parentInstanceId(-1L) // 新启动流程, 不存在父流程实例id
                    .build();
            access().createFlowInstance(flowInstance);

            // 3. 获取开始节点, 并执行任务
            FlowDefinitionNode startDefinitionNode = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                    .getDefinitionNodeByType(flowDefinition.getId(), FlowDefinitionNode.Type.START.value());
            AssertHelper.isNull(startDefinitionNode,
                    "当前流程的定义不存在开始节点, 流程定义id为: {}", flowDefinition.getId());
            FlowProcess startProcess = FlowProcess.builder()
                    .flowInstanceId(flowInstance.getId())
                    .preFlowDefinitionNodeId("-1") // 开始没有上一个节点
                    .flowDefinitionNodeId(startDefinitionNode.getId())
                    .flowData(flowData)
                    .assignee(startDefinitionNode.getAssignee())
                    .status(FlowProcess.Status.NORMAL.value())
                    .build();
            access().createFlowProcess(startProcess);

            // 4. 执行开始节点
            NodeHelper.buildStartNode().execute(workFlowEngine.buildExecution(startProcess, expression, flowData, operatorId, operatorName));

            return flowInstance;
        } catch (Exception e) {
            log.error("[Work Flow Instance] -> 启动流程实例失败, cause by: {}", Throwables.getStackTraceAsString(e));
            AssertHelper.throwException("启动流程实例失败, cause by: {}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /******************* 执行流程 相关 ********************************************/
    @Override
    public Executor getExecutor(String flowDefinitionKey, Long businessId) {
        return getExecutor(flowDefinitionKey, businessId, null);
    }

    @Override
    public Executor getExecutor(String flowDefinitionKey, Long businessId, String assignee) {
        return workFlowEngine.buildExecutor(flowDefinitionKey, businessId, assignee);
    }

    @Override
    public void endFlowInstance(String flowDefinitionKey, Long businessId) {
        endFlowInstance(flowDefinitionKey, businessId, false, null);
    }

    @Override
    public void endFlowInstance(String flowDefinitionKey, Long businessId, boolean isForce, String describe) {
        endFlowInstance(flowDefinitionKey, businessId, isForce, describe, null);
    }

    @Override
    public void endFlowInstance(String flowDefinitionKey, Long businessId, boolean isForce, String describe, Class<? extends IHandler> handler) {
        endFlowInstance(flowDefinitionKey, businessId, isForce, describe, null, null, handler);
    }

    /******************* 结束流程 相关 ********************************************/
    @Override
    public void endFlowInstance(String flowDefinitionKey, Long businessId, boolean isForce, String describe, Long operatorId, String operatorName, Class<? extends IHandler> handler) {
        // 如果不强制删除(正常结束)
        if (!isForce) {
            // 1. 校验流程实例, 和子流程实例是否存在
            List<FlowInstance> childFlowInstances = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                    .getExistChildFlowInstance(flowDefinitionKey, businessId);
            if (childFlowInstances != null && childFlowInstances.size() > 0) {
                AssertHelper.throwException("当前存在子流程实例, 不能结束流程, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
            }
            FlowInstance existFlowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                    .getExistFlowInstance(flowDefinitionKey, businessId);
            AssertHelper.isNull(existFlowInstance,
                    "主流程实例不存在, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
            // 2. 删除结束节点, 和流程实例
            List<FlowProcess> currentProcesses = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                    .getCurrentProcesses(existFlowInstance.getId());
            if (currentProcesses == null || currentProcesses.size() == 0) {
                AssertHelper.throwException("当前不存在流转的任务节点, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
            }
            if (currentProcesses.size() > 1) {
                AssertHelper.throwException("当前存在多个任务节点, 不能结束流程, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
            }
            FlowProcess currentProcess = currentProcesses.get(0);
            FlowDefinitionNode processNode = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                    .id(currentProcess.getFlowDefinitionNodeId())
                    .single();
            if (FlowDefinitionNode.Type.END.value() != processNode.getType()) {
                AssertHelper.throwException("当前存在正在执行的任务节点, 不能结束流程, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
            }
            access().deleteFlowProcess(currentProcess.getId());
            access().deleteFlowInstance(existFlowInstance.getId());

            // 记录历史活动节点
            FlowHistoryProcess flowHistoryProcess = FlowHistoryProcess.builder().build();
            BeanHelper.copy(flowHistoryProcess, currentProcess);
            flowHistoryProcess.setDescribe(describe);
            flowHistoryProcess.setOperatorId(operatorId);
            flowHistoryProcess.setOperatorName(operatorName);
            flowHistoryProcess.setStatus(FlowProcess.Status.END.value());
            access().createFlowHistoryProcess(flowHistoryProcess);
            // 记录track
            FlowProcessTrack flowProcessTrack = FlowProcessTrack.builder().build();
            BeanHelper.copy(flowProcessTrack, currentProcess);
            flowProcessTrack.setOperatorId(operatorId);
            flowProcessTrack.setOperatorName(operatorName);
            access().createFlowProcessTrack(flowProcessTrack);
            // 记录历史流程实例
            FlowHistoryInstance flowHistoryInstance = FlowHistoryInstance.builder()
                    .describe(describe)
                    .build();
            BeanHelper.copy(flowHistoryInstance, existFlowInstance);
            flowHistoryInstance.setStatus(FlowInstance.Status.END.value());
            flowHistoryInstance.setExternalHistoryId(existFlowInstance.getId());
            access().createFlowHistoryInstance(flowHistoryInstance);
            // 删除所有的流程跟踪 (暂不考虑删除)
            /*access().deleteFlowProcessTrack(
                    workFlowEngine.buildFlowQueryService().getFlowProcessTrackQuery()
                            .flowInstanceId(existFlowInstance.getId())
                            .list()
                            .stream()
                            .map(FlowProcessTrack::getId)
                            .collect(Collectors.toList())
            );*/
        }
        // 如果强制删除
        else {
            // 1. 删除主流程实例和子流程实例, 以及对应的正在执行的任务节点
            List<FlowInstance> flowInstances = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                    .flowDefinitionKey(flowDefinitionKey)
                    .businessId(businessId)
                    .list();
            boolean flag = false; // 是否已经执行过了结束的handler
            for (int i = 0; flowInstances != null && i < flowInstances.size(); i++) {
                FlowInstance flowInstance = flowInstances.get(i);
                List<FlowProcess> flowProcesses = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                        .flowInstanceId(flowInstance.getId())
                        .list();
                for (int j = 0; flowProcesses != null && j < flowProcesses.size(); j++) {
                    FlowProcess flowProcess = flowProcesses.get(j);
                    // 执行handler
                    if (!flag && handler != null) {
                        IHandler iHandler = workFlowEngine.buildContext().get(StringHelper.uncapitalize(handler.getSimpleName()));
                        if (iHandler != null) {
                            Execution execution = workFlowEngine.buildExecution(
                                    flowProcess,
                                    null,
                                    null,
                                    operatorId,
                                    operatorName
                            );
                            iHandler.preHandle(execution);
                            iHandler.handle(execution);
                            iHandler.afterHandle(execution);
                            flag = true;
                        }
                    }
                    access().deleteFlowProcess(flowProcess.getId());
                    // 记录历史活动节点
                    FlowHistoryProcess flowHistoryProcess = FlowHistoryProcess.builder().build();
                    BeanHelper.copy(flowHistoryProcess, flowProcess);
                    flowHistoryProcess.setDescribe(describe);
                    flowHistoryProcess.setOperatorId(operatorId);
                    flowHistoryProcess.setOperatorName(operatorName);
                    flowHistoryProcess.setStatus(FlowProcess.Status.DELETE.value());
                    access().createFlowHistoryProcess(flowHistoryProcess);
                    // 记录track
                    FlowProcessTrack flowProcessTrack = FlowProcessTrack.builder().build();
                    BeanHelper.copy(flowProcessTrack, flowProcess);
                    flowProcessTrack.setOperatorId(operatorId);
                    flowProcessTrack.setOperatorName(operatorName);
                    access().createFlowProcessTrack(flowProcessTrack);
                }
                access().deleteFlowInstance(flowInstance.getId());
                // 记录历史流程实例
                FlowHistoryInstance flowHistoryInstance = FlowHistoryInstance.builder().build();
                BeanHelper.copy(flowHistoryInstance, flowInstance);
                flowHistoryInstance.setDescribe(describe);
                flowHistoryInstance.setOperatorId(operatorId);
                flowHistoryInstance.setOperatorName(operatorName);
                flowHistoryInstance.setStatus(FlowInstance.Status.DELETE.value());
                flowHistoryInstance.setExternalHistoryId(flowInstance.getId());
                access().createFlowHistoryInstance(flowHistoryInstance);

                // 删除所有的流程跟踪 (暂不考虑删除)
                /*access().deleteFlowProcessTrack(
                        workFlowEngine.buildFlowQueryService().getFlowProcessTrackQuery()
                                .flowInstanceId(flowInstance.getId())
                                .list()
                                .stream()
                                .map(FlowProcessTrack::getId)
                                .collect(Collectors.toList())
                );*/
            }
        }
    }

    /******************* 回滚流程 相关 ********************************************/
    @Override
    public void rollBack(String flowDefinitionKey, Long businessId) {
        rollBack(flowDefinitionKey, businessId, 1, null, null);
    }

    @Override
    public void rollBack(String flowDefinitionKey, Long businessId, Long operatorId, String operatorName) {
        rollBack(flowDefinitionKey, businessId, 1, operatorId, operatorName);
    }

    @Override
    public void rollBack(String flowDefinitionKey, Long businessId, int depth) {
        rollBack(flowDefinitionKey, businessId, depth, null, null);
    }

    @Override
    public void rollBack(String flowDefinitionKey, Long businessId, int depth, Long operatorId, String operatorName) {
        if (depth < 1) {
            AssertHelper.throwException(
                    "当前流程回滚操作深度不能小于1, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
        }
        // TODO 子流程回滚情况...

        // 获取主流程实例
        FlowInstance mainFlowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .getExistFlowInstance(flowDefinitionKey, businessId);
        AssertHelper.isNull(mainFlowInstance,
                "主流程实例不存在, 无法回滚, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
        List<FlowProcess> currentProcesses = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                .getCurrentProcesses(mainFlowInstance.getId());
        // 1. 如果当前只存在一个执行任务
        if(currentProcesses != null && currentProcesses.size() == 1) {
            FlowDefinitionNode currNode = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                    .id(currentProcesses.get(0).getFlowDefinitionNodeId())
                    .single();
            List<FlowProcessTrack> flowTracks = workFlowEngine.buildFlowQueryService().getFlowProcessTrackQuery()
                    .flowInstanceId(mainFlowInstance.getId())
                    .desc()
                    .list();
            int count = 0;
            FlowProcessTrack currTrack = null;
            for (FlowProcessTrack flowTrack : flowTracks) {
                FlowDefinitionNode trackNode = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                        .id(flowTrack.getFlowDefinitionNodeId())
                        .type(FlowDefinitionNode.Type.TASK.value())
                        .single();
                if (trackNode == null)
                    continue;
                count ++;
                if(count == depth) {
                    currTrack = flowTrack;
                    break;
                }
            }
            AssertHelper.notEquals(count, depth,
                    "回滚的深度过大, 不能执行回滚操作, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
            // 回滚
            access().deleteFlowProcess(currentProcesses.get(0).getId());
            access().deleteFlowProcessTrack(currTrack.getId());
            FlowProcess rollBackProcess = FlowProcess.builder().build();
            BeanHelper.copy(rollBackProcess, currTrack);
            access().createFlowProcess(rollBackProcess);
            // 记录历史
            FlowHistoryProcess hisProcess = FlowHistoryProcess.builder().build();
            BeanHelper.copy(hisProcess, rollBackProcess);
            hisProcess.setOperatorId(operatorId);
            hisProcess.setOperatorName(operatorName);
            hisProcess.setStatus(FlowProcess.Status.END.value());
            hisProcess.setDescribe("任务节点[" + currNode.getName() + "], 回滚成功");
            access().createFlowHistoryProcess(hisProcess);
        }
        // 2. 如果存在多个任务 TODO


    }

    private JdbcAccess access() {
        return workFlowEngine.buildJdbcAccess();
    }
}
