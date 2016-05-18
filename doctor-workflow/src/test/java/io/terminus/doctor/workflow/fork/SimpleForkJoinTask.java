package io.terminus.doctor.workflow.fork;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Desc: 选择节点简单测试
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/14
 */
public class SimpleForkJoinTask extends BaseServiceTest {

    @Autowired
    private WorkFlowService workFlowService;

    private String flowDefinitionKey = "forkJoinFlow";
    private Long businessId = 1314L;

    @Test
    @Rollback(false)
    public void test_NORMAL_ForkJoinWorkFlow() {
        // 1. 部署流程
         defService().deploy("fork/simple_fork_join.xml");
        // 2. 启动流程实例
         processService().startFlowInstance(flowDefinitionKey, businessId);
        // 3. 执行任务节点0
         processService().getExecutor(flowDefinitionKey, businessId).execute();
        // 4. 执行任务节点1
         processService().getExecutor(flowDefinitionKey, businessId, "terminus1").execute();
        // 5. 执行任务节点11
         processService().getExecutor(flowDefinitionKey, businessId, "terminus11").execute();
        // 6. 执行任务节点2
        processService().getExecutor(flowDefinitionKey, businessId, "terminus2").execute();
    }

}
