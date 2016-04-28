package io.terminus.doctor.workflow.event;

/**
 * Desc: 流程执行的前后拦截器接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/27
 */
public interface Interceptor {

    public void before();

    public void after();
}
