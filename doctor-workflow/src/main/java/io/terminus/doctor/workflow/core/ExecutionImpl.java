package io.terminus.doctor.workflow.core;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.doctor.workflow.event.IHandler;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.AssertHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程节点执行容器
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/29
 */
@Slf4j
public class ExecutionImpl implements Execution {

    private WorkFlowEngine workFlowEngine;

    private FlowProcess flowProcess;
    private Map expression;
    private String flowData;
    private Long operatorId;
    private String operatorName;

    public ExecutionImpl(WorkFlowEngine workFlowEngine, FlowProcess flowProcess,
                         Map expression, String flowData, Long operatorId, String operatorName) {
        if (flowProcess == null) {
            log.error("[Flow Execution] -> flowProcess is null error");
            AssertHelper.throwException("[Flow Execution] -> flowProcess can not be null");
        }
        this.workFlowEngine = workFlowEngine;
        this.flowProcess = flowProcess;
        this.expression = expression;
        this.flowData = flowData;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
    }


    @Override
    public List<Interceptor> getInterceptors() {
        return workFlowEngine.buildInterceptors();
    }

    @Override
    public WorkFlowService getWorkFlowService() {
        return workFlowEngine.buildWorkFlowService();
    }

    @Override
    public FlowProcess getFlowProcess() {
        return flowProcess;
    }

    @Override
    public List<FlowDefinitionNodeEvent> getTransitions() {
        List<FlowDefinitionNodeEvent> transitions = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeEventQuery()
                .sourceNodeId(this.flowProcess.getFlowDefinitionNodeId())
                .list();
        return transitions == null ? Lists.newArrayList() : transitions;
    }

    @Override
    public List<FlowProcess> getNextFlowProcesses() {
        List<FlowDefinitionNodeEvent> events = getTransitions();
        // TODO 如果存在decision, decision根据判断表达式
        List<FlowProcess> nextProcesses = Lists.newArrayList();
        if (events != null && events.size() > 0) {
            events.forEach(event -> {
                FlowDefinitionNode nextNode = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                        .id(event.getTargetNodeId())
                        .single();
                FlowProcess nextProcess = FlowProcess.builder()
                        .flowDefinitionNodeId(nextNode.getId())
                        .flowInstanceId(this.flowProcess.getFlowInstanceId())
                        .flowData(this.flowData)
                        .assignee(nextNode.getAssignee())
                        .status(FlowProcess.Status.NORMAL.value())
                        .build();
                nextProcesses.add(nextProcess);
            });
        }
        return nextProcesses;
    }

    @Override
    public void createNextFlowProcess(FlowProcess flowProcess) {
        // 1. 删除当前已经完成的节点, TODO 记录历史和Track
        workFlowEngine.buildJdbcAccess().deleteFlowProcess(this.flowProcess.getId());
        // 2. 创建下一个节点
        workFlowEngine.buildJdbcAccess().createFlowProcess(flowProcess);
    }

    @Override
    public IHandler getHandler(String handlerName) {
        IHandler handler = null;
        // 如果配置了handler
        if (StringUtils.isNotBlank(handlerName)) {
            handler = workFlowEngine.buildContext().get(handlerName);
            if (handler == null) {
                try {
                    handler = (IHandler) Class.forName(handlerName).newInstance();
                } catch (Exception e) {
                    log.error("[Flow Execution] -> handler not found, handler is {}, cause by {}",
                            handlerName, Throwables.getStackTraceAsString(e));
                    AssertHelper.throwException("[Flow Execution] -> handler not found, handler is {}, cause by {}",
                            handlerName, Throwables.getStackTraceAsString(e));
                }
            }
        }
        return handler;
    }

    @Override
    public Map getExpression() {
        return this.expression;
    }

    @Override
    public String getFlowData() {
        return this.flowData;
    }

    @Override
    public void setFlowData(String flowData) {
        this.flowData = flowData;
    }

    @Override
    public String getBusinessData() {
        FlowInstance flowInstance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .id(this.flowProcess.getFlowInstanceId())
                .single();
        if (flowInstance != null) {
            return flowInstance.getBusinessData();
        }
        return null;
    }

    @Override
    public Long getOperatorId() {
        return this.operatorId;
    }

    @Override
    public String getOperatorName() {
        return this.operatorName;
    }

}
