package io.terminus.doctor.workflow.event;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: IHandler类的空白封装
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public abstract class HandlerAware implements IHandler {
    @Override
    public void preHandle(Execution execution) {

    }
    @Override
    public void AfterHandle(Execution execution) {

    }
}
