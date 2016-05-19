package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: Fork节点处理
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/17
 */
public class ForkNode extends BaseNode {

    @Override
    protected void exec(Execution execution) {
        execution.getTransitions().forEach(transition ->
                forward(execution.getHandler(transition.getHandler()), execution, transition));
    }

}
