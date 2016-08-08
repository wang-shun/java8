package io.terminus.doctor.workflow.core;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.model.*;
import io.terminus.doctor.workflow.service.FlowDefinitionService;
import io.terminus.doctor.workflow.service.FlowProcessService;
import io.terminus.doctor.workflow.service.FlowQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc: 工作流公共服务类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/12
 */
@Slf4j
@Service
@RpcProvider
public class WorkFlowServiceImpl implements WorkFlowService {

    @Autowired
    private WorkFlowEngine workFlowEngine;

    @Override
    public FlowDefinitionService getFlowDefinitionService() {
        return workFlowEngine.buildFlowDefinitionService();
    }

    @Override
    public FlowProcessService getFlowProcessService() {
        return workFlowEngine.buildFlowProcessService();
    }

    @Override
    public FlowQueryService getFlowQueryService() {
        return workFlowEngine.buildFlowQueryService();
    }

    @Override
    public void doTimerSchedule() {
        workFlowEngine.buildScheduler().doSchedule();
    }

    @Override
    public Response<Boolean> updateData(String flowDefinitionKey, Long businessId) {
        try {
            FlowQueryService flowQueryService = getFlowQueryService();
            Map<Long, Long> map = Maps.newHashMap();
            FlowDefinition flowDefinition = flowQueryService.getFlowDefinitionQuery().getLatestDefinitionByKey(flowDefinitionKey);
            List<FlowInstance> flowInstanceList = flowQueryService.getFlowInstanceQuery().flowDefinitionKey(flowDefinitionKey).list();
            for (FlowInstance flowInstance : flowInstanceList
                    ) {
                if (businessId == null?flowInstance.getFlowDefinitionId() != flowDefinition.getId():flowInstance.getFlowDefinitionId() != flowDefinition.getId() && flowInstance.getBusinessId() == businessId) {
                    Long oldFlowDefinitionNodeId = flowQueryService.getFlowProcessQuery().flowInstanceId(flowInstance.getId()).single().getFlowDefinitionNodeId();
                    Long oldPreFlowDefinitionNodeId = Long.parseLong(flowQueryService.getFlowProcessQuery().flowInstanceId(flowInstance.getId()).single().getPreFlowDefinitionNodeId());
                    Long lastFlowDefinitionNodeId;
                    Long lastPreFlowDefinitionNodeId;
                    lastFlowDefinitionNodeId = getLastNodeId(map, oldFlowDefinitionNodeId, flowQueryService, flowDefinition);
                    lastPreFlowDefinitionNodeId = getLastNodeId(map, oldPreFlowDefinitionNodeId, flowQueryService, flowDefinition);
                    FlowProcess flowProcess = flowQueryService.getFlowProcessQuery().flowInstanceId(flowInstance.getId()).single();
                    if (lastFlowDefinitionNodeId !=null)
                    flowProcess.setFlowDefinitionNodeId(lastFlowDefinitionNodeId);
                    if (lastPreFlowDefinitionNodeId != null )
                    flowProcess.setPreFlowDefinitionNodeId(String.valueOf(lastPreFlowDefinitionNodeId));
                    access().updateFlowProcess(flowProcess);
                    List<FlowProcessTrack> flowProcessTracks = flowQueryService.getFlowProcessTrackQuery().flowInstanceId(flowInstance.getId()).list();
                    for (FlowProcessTrack flowProcessTrack: flowProcessTracks
                         ) {
                        lastFlowDefinitionNodeId = getLastNodeId(map, flowProcessTrack.getFlowDefinitionNodeId(), flowQueryService, flowDefinition);
                        lastPreFlowDefinitionNodeId = getLastNodeId(map, Long.parseLong(flowProcessTrack.getPreFlowDefinitionNodeId()), flowQueryService, flowDefinition);
                        if (lastFlowDefinitionNodeId !=null)
                        flowProcessTrack.setFlowDefinitionNodeId(lastFlowDefinitionNodeId);
                        if (lastPreFlowDefinitionNodeId != null )
                        flowProcessTrack.setPreFlowDefinitionNodeId(String.valueOf(lastPreFlowDefinitionNodeId));
                        access().updateFlowProcessTrack(flowProcessTrack);
                    }

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

    private Long getLastNodeId(Map<Long, Long > map, Long oldFlowDefinitionNodeId, FlowQueryService flowQueryService, FlowDefinition flowDefinition){
        Long lastFlowDefinitionNodeId = null;
        FlowDefinitionNode flowDefinitionNode;
        if (map.containsKey(oldFlowDefinitionNodeId)) {
            lastFlowDefinitionNodeId = map.get(oldFlowDefinitionNodeId);
        } else {
            if (oldFlowDefinitionNodeId != -1) {
                flowDefinitionNode = flowQueryService.getFlowDefinitionNodeQuery().getDefinitionNodeByName(flowDefinition.getId(), flowQueryService.getFlowDefinitionNodeQuery().id(oldFlowDefinitionNodeId).single().getName());
                if (flowDefinitionNode != null)
                    lastFlowDefinitionNodeId = flowDefinitionNode.getId();
            }
            map.put(oldFlowDefinitionNodeId, lastFlowDefinitionNodeId);
        }
        return lastFlowDefinitionNodeId;
    }
}
