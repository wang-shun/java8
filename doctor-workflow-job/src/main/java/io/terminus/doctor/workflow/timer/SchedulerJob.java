package io.terminus.doctor.workflow.timer;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.core.Scheduler;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Desc: 任务轮询
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/26
 */
@Component
@EnableScheduling
@Configuration
@Slf4j
public class SchedulerJob {

    @Autowired
    private WorkFlowService workFlowService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void doSchedule() {
        try{
            log.info("[work flow job start]");
            workFlowService.doTimerSchedule();
            log.info("[work flow job end]");
        } catch (Exception e) {
            log.error("work flow job failed", Throwables.getStackTraceAsString(e));
        }
    }
}
