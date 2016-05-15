package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: 任务节点
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/13
 */
public class TaskNode extends BaseNode {

    @Override
    protected void exec(Execution execution) {
        execution.getTransitions().forEach(transition ->
                forward(execution.getHandler(transition.getHandler()), execution, execution.getNextFlowProcess(transition)));
    }
}
