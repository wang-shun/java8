package io.terminus.doctor.event.handler;

import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.HandlerAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desc: 断奶事件
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/19
 */
@Component
@Slf4j
public class WeaningHandler extends HandlerAware {

    @Override
    public void handle(Execution execution) {
        log.info("断奶事件执行");
    }
}
