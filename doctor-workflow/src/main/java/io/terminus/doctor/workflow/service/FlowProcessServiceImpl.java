package io.terminus.doctor.workflow.service;

import com.google.common.base.Throwables;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.core.WorkFlowException;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.AssertHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void startFlowInstance(String flowDefinitionKey, Long businessId) {
        startFlowInstance(flowDefinitionKey, businessId, null);
    }

    @Override
    public void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData) {
        startFlowInstance(flowDefinitionKey, businessId, businessData, null);
    }

    @Override
    public void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData) {
        startFlowInstance(flowDefinitionKey, businessId, businessData, flowData, null, null);
    }

    @Override
    public void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData, Long operatorId, String operatorName) {
        try {
            // 1. 校验当前 businessId 是否存在流程实例
            FlowInstance existFlowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                    .findExistFlowInstance(flowDefinitionKey, businessId);
            AssertHelper.notNull(existFlowInstance,
                    "当前流程定义已经存在流程实例, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);

            // 2. 获取最新的流程实例, 并创建流程实例
            FlowDefinition flowDefinition = workFlowEngine.buildFlowQueryService().
                    getFlowDefinitionQuery().findLatestDefinitionByKey(flowDefinitionKey);
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
                    .findDefinitionNodeByType(flowDefinition.getId(), FlowDefinitionNode.Type.START.value());
            AssertHelper.isNull(startDefinitionNode,
                    "当前流程的定义不存在开始节点, 流程定义id为: {}", flowDefinition.getId());
            FlowProcess startProcess = FlowProcess.builder()
                    .flowInstanceId(flowInstance.getId())
                    .flowDefinitionNodeId(startDefinitionNode.getId())
                    .flowData(flowData)
                    .assignee(startDefinitionNode.getAssignee())
                    .status(FlowProcess.Status.NORMAL.value())
                    .build();
            access().createFlowProcess(startProcess);

            // 4. 执行任务 TODO


        }catch (Exception e) {
            log.error("[Work Flow Instance] -> 启动流程实例失败, cause by: {}", Throwables.getStackTraceAsString(e));
            throw new WorkFlowException(e);
        }
    }

    private JdbcAccess access() {
        return workFlowEngine.buildJdbcAccess();
    }
}
