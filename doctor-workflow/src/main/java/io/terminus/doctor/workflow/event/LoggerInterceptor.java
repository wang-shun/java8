package io.terminus.doctor.workflow.event;

import io.terminus.doctor.workflow.core.Execution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desc: 日志拦截器
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/27
 */
@Slf4j
@Component
public class LoggerInterceptor implements Interceptor {

    // TODO 日志

    @Override
    public void before(Execution execution) {
        log.info("开始开始开始......");
    }

    @Override
    public void after(Execution execution) {
        log.info("结束结束结束......");
    }
}
