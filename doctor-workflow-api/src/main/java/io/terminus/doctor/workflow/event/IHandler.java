package io.terminus.doctor.workflow.event;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: 流程运转事件处理接口, 如果没有前后拦截可继承 HandlerAware 类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/29
 */
public interface IHandler {

    /**
     * 事件执行前置拦截
     * @param execution 执行容器
     */
    public void preHandle(Execution execution);

    /**
     * 事件执行
     * @param execution 执行容器
     */
    public void handle(Execution execution);

    /**
     * 事件后置拦截
     * @param execution 执行容器
     */
    public void afterHandle(Execution execution);
}
