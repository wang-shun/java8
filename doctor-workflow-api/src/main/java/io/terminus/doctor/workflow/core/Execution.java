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
    public List<Interceptor> getInterceptors();

    /**
     * 获取流程公共服务类
     * @return
     */
    public WorkFlowService getWorkFlowService();

    /**
     * 获取当前活动的节点
     * @return
     */
    public FlowProcess getFlowProcess();

    /**
     * 设置当前活动的节点
     * @return
     */
    public FlowProcess setFlowProcess(FlowProcess flowProcess);

    /**
     * 获取当前活动节点的所有事件连线
     * @return
     */
    public List<FlowDefinitionNodeEvent> getTransitions();

    /**
     * 根据事件连线, 获取当前活动节点的下个执行节点
     * @return
     */
    public FlowProcess getNextFlowProcess(FlowDefinitionNodeEvent transition);

    /**
     * 创建下一个流程活动节点
     */
    public void createNextFlowProcess(FlowProcess flowProcess);

    /**
     * 获取当前活动节点的处理事件
     * @param handlerName   事件处理类名称
     * @return
     */
    public IHandler getHandler(String handlerName);

    /**
     * 获取执行表达式
     * @return
     */
    public Map getExpression();

    /**
     * 获取节点间流转数据
     * @return
     */
    public String getFlowData();

    /**
     * 设置节点间流转数据
     * @param flowData
     */
    public void setFlowData(String flowData);

    /**
     * 获取全局业务数据
     * @return
     */
    public String getBusinessData();

    /**
     * 设置全局业务数据
     * @return
     */
    public void setBusinessData(String businessData);

    /**
     * 获取操作者id
     * @return
     */
    public Long getOperatorId();

    /**
     * 获取操作者姓名
     * @return
     */
    public String getOperatorName();
}
