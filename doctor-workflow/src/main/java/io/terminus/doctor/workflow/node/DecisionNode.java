package io.terminus.doctor.workflow.node;

import com.google.common.base.Throwables;
import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.StringHelper;

/**
 * Desc: 选择节点(唯一网关)
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/14
 */
public class DecisionNode extends BaseNode {

    @Override
    protected void exec(Execution execution) {
        int onlyOk = 0; // 是否只有一个能够执行
        FlowDefinitionNodeEvent defaultTransition = null;
        FlowDefinitionNodeEvent goTransition = null;
        for (FlowDefinitionNodeEvent transition : execution.getTransitions()) {
            String expression = transition.getExpression();
            if (StringHelper.isBlank(expression)) {
                defaultTransition = transition;
                continue;
            }
            try {
                if (StringHelper.parseExpression(expression, execution.getExpression())) {
                    onlyOk++;
                    goTransition = transition;
                }
            } catch (Exception e) {
                AssertHelper.throwException("表达式解析失败, 请查看判断表达式是否正确, 事件表达式为: {}, 执行参数为: {}, cause by: {}",
                        transition.getExpression(), execution.getExpression(), Throwables.getStackTraceAsString(e));
            }
        }
        // 如果存在多个符合节点
        if (onlyOk > 1) {
            AssertHelper.throwException("decision节点找到了多个可执行的连接事件, 事件表达式为: {}, 执行参数为: {}",
                    goTransition.getExpression(), execution.getExpression());
        }

        // 如果不存在符合节点, 走默认节点, 默认节点为空, 抛出异常
        if (onlyOk == 0) {
            goTransition = defaultTransition;
        }
        AssertHelper.isNull(goTransition,
                "decision节点没有找到可执行的连接事件, 事件表达式为: {}, 执行参数为: {}", goTransition.getExpression(), execution.getExpression());

        // 执行handler
        forward(execution.getHandler(goTransition.getHandler()), execution, execution.getNextFlowProcess(goTransition));
    }

}
