package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.IHandler;
import io.terminus.doctor.workflow.event.Interceptor;
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

    protected void forward(IHandler handler, Execution execution) {
        handler.preHandle(execution);
        handler.handle(execution);
        handler.afterHandle(execution);
    }
}
