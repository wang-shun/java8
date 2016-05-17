package io.terminus.doctor.workflow.decision;

import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.Map;

/**
 * Desc: 选择节点简单测试
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/14
 */
public class DecisionTwoTask extends BaseServiceTest {

    @Autowired
    private WorkFlowService workFlowService;

    private String flowDefinitionKey = "decisionFlow";
    private Long businessId = 1314L;

    @Test
    @Rollback(false)
    public void test_NORMAL_DecisionWorkFlow() {
        // 1. 部署流程
        defService().deploy("decision/decision_two_task.xml");
        // 2. 启动流程实例
        processService().startFlowInstance(flowDefinitionKey, businessId);
        // 3. 执行第一个任务
        Map expression = Maps.newHashMap();
        expression.put("money",700);
        processService().getExecutor(flowDefinitionKey, businessId).execute(expression);
        // 4. 查询, 当前任务应该为 "任务节点2"
        FlowInstance flowInstance = instanceQuery().getExistFlowInstance(flowDefinitionKey, businessId);
        Assert.assertNotNull(flowInstance);
        FlowProcess process = processQuery().getCurrentProcess(flowInstance.getId(), "terminus2");
        Assert.assertNotNull(process);
        // 5. 执行 任务节点2
        processService().getExecutor(flowDefinitionKey, businessId).execute();
    }

    @Test
    public void test_NORMAL1_DecisionWorkFlow() {
        // 1. 部署流程
        defService().deploy("decision/decision_two_task.xml");
        // 2. 启动流程实例
        processService().startFlowInstance(flowDefinitionKey, businessId);
        // 3. 执行第一个任务
        Map expression = Maps.newHashMap();
        expression.put("money",1001);
        workFlowService.getFlowProcessService()
                .getExecutor(flowDefinitionKey, businessId)
                .execute(expression);
        // 4. 查询, 流程实例应该结束
        FlowInstance flowInstance = instanceQuery().getExistFlowInstance(flowDefinitionKey, businessId);
        Assert.assertNull(flowInstance);
    }
}
