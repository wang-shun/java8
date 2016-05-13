package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.model.FlowInstance;

/**
 * Desc: 结束节点的执行
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/13
 */
public class EndNode extends BaseNode {

    @Override
    protected void exec(Execution execution) {
        FlowInstance flowInstance = execution.getWorkFlowService().getFlowQueryService().getFlowInstanceQuery()
                .id(execution.getFlowProcess().getFlowInstanceId())
                .single();
        if(flowInstance != null) {
            execution.getWorkFlowService().getFlowProcessService()
                    .endFlowInstance(flowInstance.getFlowDefinitionKey(), flowInstance.getBusinessId());
        }
    }
}
