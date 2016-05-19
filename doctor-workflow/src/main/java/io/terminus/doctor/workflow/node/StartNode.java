package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: 开始节点
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/29
 */
public class StartNode extends BaseNode {

    @Override
    protected void exec(Execution execution) {
        execution.getTransitions().forEach(transition ->
                forward(execution.getHandler(transition.getHandler()), execution, transition));
    }
}
