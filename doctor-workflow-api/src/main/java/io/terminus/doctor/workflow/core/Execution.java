package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.event.IHandler;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowProcess;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程节点执行容器
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/29
 */
public interface Execution {

    /**
     * 获取执行前后的拦截器
     * @return
     */
    List<Interceptor> getInterceptors();

    /**
     * 获取流程公共服务类
     * @return
     */
    WorkFlowService getWorkFlowService();

    /**
     * 获取当前活动的节点
     * @return
     */
    FlowProcess getFlowProcess();

    /**
     * 设置当前活动的节点
     * @return
     */
    FlowProcess setFlowProcess(FlowProcess flowProcess);

    /**
     * 获取当前活动节点的所有事件连线
     * @return
     */
    List<FlowDefinitionNodeEvent> getTransitions();

    /**
     * 根据事件连线, 获取当前活动节点的下个执行节点
     * @return
     */
    FlowProcess getNextFlowProcess(FlowDefinitionNodeEvent transition);

    /**
     * 创建下一个流程活动节点
     */
    void createNextFlowProcess(FlowProcess flowProcess, boolean ifCreate);

    /**
     * 获取当前活动节点的处理事件
     * @param handlerName   事件处理类名称
     * @return
     */
    IHandler getHandler(String handlerName);

    /**
     * 获取执行表达式
     * @return
     */
    Map getExpression();

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
     * 获取全局业务数据
     * @return
     */
    String getBusinessData();

    /**
     * 获取操作者id
     * @return
     */
    Long getOperatorId();

    /**
     * 获取操作者姓名
     * @return
     */
    String getOperatorName();
}
