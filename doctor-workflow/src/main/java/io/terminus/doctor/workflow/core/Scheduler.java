package io.terminus.doctor.workflow.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.StringHelper;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
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
public class Scheduler {

    @Autowired
    private WorkFlowEngine workFlowEngine;

    private static final String TIMER_PREFIX = "scheduler_timer";

    /**
     * 存放FlowDefinitionNode与Timer的对应关系
     * key : TIMER_PREFIX_{id}
     * value: Timer对象
     * id为FlowDefinitionNode的id
     */
    private Map<String, Timer> timerMap;

    /**
     * 每 10s 轮询一次任务节点
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void taskInvocation() {

        // 1. 获取所有的node ids
        List<Long> nodeIds = getTimerNodeIds();

        // 2. 获取所有的process, 并执行
        List<FlowProcess> processes = workFlowEngine.buildFlowQueryService().getFlowProcessQuery()
                .findFlowProcesses(ImmutableMap.of("flowDefinitionNodeIds", nodeIds));
        for (int i = 0; processes != null && i < processes.size(); i++) {
            FlowProcess flowProcess = processes.get(i);
            // 如果到达执行时间, 则执行任务
            if (isBefore(flowProcess)) {
                FlowInstance instance = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                        .id(flowProcess.getFlowInstanceId())
                        .status(FlowInstance.Status.NORMAL.value())
                        .single();
                if(instance != null) {
                    workFlowEngine.buildFlowProcessService()
                            .getExecutor(instance.getFlowDefinitionKey(), instance.getBusinessId(), flowProcess.getAssignee())
                            .execute();
                }
            }
        }
    }

    /**
     * 判断是否到达执行点
     * @param flowProcess   当前流程节点
     * @return
     */
    private boolean isBefore(FlowProcess flowProcess) {
        Timer timer = timerMap.get(TIMER_PREFIX + flowProcess.getFlowDefinitionNodeId());
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
        List<FlowDefinitionNode> flowDefinitionNodes = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                .type(FlowDefinitionNode.Type.TASK.value())
                .list();
        if (flowDefinitionNodes != null && flowDefinitionNodes.size() > 0) {
            flowDefinitionNodes.forEach(node -> {
                if (StringHelper.isNotBlank(node.getTimer())) {
                    nodeIds.add(node.getId());
                    timerMap.put(TIMER_PREFIX + node.getId(), new Timer(node.getTimer()));
                }
            });
        }
        return nodeIds;
    }

    /**
     * 定时对象
     */
    @Data
    class Timer implements Serializable {

        private static final long serialVersionUID = -1962422458604599286L;

        private int year;
        private int month;
        private int day;
        private int hour;
        private int minute;
        private int second;

        public Timer(String timer) {
            String[] strings = timer.trim().split("\\s+");
            for (int i = 0; strings != null && i < strings.length; i++) {
                int value = Integer.parseInt(strings[i]);
                switch (i) {
                    case 0:
                        this.second = value;
                        break;
                    case 1:
                        this.minute = value;
                        break;
                    case 2:
                        this.hour = value;
                        break;
                    case 3:
                        this.day = value;
                        break;
                    case 4:
                        this.month = value;
                        break;
                    case 5:
                        this.year = value;
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
