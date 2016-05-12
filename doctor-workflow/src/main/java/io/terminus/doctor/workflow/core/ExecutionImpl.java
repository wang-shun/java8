package io.terminus.doctor.workflow.core;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.doctor.workflow.event.IHandler;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.AssertHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Desc: 流程节点执行器
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/29
 */
@Slf4j
public class ExecutionImpl implements Execution {

    private WorkFlowEngine workFlowEngine;

    private FlowProcess flowProcess;

    public ExecutionImpl(WorkFlowEngine workFlowEngine, FlowProcess flowProcess) {
        if(flowProcess == null) {
            log.error("[Flow Execution] -> flowProcess is null error");
            AssertHelper.throwException("[Flow Execution] -> flowProcess can not be null");
        }
        this.workFlowEngine = workFlowEngine;
        this.flowProcess = flowProcess;
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
    public List<FlowProcess> getNextFlowProcesses() {
        return null;
    }

    @Override
    public List<IHandler> getHandler() {
        List<FlowDefinitionNodeEvent> events = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeEventQuery()
                .sourceNodeId(this.flowProcess.getId())
                .list();
        // TODO 如果存在decision
        List<IHandler> handlers = Lists.newArrayList();
        if(events != null && events.size() > 0) {
            events.forEach(event -> {
                // 如果配置了handler
                if(StringUtils.isNoneBlank(event.getHandler())) {
                    IHandler handler = workFlowEngine.buildContext().get(event.getHandler());
                    if(handler == null) {
                        try {
                            handler = (IHandler) Class.forName(event.getHandler()).newInstance();
                        } catch (Exception e) {
                            log.error("[Flow Execution] -> handler not found, handler is {}, cause by {}",
                                    event.getHandler(), Throwables.getStackTraceAsString(e));
                            AssertHelper.throwException("[Flow Execution] -> handler not found, handler is {}, cause by {}",
                                    event.getHandler(), Throwables.getStackTraceAsString(e));
                        }
                    }
                    handlers.add(handler);
                }
            });
        }
        return handlers;
    }

}
