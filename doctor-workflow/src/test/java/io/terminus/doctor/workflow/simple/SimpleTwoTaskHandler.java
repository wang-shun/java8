package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.base.handler.HandlerForceEnd;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

/**
 * Desc: 简单流程(含有事件)
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/16
 */
public class SimpleTwoTaskHandler extends BaseServiceTest{

    private String flowDefinitionKey = "simpleFlowHandler";
    private Long businessId = 1314L;

    @Test
    public void test_NORMAL_SimpleWorkFlowHandler() {
        // 1. 部署流程
        defService().deploy("simple/simple_two_task_handler.xml");
        // 2. 启动一个流程实例
        processService().startFlowInstance(flowDefinitionKey, businessId, "{businessData:100}", "{flowData:200}");
        // 3. 查询
        FlowInstance flowInstance = instanceQuery().getExistFlowInstance(flowDefinitionKey, businessId);
        Assert.assertNotNull(flowInstance);
        FlowProcess process = processQuery().getCurrentProcess(flowInstance.getId(), "terminus1");
        Assert.assertNotNull(process);
        // 5. 执行第一个任务
        processService().getExecutor(flowDefinitionKey, businessId).execute();
        process = processQuery().getCurrentProcess(flowInstance.getId(), "terminus2");
        Assert.assertNotNull(process);
        // 6. 执行第二个任务
        processService().getExecutor(flowDefinitionKey, businessId).execute();
        // 7. 最后查询
        flowInstance = instanceQuery().getExistFlowInstance(flowDefinitionKey, businessId);
        Assert.assertNull(flowInstance);
    }

    @Test
    public void test_ROLLBACK_SimpleWorkFlowHandler() {
        // 1. 部署流程
        defService().deploy("simple/simple_two_task_handler.xml");
        // 2. 启动一个流程实例
        processService().startFlowInstance(flowDefinitionKey, businessId, "{businessData:100}", "{flowData:200}");
        // 3. 查询
        FlowInstance flowInstance = instanceQuery().getExistFlowInstance(flowDefinitionKey, businessId);
        Assert.assertNotNull(flowInstance);
        FlowProcess process = processQuery().getCurrentProcess(flowInstance.getId(), "terminus1");
        Assert.assertNotNull(process);
        // 5. 执行第一个任务
        processService().getExecutor(flowDefinitionKey, businessId).execute();
        process = processQuery().getCurrentProcess(flowInstance.getId(), "terminus2");
        Assert.assertNotNull(process);

        // 回滚
        processService().rollBack(flowDefinitionKey, businessId);
        process = processQuery().getCurrentProcess(flowInstance.getId(), "terminus1");
        Assert.assertNotNull(process);
    }

    @Test
    public void test_FORCE_END_flowInstance() {
        // 1. 部署流程
        defService().deploy("simple/simple_two_task_handler.xml");
        // 2. 启动一个流程实例
        processService().startFlowInstance(flowDefinitionKey, businessId, "{businessData:100}", "{flowData:200}");
        // 3. 查询
        FlowInstance flowInstance = instanceQuery().getExistFlowInstance(flowDefinitionKey, businessId);
        Assert.assertNotNull(flowInstance);
        FlowProcess process = processQuery().getCurrentProcess(flowInstance.getId(), "terminus1");
        Assert.assertNotNull(process);
        // 5. 执行第一个任务
        processService().getExecutor(flowDefinitionKey, businessId).execute();
        process = processQuery().getCurrentProcess(flowInstance.getId(), "terminus2");
        Assert.assertNotNull(process);

        // 6. 强制结束流程实例
        processService().endFlowInstance(flowDefinitionKey, businessId, true, "强制结束实例", HandlerForceEnd.class);
        flowInstance = instanceQuery().getExistFlowInstance(flowDefinitionKey, businessId);
        Assert.assertNull(flowInstance);
    }
}
