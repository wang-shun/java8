package io.terminus.doctor.event.test;

import io.terminus.doctor.event.service.SimpleFlowService;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.List;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/19
 */
public class TestSimpleFowServive extends BaseServiceTest {

    @Autowired
    private SimpleFlowService simpleFlowService;

    private String flowDefinitionKey = "FemaleSimpleFlow";

    private Long pigId = 1314L;

    @Test
    public void testDeploy() {
        // 1. 部署流程, 并查询
        simpleFlowService.depoly(TestSimpleFowServive.class.getClassLoader().getResourceAsStream("flow/simple.xml"));
        List<FlowDefinition> flowDefinitions = simpleFlowService.getFlowDefinitions(flowDefinitionKey);
        Assert.assertNotNull(flowDefinitions);
        Assert.assertEquals(1, flowDefinitions.size());
        // 2. 启动流程实例, 此时执行了配种事件, 当前处于怀孕状态
        simpleFlowService.startFlowInstance(flowDefinitionKey, pigId);
        FlowInstance currentInstance = simpleFlowService.getCurrentInstance(flowDefinitionKey, pigId);
        Assert.assertNotNull(currentInstance);
        List<FlowProcess> currentProcess = simpleFlowService.getCurrentProcess(currentInstance.getId());
        Assert.assertEquals(1, currentProcess.size());

        // 3. 执行分娩事件, 当前处于哺乳状态
        simpleFlowService.execute(flowDefinitionKey, pigId);

        // 4. 执行断奶事件, 当前处于已断奶状态
        simpleFlowService.execute(flowDefinitionKey, pigId);

        // 5. 执行离场事件, 流程实例结束
        simpleFlowService.execute(flowDefinitionKey, pigId);

        currentInstance = simpleFlowService.getCurrentInstance(flowDefinitionKey, pigId);
        Assert.assertNull(currentInstance);
    }
}
