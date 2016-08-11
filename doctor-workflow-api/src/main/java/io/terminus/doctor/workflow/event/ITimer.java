package io.terminus.doctor.workflow.event;

import io.terminus.doctor.workflow.core.TimerExecution;

/**
 * Created by xiao on 16/8/11.
 */
public interface ITimer {
    void Timer(TimerExecution timerExecution);
}
