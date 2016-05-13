package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
public class Simple extends BaseServiceTest {

    @Autowired
    private WorkFlowEngine workFlowEngine;

    private String flowDefinitionKey = "simpleFlow";
    private Long businessId = 1314L;

    @Test
    public void testDeploySimpleWorkFlow() {
        workFlowEngine.buildFlowDefinitionService().deploy("simple/simple.xml");
    }

    @Test
    public void testStartFlowInstance() {
        workFlowEngine.buildFlowProcessService().startFlowInstance(flowDefinitionKey, businessId);
    }

    @Test
    public void testGetCurrentTask() {
        // 1. 获取流程实例
        FlowInstance flowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .getExistFlowInstance(flowDefinitionKey, businessId);
        // 2. 获取当前执行的任务
        FlowProcess process = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                .getCurrentProcess(flowInstance.getId(), "terminus");
        System.out.println(process);
    }

    @Test
    public void testExecuteTask() {
        workFlowEngine.buildFlowProcessService().getExecutor(flowDefinitionKey, businessId)
                .execute();
    }
}
