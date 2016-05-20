package io.terminus.doctor.workflow.sub;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desc: 选择节点简单测试
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/14
 */
public class SimpleSubTask extends BaseServiceTest {

    @Autowired
    private WorkFlowService workFlowService;

    private String flowDefinitionKey = "simpleSubFlow";
    private Long businessId = 1314L;

    @Test
    public void test_NORMAL_ForkJoinWorkFlow() {
        // 1. 部署流程
        defService().deploy("subflow/simple_sub_flow.xml");
        // 2. 启动流程实例
        processService().startFlowInstance(flowDefinitionKey, businessId);
        processService().getExecutor(flowDefinitionKey, businessId).startSubFlowInstance();

        // 3. 执行子流程任务1
        processService().getExecutor(flowDefinitionKey, businessId).execute();

        // 4. 执行子流程任务2, 子流程结束
        processService().getExecutor(flowDefinitionKey, businessId).execute();

        // 5. 执行任务1和2, 结束流程
        processService().getExecutor(flowDefinitionKey, businessId).execute();
        processService().getExecutor(flowDefinitionKey, businessId).execute();
    }

}
