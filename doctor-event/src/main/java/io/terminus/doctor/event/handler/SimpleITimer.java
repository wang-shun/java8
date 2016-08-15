package io.terminus.doctor.event.handler;

import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.core.TimerExecution;
import io.terminus.doctor.workflow.event.ITimer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by xiao on 16/8/11.
 */
@Slf4j
@Component
public class SimpleITimer implements ITimer {
    @Override
    public void Timer(TimerExecution timerExecution) {
        log.info("[simple itimer] -> 开始执行");
        Map expression = Maps.newHashMap();
        expression.put("money", 1000);
        timerExecution.setExpression(expression);
        log.info("[simple itimer] -> 执行结束");

    }
}
