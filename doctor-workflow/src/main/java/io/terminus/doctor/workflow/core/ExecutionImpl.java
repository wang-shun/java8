package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.event.Interceptor;

import java.util.List;

/**
 * Desc: 流程节点执行器
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/29
 */
public class ExecutionImpl implements Execution {

    private WorkFlowEngine workFlowEngine;

    private String flowDefinitionKey;
    private Long businessId;

    public ExecutionImpl(WorkFlowEngine workFlowEngine, String flowDefinitionKey, Long businessId) {
        this.workFlowEngine = workFlowEngine;
        this.flowDefinitionKey = flowDefinitionKey;
        this.businessId = businessId;
    }

    @Override
    public List<Interceptor> getInterceptors() {
        return workFlowEngine.buildInterceptors();
    }

    @Override
    public WorkFlowService getWorkFlowService() {
        return workFlowEngine.buildWorkFlowService();
    }
}
