package io.terminus.doctor.workflow.core;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.event.ITimer;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.model.FlowTimer;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.StringHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Created by xiao on 16/8/11.
 */
@Slf4j
public class TimerExecutionImpl implements TimerExecution {
    private WorkFlowEngine workFlowEngine;
    private FlowProcess flowProcess;
    private Map expression;
    private String flowData;
    private FlowTimer flowTimer;
    private Long businessId;
    private String flowDefinitionKey;

    public TimerExecutionImpl(WorkFlowEngine workFlowEngine, FlowProcess flowProcess, String flowData, FlowTimer flowTimer, Long businessId, String flowDefinitionKey) {
        this.workFlowEngine = workFlowEngine;
        this.flowProcess = flowProcess;
        this.flowData = flowData;
        this.flowTimer = flowTimer;
        this.businessId = businessId;
        this.flowDefinitionKey = flowDefinitionKey;
    }

    @Override
    public Map getExpression() {
        if (expression == null) {
            expression = Maps.newHashMap();
        }
        return expression;
    }

    @Override
    public String getFlowData() {
        return flowData;
    }

    @Override
    public void setFlowData(String flowData) {
        this.flowData = flowData;
    }

    @Override
    public ITimer getITimer(String iTimerName) {
        ITimer iTimer = null;
        if (StringHelper.isNotBlank(iTimerName)) {
            iTimer = workFlowEngine.buildContext().get(iTimerName);
            if (iTimer == null) {
                // 获取类的简单名称, 从上下文中获取
                iTimer = workFlowEngine.buildContext().get(
                        StringHelper.uncapitalize(iTimerName.substring(iTimerName.lastIndexOf(".") + 1)));
                if (iTimer == null) {
                    try {
                        // 实例化, 并存到上下文
                        iTimer = (ITimer) Class.forName(iTimerName).newInstance();
                        workFlowEngine.buildContext().put(
                                StringHelper.uncapitalize(iTimerName.substring(iTimerName.lastIndexOf(".") + 1)), iTimer);
                    } catch (Exception e) {
                        log.error("[Flow ITimerExecution] -> iTimer not found, iTimerName is {}, cause by {}",
                                iTimerName, Throwables.getStackTraceAsString(e));
                        AssertHelper.throwException("[Flow ITimerExecution] -> iTimer not found, iTimerName is {}, cause by {}",
                                iTimerName, Throwables.getStackTraceAsString(e));
                    }
                }
            }
        }
        return iTimer;
    }


    @Override
    public FlowTimer getFlowTimer() {
        if (flowTimer == null) {
            flowTimer = FlowTimer.builder().build();
        }
        return flowTimer;
    }

    @Override
    public FlowProcess getFlowProcess() {
        return flowProcess;
    }

    @Override
    public Long getBusinessId() {
        return businessId;
    }

    @Override
    public String getFlowDefinitionKey() {
        return flowDefinitionKey;
    }
}
