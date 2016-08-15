package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.event.ITimer;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.model.FlowTimer;

import java.util.Map;

/**
 * Created by xiao on 16/8/11.
 */
public interface TimerExecution {
    /**
     * 获取FlowTimer
     * @return
     */
    FlowTimer getFlowTimer();

    /**
     * 设置FlowTimer
     */
    void setFlowTimer(FlowTimer flowTimer);
    /**
     * 获取表达式
     * @return
     */

    Map getExpression();

    /**
     * 设置表达式
     * @param expression
     */
    void setExpression(Map expression);

    /**
     * 获取节点间流转数据
     * @return
     */

    String getFlowData();

    /**
     * 设置节点间流转数据
     * @param flowData
     */
    void setFlowData(String flowData);

    /**
     * 获取timer处理器
     * @param iTimerName
     * @return
     */
    ITimer getITimer(String iTimerName);

    /**
     * 获取流程
     * @return
     */
    FlowProcess getFlowProcess();

    /**
     * 获取businessId
     * @return
     */
    Long getBusinessId();

    /**
     * 获取flowDefinitionKey
     * @return
     */
    String getFlowDefinitionKey();
}
