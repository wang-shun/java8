package io.terminus.doctor.workflow.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.event.ITimer;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.model.FlowTimer;
import io.terminus.doctor.workflow.utils.AssertHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Desc: 任务轮询
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/26
 */
@Component
@Slf4j
public class SchedulerImpl implements Scheduler {

    @Autowired
    private WorkFlowService workFlowService;

    @Autowired
    private WorkFlowEngine workFlowEngine;

    private static final String TIMER_PREFIX = "scheduler_timer";

    /**
     * 存放FlowDefinitionNode与Timer的对应关系
     * key : TIMER_PREFIX_{id}
     * value: Timer对象
     * id为FlowDefinitionNode的id
     */
    private Map<String, FlowTimer> timerMap;

    /**
     * 执行定时Task任务
     */
    @Transactional
    public void doSchedule() {
        // 1. 获取所有的node ids
        List<Long> nodeIds = getTimerNodeIds();

        // 2. 获取所有的process, 并执行
        List<FlowProcess> processes = workFlowService.getFlowQueryService().getFlowProcessQuery()
                .findFlowProcesses(ImmutableMap.of("flowDefinitionNodeIds", nodeIds));
        for (int i = 0; processes != null && i < processes.size(); i++) {
            FlowProcess flowProcess = processes.get(i);
            FlowInstance instance = workFlowService.getFlowQueryService().getFlowInstanceQuery()
                    .id(flowProcess.getFlowInstanceId())
                    .status(FlowInstance.Status.NORMAL.value())
                    .single();
            if (instance != null) {
                FlowTimer timer = timerMap.get(TIMER_PREFIX + flowProcess.getFlowDefinitionNodeId());
                TimerExecution timerExecution = new TimerExecutionImpl(workFlowEngine, flowProcess, flowProcess.getFlowData(), timer, instance.getBusinessId(), instance.getFlowDefinitionKey());
                ITimer iTimer = timerExecution.getITimer(workFlowService.getFlowQueryService().getFlowDefinitionNodeQuery().id(flowProcess.getFlowDefinitionNodeId()).single().getITimer());

                // 如果含有执行器
                if (iTimer != null) {
                    iTimer.timer(timerExecution);
                    // 如果到达执行时间, 则执行任务
                    if (isBefore(flowProcess)) {
                        workFlowService.getFlowProcessService()
                                .getExecutor(instance.getFlowDefinitionKey(), instance.getBusinessId(), flowProcess.getAssignee())
                                .execute(timerExecution.getExpression(), timerExecution.getFlowData());
                    }
                }
                // 否则
                else {
                    // 如果到达执行时间, 则执行任务
                    if (isBefore(flowProcess)) {
                        workFlowService.getFlowProcessService()
                                .getExecutor(instance.getFlowDefinitionKey(), instance.getBusinessId(), flowProcess.getAssignee())
                                .execute();
                    }
                }
            }
        }

    }

    /**
     * 判断是否到达执行点
     *
     * @param flowProcess 当前流程节点
     * @return
     */
    private boolean isBefore(FlowProcess flowProcess) {
        FlowTimer timer = timerMap.get(TIMER_PREFIX + flowProcess.getFlowDefinitionNodeId());
        if (timer != null) {
            return new DateTime(flowProcess.getCreatedAt())
                    .isBefore(DateTime
                            .now()
                            .minusYears(timer.getYear())
                            .minusMonths(timer.getMonth())
                            .minusDays(timer.getDay())
                            .minusHours(timer.getHour())
                            .minusMinutes(timer.getMinute())
                            .minusSeconds(timer.getSecond())
                    );
        }
        return false;
    }

    /**
     * 获取所有含有timer的FlowDefinitionNode ids
     */
    private List<Long> getTimerNodeIds() {
        timerMap = Maps.newHashMap();
        List<Long> nodeIds = Lists.newArrayList();
        List<FlowDefinitionNode> flowDefinitionNodes = workFlowService.getFlowQueryService().getFlowDefinitionNodeQuery()
                .type(FlowDefinitionNode.Type.TASK.value())
                .list();
        if (flowDefinitionNodes != null && flowDefinitionNodes.size() > 0) {
            flowDefinitionNodes.forEach(node -> {
                if (StringUtils.isNotBlank(node.getTimer())) {
                    nodeIds.add(node.getId());
                    timerMap.put(TIMER_PREFIX + node.getId(), new FlowTimer(node.getTimer()));
                }
            });
        }
        return nodeIds;
    }


}
