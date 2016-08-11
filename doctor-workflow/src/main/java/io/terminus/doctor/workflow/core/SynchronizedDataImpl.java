package io.terminus.doctor.workflow.core;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.model.Response;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.model.*;
import io.terminus.doctor.workflow.service.FlowQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by xiao on 16/8/11.
 */
@Component
@Slf4j
public class SynchronizedDataImpl implements SynchronizedData{

    @Autowired
    private WorkFlowService workFlowService;

    @Autowired
    private WorkFlowEngine workFlowEngine;

    @Override
    public Response<Boolean> updateData(String flowDefinitionKey, Long businessId) {
        try {
            FlowQueryService flowQueryService = workFlowService.getFlowQueryService();
            Map<Long, Long> map = Maps.newHashMap();
            Map<Long, String> flowDefinitionNodeCacheMap = Maps.newHashMap();
            List<FlowDefinitionNode> flowDefinitionNodes = flowQueryService.getFlowDefinitionNodeQuery().list();
            flowDefinitionNodes.forEach(flowDefinitionNode -> {
                flowDefinitionNodeCacheMap.put(flowDefinitionNode.getId(),flowDefinitionNode.getName());
            });

            FlowDefinition flowDefinition = flowQueryService.getFlowDefinitionQuery().getLatestDefinitionByKey(flowDefinitionKey);
            List<FlowInstance> flowInstanceList = flowQueryService.getFlowInstanceQuery().flowDefinitionKey(flowDefinitionKey).list();
            for (FlowInstance flowInstance : flowInstanceList
                    ) {
                if (businessId == null?flowInstance.getFlowDefinitionId() != flowDefinition.getId():flowInstance.getFlowDefinitionId() != flowDefinition.getId() && flowInstance.getBusinessId() == businessId) {
                    Long lastFlowDefinitionNodeId;
                    Long lastPreFlowDefinitionNodeId;
                    //更新workflow_processes 数据
                    List<FlowProcess> flowProcesses = flowQueryService.getFlowProcessQuery().flowInstanceId(flowInstance.getId()).list();
                    for(FlowProcess flowProcess : flowProcesses ){
                        lastFlowDefinitionNodeId = getLastNodeId(map, flowProcess.getFlowDefinitionNodeId(), flowQueryService, flowDefinition, flowDefinitionNodeCacheMap);
                        lastPreFlowDefinitionNodeId = getLastNodeId(map, Long.parseLong(flowProcess.getPreFlowDefinitionNodeId()), flowQueryService, flowDefinition, flowDefinitionNodeCacheMap);
                        if (lastFlowDefinitionNodeId != null)
                            flowProcess.setFlowDefinitionNodeId(lastFlowDefinitionNodeId);
                        if (lastPreFlowDefinitionNodeId != null)
                            flowProcess.setPreFlowDefinitionNodeId(String.valueOf(lastPreFlowDefinitionNodeId));
                        access().updateFlowProcess(flowProcess);
                    }
                    //更新 workflow_process_tracks数据
                    List<FlowProcessTrack> flowProcessTracks = flowQueryService.getFlowProcessTrackQuery().flowInstanceId(flowInstance.getId()).list();
                    for (FlowProcessTrack flowProcessTrack: flowProcessTracks) {
                        lastFlowDefinitionNodeId = getLastNodeId(map, flowProcessTrack.getFlowDefinitionNodeId(), flowQueryService, flowDefinition, flowDefinitionNodeCacheMap);
                        lastPreFlowDefinitionNodeId = getLastNodeId(map, Long.parseLong(flowProcessTrack.getPreFlowDefinitionNodeId()), flowQueryService, flowDefinition, flowDefinitionNodeCacheMap);
                        if (lastFlowDefinitionNodeId != null)
                            flowProcessTrack.setFlowDefinitionNodeId(lastFlowDefinitionNodeId);
                        if (lastPreFlowDefinitionNodeId != null)
                            flowProcessTrack.setPreFlowDefinitionNodeId(String.valueOf(lastPreFlowDefinitionNodeId));
                        access().updateFlowProcessTrack(flowProcessTrack);
                    }
                    //更新workflow_process_instances
                    flowInstance.setFlowDefinitionId(flowDefinition.getId());
                    access().updateFlowInstance(flowInstance);
                }
            }
            return Response.ok(Boolean.TRUE);

        } catch (Exception e) {
            log.error("update data fail, flowDefinitionKey:{} cause:{}", flowDefinitionKey, Throwables.getStackTraceAsString(e));
            return Response.fail("update.data.fail");
        }
    }

    private JdbcAccess access() {
        return workFlowEngine.buildJdbcAccess();
    }

    private Long getLastNodeId(Map<Long, Long > map, Long oldFlowDefinitionNodeId, FlowQueryService flowQueryService, FlowDefinition flowDefinition, Map<Long, String> flowDefinitionNodeCacheMap){
        Long lastFlowDefinitionNodeId = null;
        FlowDefinitionNode flowDefinitionNode;
        if (map.containsKey(oldFlowDefinitionNodeId)) {
            lastFlowDefinitionNodeId = map.get(oldFlowDefinitionNodeId);
        } else {
            if (oldFlowDefinitionNodeId != -1) {
                flowDefinitionNode = flowQueryService.getFlowDefinitionNodeQuery().getDefinitionNodeByName(flowDefinition.getId(), flowDefinitionNodeCacheMap.get(oldFlowDefinitionNodeId));
                if (flowDefinitionNode != null)
                    lastFlowDefinitionNodeId = flowDefinitionNode.getId();
            }
            map.put(oldFlowDefinitionNodeId, lastFlowDefinitionNodeId);
        }
        return lastFlowDefinitionNodeId;
    }
}
