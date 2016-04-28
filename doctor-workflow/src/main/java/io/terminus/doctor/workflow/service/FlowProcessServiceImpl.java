package io.terminus.doctor.workflow.service;

import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.utils.AssertHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Autowired
    private FlowDefinitionService flowDefinitionService;


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
        // 1. 校验当前 businessId 是否存在流程实例
        FlowInstance existFlowInstance = findExistFlowInstance(flowDefinitionKey, businessId);
        AssertHelper.notNull(existFlowInstance,
                "当前流程定义已经存在流程实例, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);

        // 2. 获取最新的流程实例, 并创建流程实例
        FlowDefinition flowDefinition = flowDefinitionService.findLatestDefinitionByKey(flowDefinitionKey);
        AssertHelper.isNull(flowDefinition,
                "启动流程实例错误, 当前不存在key为: {} 的流程定义", flowDefinitionKey);
        AssertHelper.isNull(businessId,
                "启动流程实例的业务id不能为空, 流程定义key为: {}, 业务id为: {}", flowDefinitionKey, businessId);
        FlowInstance flowInstance = FlowInstance.builder()
                .name(flowDefinition.getName() + System.currentTimeMillis())
                .flowDefinitionId(flowDefinition.getId())
                .flowDefinitionKey(flowDefinition.getKey())
                .businessId(businessId)
                .businessData(businessData)
                .status(FlowInstance.Status.NORMAL.value())
                .operatorId(operatorId)
                .operatorName(operatorName)
                .parentInstanceId(-1L) // 新启动流程, 不存在父流程实例id
                .build();
        // 保存
    }

    /******************* 查询流程 相关 ********************************************/
    @Override
    public FlowInstance findExistFlowInstance(String flowDefinitionKey, Long businessId) {
        List<FlowInstance> flowInstances = access().findExistFlowInstance(flowDefinitionKey, businessId);
        if(flowInstances != null && flowInstances.size() > 0) {
            return flowInstances.get(0);
        }
        return  null;
    }

    private JdbcAccess access() {
        return workFlowEngine.buildJdbcAccess();
    }
}
