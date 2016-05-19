package io.terminus.doctor.event.handler;

import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.HandlerAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desc: 离场事件
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/19
 */
@Component
@Slf4j
public class LeaveHandler extends HandlerAware {

    @Override
    public void handle(Execution execution) {
        log.info("离场事件执行");
    }
}
