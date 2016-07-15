package io.terminus.doctor.workflow.node;

import com.google.common.base.Throwables;
import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.IHandler;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.NodeHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Desc: 流程节点基础接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/29
 */
@Slf4j
public abstract class BaseNode implements Node {

    @Override
    public void execute(Execution execution) {
        preIntercept(execution.getInterceptors(), execution);
        exec(execution);
        afterIntercept(execution.getInterceptors(), execution);
    }

    protected abstract void exec(Execution execution);

    /**
     * 执行拦截器 前置
     *
     * @param interceptors
     */
    private void preIntercept(List<Interceptor> interceptors, Execution execution) {
        try {
            if (interceptors != null && interceptors.size() > 0) {
                interceptors.forEach(i -> i.before(execution));
            }
        } catch (Exception e) {
            log.error("[interceptor invoke] -> 拦截器前置方法执行失败");
        }
    }

    /**
     * 执行拦截器 后置
     *
     * @param interceptors
     */
    private void afterIntercept(List<Interceptor> interceptors, Execution execution) {
        try {
            if (interceptors != null && interceptors.size() > 0) {
                interceptors.forEach(i -> i.after(execution));
            }
        } catch (Exception e) {
            log.error("[interceptor invoke] -> 拦截器后置方法执行失败");
        }
    }

    protected void forward(IHandler handler, Execution execution, FlowDefinitionNodeEvent transition) {
        // 1. 执行节点
        try{
            if (handler != null) {
                handler.preHandle(execution);
                handler.handle(execution);
                handler.afterHandle(execution);
            }
        } catch (IllegalStateException e){
            log.error("[handler execute] -> handler execute failed cause by {}", Throwables.getStackTraceAsString(e));
            throw e;
        }catch (Exception e) {
            log.error("[handler execute] -> handler execute failed cause by {}", Throwables.getStackTraceAsString(e));
            AssertHelper.throwException("[handler execute] -> handler [{}] execute failed cause by {}",
                    handler.getClass().getSimpleName(), Throwables.getStackTraceAsString(e));
        }


        FlowProcess nextProcess = execution.getNextFlowProcess(transition);
        if (nextProcess != null) {

            // 2. 判断节点类型, 执行下个节点
            FlowDefinitionNode nextNode = execution.getWorkFlowService().getFlowQueryService().getFlowDefinitionNodeQuery()
                    .id(nextProcess.getFlowDefinitionNodeId())
                    .single();
            // 结束节点, 继续执行
            if (FlowDefinitionNode.Type.END.value() == nextNode.getType()) {
                goEnd(nextProcess, execution);
            }
            // 选择节点
            else if (FlowDefinitionNode.Type.DECISION.value() == nextNode.getType()) {
                goDecision(nextProcess, execution);
            }
            // fork 节点
            else if (FlowDefinitionNode.Type.FORK.value() == nextNode.getType()) {
                goFork(nextProcess, execution);
            }
            // join 节点
            else if (FlowDefinitionNode.Type.JOIN.value() == nextNode.getType()) {
                goJoin(nextProcess, execution);
            }
            // 任务节点
            else if (FlowDefinitionNode.Type.TASK.value() == nextNode.getType()) {
                goTask(nextProcess, execution);
            }
            // 子流程结束节点
            else if (FlowDefinitionNode.Type.SUBEND.value() == nextNode.getType()) {
                goSubEnd(nextProcess, execution);
            }
        }
    }

    /**
     * end 节点执行
     */
    private void goEnd(FlowProcess nextProcess, Execution execution) {
        execution.createNextFlowProcess(nextProcess, true);
        NodeHelper.buildEndNode().execute(execution.getExecution(nextProcess));
    }

    /**
     * task 节点执行
     */
    private void goTask(FlowProcess nextProcess, Execution execution) {
        execution.createNextFlowProcess(nextProcess, true);
        // 暂停 task 节点, 不执行
    }

    /**
     * decision 节点执行
     */
    private void goDecision(FlowProcess nextProcess, Execution execution) {
        execution.createNextFlowProcess(nextProcess, true);
        NodeHelper.buildDecisionNode().execute(execution.getExecution(nextProcess));
    }

    /**
     * fork 节点执行
     */
    private void goFork(FlowProcess nextProcess, Execution execution) {
        execution.createNextFlowProcess(nextProcess, true);
        nextProcess.setForkNodeId(nextProcess.getFlowDefinitionNodeId());
        NodeHelper.buildForkNode().execute(execution.getExecution(nextProcess));
    }

    /**
     * join 节点执行
     */
    private void goJoin(FlowProcess nextProcess, Execution execution) {
        // 1. 查询正在执行的fork节点相关的任务节点个数
        List<FlowProcess> forkNodes = execution.getWorkFlowService().getFlowQueryService().getFlowProcessQuery()
                .forkNodeId(nextProcess.getForkNodeId())
                .list();
        // 如果大于1, 表示存在多个执行节点
        if(forkNodes.size() > 1) {
            execution.createNextFlowProcess(nextProcess, false);
        }
        // 否则汇聚merge
        else {
            execution.createNextFlowProcess(nextProcess, true);
            nextProcess.setForkNodeId(null);
            NodeHelper.buildDecisionNode().execute(execution.getExecution(nextProcess));
        }
    }

    /**
     * 子流程 end 节点执行
     */
    private void goSubEnd(FlowProcess nextProcess, Execution execution) {
        execution.createNextFlowProcess(nextProcess, true);
        NodeHelper.buildSubEndNode().execute(execution.getExecution(nextProcess));
    }
}
