package io.terminus.doctor.workflow.event;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: 流程执行的前后拦截器接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/27
 */
public interface Interceptor {

    /**
     * 拦截前置方法
     * @param execution
     */
    void before(Execution execution);

    /**
     * 拦截后置方法
     * @param execution
     */
    void after(Execution execution);
}
