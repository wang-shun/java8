package io.terminus.doctor.workflow.base.handler;

import io.terminus.doctor.workflow.event.HandlerAware;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc: 测试事件2
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/16
 */
// @Component (也可以不添加, 但必须配置全类名)
@Slf4j
public class HandlerTwo extends HandlerAware {
//
//    @Override
//    public void handle(Execution execution) {
//        log.info("[handler two] -> 执行");
//        log.info("全局业务数据为: " + execution.getBusinessData());
//        log.info("流转数据为: " + execution.getFlowData());
//        log.info("[handler two] -> 修改了流转数据");
//        execution.setFlowData("{flowData:300}");
//        log.info("[handler two] -> 执行结束");
//    }
}
