package io.terminus.doctor.workflow.base.handler;

import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.HandlerAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desc: 强制结束流程的handler
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
@Slf4j
@Component
public class HandlerForceEnd extends HandlerAware {

    @Override
    public void handle(Execution execution) {
        log.info("[HandlerForceEnd] -> 执行");
        log.info("[FlowDefinition Key] -> {}", execution.getFlowDefinitionKey());
        log.info("[Business Id] -> {}", execution.getBusinessId());
        log.info("全局业务数据为: " + execution.getBusinessData());
        log.info("[HandlerForceEnd] -> 执行结束");
    }
}
