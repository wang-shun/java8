package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.event.IHandler;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.model.FlowProcess;

import java.util.List;

/**
 * Desc: 流程节点执行器
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
     * 获取当前活动节点的下个执行节点
     * @return
     */
    public List<FlowProcess> getNextFlowProcesses();

    /**
     * 获取当前活动节点的处理事件
     * @return
     */
    public List<IHandler> getHandler();
}
