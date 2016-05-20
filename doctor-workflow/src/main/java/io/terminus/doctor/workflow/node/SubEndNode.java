package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: 子流程结束节点
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/19
 */
public class SubEndNode extends BaseNode {

    @Override
    protected void exec(Execution execution) {
        execution.getWorkFlowService().getFlowProcessService()
                .getExecutor(execution.getFlowDefinitionKey(), execution.getBusinessId())
                .endSubFlowInstance(execution.getFlowProcess());
    }
}
