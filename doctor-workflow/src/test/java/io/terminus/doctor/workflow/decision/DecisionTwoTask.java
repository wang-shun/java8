package io.terminus.doctor.workflow.decision;

import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Desc:
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
    public void testDeploySimpleWorkFlow() {
        workFlowService.getFlowDefinitionService().deploy("decision/decision_two_task.xml");
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
                // .getCurrentProcess(flowInstance.getId(), "terminus");
                .getCurrentProcess(flowInstance.getId(), "terminus2");
        FlowDefinitionNode node = workFlowService.getFlowQueryService().getFlowDefinitionNodeQuery()
                .id(process.getFlowDefinitionNodeId())
                .single();
        System.out.println(node);
    }

    @Test
    public void testExecuteTask() {
        Map expression = Maps.newHashMap();
        expression.put("money",700);
        workFlowService.getFlowProcessService()
                .getExecutor(flowDefinitionKey, businessId)
                .execute();
                //.execute(expression);
    }
}
