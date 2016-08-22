package io.terminus.doctor.workflow.base.ITimer;

import io.terminus.doctor.workflow.core.TimerExecution;
import io.terminus.doctor.workflow.event.ITimer;
import org.springframework.stereotype.Component;

/**
 * Created by xiao on 16/8/11.
 */
@Component
public class SimpleITimer implements ITimer {
    @Override
    public void timer(TimerExecution timerExecution) {
        timerExecution.getExpression().put("money", 100);
    }
}
