package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.junit.FixMethodOrder;
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
    public void testNormalSimpleWorkFlow() {
        // 1. 部署流程
        workFlowService.getFlowDefinitionService().deploy("simple/simple_two_task.xml");
        // 2. 启动一个流程实例
        workFlowService.getFlowProcessService().startFlowInstance(flowDefinitionKey, businessId);
        // 3. 查询
        FlowInstance flowInstance = workFlowService.getFlowQueryService().getFlowInstanceQuery()
                .getExistFlowInstance(flowDefinitionKey, businessId);
        FlowProcess process = workFlowService.getFlowQueryService().getFlowProcessQuery()
                .getCurrentProcess(flowInstance.getId(), "terminus");
        System.out.println(process);
        // 5. 执行第一个任务
        workFlowService.getFlowProcessService()
                .getExecutor(flowDefinitionKey, businessId)
                .execute();
        // 6. 执行第二个任务
        workFlowService.getFlowProcessService()
                .getExecutor(flowDefinitionKey, businessId)
                .execute();
    }
}
