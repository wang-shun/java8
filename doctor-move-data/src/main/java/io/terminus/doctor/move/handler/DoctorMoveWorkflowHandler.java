package io.terminus.doctor.move.handler;

import com.google.common.collect.ImmutableList;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Desc: 工作流数据迁移
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/27
 */
//@Component
public class DoctorMoveWorkflowHandler {

    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigSnapshotDao doctorPigSnapshotDao;

    @Autowired
    private JdbcAccess jdbcAccess;
    @Autowired
    private WorkFlowService workFlowService;

    @Value("${flow.definition.key.sow:sow}")
    private String sowFlowKey;

    // 流程定义
    private FlowDefinition flowDefinition;

    // 流程节点Map对象, key为节点的id, value为节点对象
    private Map<Long, FlowDefinitionNode> nodesMapById = new HashMap<>();
    // 流程节点Map对象, key为节点的name, value为节点对象
    private Map<String, FlowDefinitionNode> nodesMapByName = new HashMap<>();

    // 流程事件连线Map对象, key为连线的value值(猪eventType), value为连线对象
    private Map<Integer, FlowDefinitionNodeEvent> eventsMapByValue = new HashMap<>();
    // 流程事件连线Map对象, key为连线的sourceId, value为连线对象集合
    private Map<Long, List<FlowDefinitionNodeEvent>> eventsMapBySourceId = new HashMap<>();

    @PostConstruct
    public void initBasicData() {
        // 1. 流程定义相关的初始化
        flowDefinition = workFlowService.getFlowQueryService().getFlowDefinitionQuery().getLatestDefinitionByKey(sowFlowKey);
        if (flowDefinition == null) {
            return;
        }
        // 流程节点缓存
        nodesMapByName = workFlowService.getFlowQueryService().getFlowDefinitionNodeQuery()
                .getDefinitionNodes(flowDefinition.getId())
                .stream()
                .collect(Collectors.toMap(FlowDefinitionNode::getName, v -> v));
        nodesMapById = workFlowService.getFlowQueryService().getFlowDefinitionNodeQuery()
                .getDefinitionNodes(flowDefinition.getId())
                .stream()
                .collect(Collectors.toMap(FlowDefinitionNode::getId, v -> v));
        // 流程事件连线缓存
        eventsMapByValue = workFlowService.getFlowQueryService().getFlowDefinitionNodeEventQuery()
                .getNodeEvents(flowDefinition.getId())
                .stream()
                .filter(event -> StringUtils.isNoneBlank(event.getValue()))
                .collect(Collectors.toMap(k -> Integer.parseInt(k.getValue()), v -> v));
        eventsMapBySourceId = workFlowService.getFlowQueryService().getFlowDefinitionNodeQuery()
                .getDefinitionNodes(flowDefinition.getId())
                .stream()
                .collect(Collectors.toMap(
                        FlowDefinitionNode::getId,
                        v -> workFlowService.getFlowQueryService().getFlowDefinitionNodeEventQuery()
                                .getNodeEventsBySourceId(flowDefinition.getId(), v.getId())
                ));
    }


    /**
     * 对猪只进行导入工作流处理
     * @param pigInfoDtos
     */
    @Transactional
    public void handle(List<DoctorPigInfoDto> pigInfoDtos) {
        pigInfoDtos.forEach(pig -> {
            // 1. 生成一个流程实例
            FlowInstance flowInstance = FlowInstance.builder()
                    .name(flowDefinition.getName() + System.currentTimeMillis())
                    .flowDefinitionId(flowDefinition.getId())
                    .flowDefinitionKey(flowDefinition.getKey())
                    .businessId(pig.getId())
                    .status(FlowInstance.Status.NORMAL.value())
                    .type(FlowInstance.Type.PARENT.value())
                    .parentInstanceId(-1L)
                    .build();
            jdbcAccess.createFlowInstance(flowInstance);

            // 2. 获取猪最新的一次事件类型, 获取下一个节点对象
            // DoctorPigEvent pigEvent = doctorPigEventDao.queryLastPigEventById(pig.getPigId());
            DoctorPigEvent pigEvent = doctorPigEventDao.queryLastPigEventInWorkflow(pig.getPigId(),
                    ImmutableList.of(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19));
            Integer type = pigEvent.getType();
            FlowDefinitionNode sourceNode = nodesMapById.get(eventsMapByValue.get(type).getSourceNodeId());
            FlowDefinitionNode targetNode = nodesMapById.get(eventsMapByValue.get(type).getTargetNodeId());

            // 2.1 如果 Task 节点
            if (targetNode.getType() == FlowDefinitionNode.Type.TASK.value()) {
                // 生成 Flow Process 对象
                FlowProcess flowProcess = FlowProcess.builder()
                        .flowDefinitionNodeId(targetNode.getId())
                        .preFlowDefinitionNodeId(sourceNode.getId() + "")
                        .flowInstanceId(flowInstance.getId())
                        .flowData(getFlowData(pig, pigEvent))
                        .status(FlowProcess.Status.NORMAL.value())
                        .assignee(targetNode.getAssignee())
                        .forkNodeId(null)
                        .build();

                jdbcAccess.createFlowProcess(flowProcess);
            }
        });
    }

    /**
     * 获取flowData的数据信息
     * @param pig
     * @return
     */
    private String getFlowData(DoctorPigInfoDto pig, DoctorPigEvent pigEvent) {

        Map<String, String> flowData = new HashMap<>();

        // 1. basic 信息
        DoctorBasicInputInfoDto basic = DoctorBasicInputInfoDto.builder()
                .pigId(pig.getPigId())
                .pigCode(pig.getPigCode())
                .pigType(pig.getPigType())
                .orgId(pig.getOrgId())
                .orgName(pig.getOrgName())
                .farmId(pig.getFarmId())
                .farmName(pig.getFarmName())
                .barnId(pig.getBarnId())
                .barnName(pig.getBarnName())
                .staffId(pig.getCreatorId())
                .staffName(pig.getCreatorName())
                .eventType(pigEvent.getType())
                .eventName(pigEvent.getName())
                .eventDesc(pigEvent.getDesc())
                .relEventId(pigEvent.getRelEventId())
                .build();
        flowData.put("basic", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(basic));

        // 2. extra
        flowData.put("extra", pig.getExtraTrack());

        // 3. createEventResult
        Map<String, Object> eventResult = new HashMap<>();
        eventResult.put("doctorPigId", pig.getPigId());
        eventResult.put("doctorEventId", pigEvent.getId());
        DoctorPigSnapshot snapshot = doctorPigSnapshotDao.queryByEventId(pigEvent.getId()); // 快照
        if (snapshot != null) {
            eventResult.put("doctorSnapshotId", snapshot.getId());
        }
        flowData.put("createEventResult", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(eventResult));

        return JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(flowData);
    }
}
