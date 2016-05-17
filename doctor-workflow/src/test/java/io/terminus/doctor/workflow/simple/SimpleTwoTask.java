package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowException;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

/**
 * Desc: 简单任务流程(两个任务节点)
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
public class SimpleTwoTask extends BaseServiceTest {

    private String flowDefinitionKey = "simpleFlow";
    private Long businessId = 1314L;

    @Test
    @Rollback(false)
    public void test_NORMAL_SimpleWorkFlow() {
        // 1. 部署流程
        defService().deploy("simple/simple_two_task.xml");
        // 2. 启动一个流程实例
        processService().startFlowInstance(flowDefinitionKey, businessId);
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
    public void test_ONE_MORE_SimpleWorkFlow() {
        // 1. 部署两个流程, 按照版本号递增
        defService().deploy("simple/simple_two_task.xml");
        defService().deploy("simple/simple_two_task.xml");
        // 2. 启动一个流程实例
        processService().startFlowInstance(flowDefinitionKey, businessId);
        // 3. 查询
        FlowInstance flowInstance = instanceQuery().getExistFlowInstance(flowDefinitionKey, businessId);
        FlowDefinition latestDefinition = defQuery().getLatestDefinitionByKey(flowDefinitionKey);
        FlowDefinition currDefinition = defQuery().id(flowInstance.getFlowDefinitionId()).single();
        Assert.assertEquals(latestDefinition.getId(), currDefinition.getId());
    }

    @Test
    public void test_DELETE_SimpleWorkFlow() {
        // 1. 部署流程
        defService().deploy("simple/simple_two_task.xml");
        FlowDefinition flowDefinition = defQuery().getLatestDefinitionByKey(flowDefinitionKey);
        // 2. 删除流程定义
        defService().delete(flowDefinition.getId());
        flowDefinition = defQuery().getLatestDefinitionByKey(flowDefinitionKey);
        Assert.assertNull(flowDefinition);
    }

    @Test(expected = WorkFlowException.class)
    public void test_DELETE_NOT_FORCE_SimpleWorkFlow() {
        // 1. 部署流程, 并启动一个流程实例
        defService().deploy("simple/simple_two_task.xml");
        processService().startFlowInstance(flowDefinitionKey, businessId);
        FlowDefinition flowDefinition = defQuery().getLatestDefinitionByKey(flowDefinitionKey);
        // 2. 删除流程定义
        defService().delete(flowDefinition.getId());
    }

    @Test
    public void test_DELETE_FORCE_SimpleWorkFlow() {
        // 1. 部署流程, 并启动一个流程实例
        defService().deploy("simple/simple_two_task.xml");
        processService().startFlowInstance(flowDefinitionKey, businessId);
        FlowDefinition flowDefinition = defQuery().getLatestDefinitionByKey(flowDefinitionKey);
        // 2. 删除流程定义
        defService().delete(flowDefinition.getId(), true);
        flowDefinition = defQuery().getLatestDefinitionByKey(flowDefinitionKey);
        Assert.assertNull(flowDefinition);
    }
}
