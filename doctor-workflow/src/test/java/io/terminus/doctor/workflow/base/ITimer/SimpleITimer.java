package io.terminus.doctor.workflow.base.ITimer;

import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.core.TimerExecution;
import io.terminus.doctor.workflow.event.ITimer;
import io.terminus.doctor.workflow.model.FlowTimer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by xiao on 16/8/11.
 */
@Component
public class SimpleITimer implements ITimer {
    @Override
    public void Timer(TimerExecution timerExecution) {
        Map expression = Maps.newHashMap();
        expression.put("money", 100);
        timerExecution.setExpression(expression);
    }
}
