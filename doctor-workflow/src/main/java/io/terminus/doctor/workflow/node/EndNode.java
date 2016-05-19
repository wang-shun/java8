package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: 结束节点的执行
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/13
 */
public class EndNode extends BaseNode {

    @Override
    protected void exec(Execution execution) {
            execution.getWorkFlowService().getFlowProcessService()
                    .endFlowInstance(
                            execution.getFlowDefinitionKey(),
                            execution.getBusinessId(),
                            false,
                            "流程实例正常结束"
                    );
    }
}
