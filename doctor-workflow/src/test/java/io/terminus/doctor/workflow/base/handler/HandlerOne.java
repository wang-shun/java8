package io.terminus.doctor.workflow.base.handler;

import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.HandlerAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desc: 测试事件1
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/16
 */
@Component
@Slf4j
public class HandlerOne extends HandlerAware {

    @Override
    public void handle(Execution execution) {
        log.info("[handler one] -> 执行");
        log.info("全局业务数据为: " + execution.getBusinessData());
        log.info("流转数据为: " + execution.getFlowData());
        log.info("流转数据为被 [handler one] 变更为 {flowData:250}");
        execution.setFlowData("{flowData:250}");
        log.info("[handler one] -> 执行结束");
    }
}
