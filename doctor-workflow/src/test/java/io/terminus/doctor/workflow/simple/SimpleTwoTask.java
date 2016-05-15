package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desc: 简单任务流程(两个任务节点)
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
public class SimpleTwoTask extends BaseServiceTest {

    @Autowired
    private WorkFlowService workFlowService;

    private String flowDefinitionKey = "simpleFlow";
    private Long businessId = 1314L;

    @Test
    public void testDeploySimpleWorkFlow() {
        workFlowService.getFlowDefinitionService().deploy("simple/simple_two_task.xml");
    }

    @Test
    public void testStartFlowInstance() {
        workFlowService.getFlowProcessService().startFlowInstance(flowDefinitionKey, businessId);
    }

    @Test
    public void testGetCurrentTask() {
        // 1. 获取流程实例
        FlowInstance flowInstance = workFlowService.getFlowQueryService().getFlowInstanceQuery()
                .getExistFlowInstance(flowDefinitionKey, businessId);
        // 2. 获取当前执行的任务
        FlowProcess process = workFlowService.getFlowQueryService().getFlowProcessQuery()
                .getCurrentProcess(flowInstance.getId(), "terminus");
        System.out.println(process);
    }

    @Test
    public void testExecuteTask() {
        workFlowService.getFlowProcessService()
                .getExecutor(flowDefinitionKey, businessId)
                .execute();
    }
}
