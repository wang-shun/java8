package io.terminus.doctor.workflow.event;

import io.terminus.doctor.workflow.core.TimerExecution;

/**
 * Created by xiao on 16/8/11.
 */
public interface ITimer {
    /**
     *定时器事件执行
     * @param timerExecution
     */
    void Timer(TimerExecution timerExecution);
}
