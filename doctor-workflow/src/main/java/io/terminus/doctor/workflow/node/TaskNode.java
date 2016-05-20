package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;

/**
 * Desc: 任务节点
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/13
 */
public class TaskNode extends BaseNode {

    @Override
    protected void exec(Execution execution) {
        execution.getTransitions().forEach(transition ->{
            FlowDefinitionNode targetNode = execution.getWorkFlowService().getFlowQueryService().getFlowDefinitionNodeQuery()
                    .id(transition.getTargetNodeId())
                    .single();
            if(FlowDefinitionNode.Type.SUBSTART.value() != targetNode.getType()) {
                forward(execution.getHandler(transition.getHandler()), execution, transition);
            }
        });
    }
}
