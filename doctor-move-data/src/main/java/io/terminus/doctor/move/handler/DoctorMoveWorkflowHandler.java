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
import org.springframework.stereotype.Component;
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
@Component
@SuppressWarnings("all")
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
        workFlowService.getFlowQueryService().getFlowDefinitionNodeEventQuery()
                .getNodeEvents(flowDefinition.getId())
                .stream()
                .filter(event -> StringUtils.isNoneBlank(event.getValue()))
                .forEach(event -> eventsMapByValue.put(Integer.parseInt(event.getValue()), event));
        /*eventsMapBySourceId = workFlowService.getFlowQueryService().getFlowDefinitionNodeQuery()
                .getDefinitionNodes(flowDefinition.getId())
                .stream()
                .collect(Collectors.toMap(
                        FlowDefinitionNode::getId,
                        v -> workFlowService.getFlowQueryService().getFlowDefinitionNodeEventQuery()
                                .getNodeEventsBySourceId(flowDefinition.getId(), v.getId())
                ));*/
    }


    /**
     * 对猪只进行导入工作流处理
     *
     * @param pigInfoDtos
     */
    @Transactional
    public void handle(List<DoctorPigInfoDto> pigInfoDtos) {
        initBasicData();

        // 处理数据
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
                    ImmutableList.of(9, 10, 11, 12, 13, 14, 15, 16, 17, 18)); //,19));

            if (pigEvent != null) {
                Integer type = pigEvent.getType();
                FlowDefinitionNode sourceNode = nodesMapById.get(eventsMapByValue.get(type).getSourceNodeId()); // 可能存在多个, 随机一个(无所谓)
                FlowDefinitionNode targetNode = nodesMapById.get(eventsMapByValue.get(type).getTargetNodeId());

                // 2.1 如果 Task 节点
                // if (targetNode.getType() == FlowDefinitionNode.Type.TASK.value()) {
                    // 1.  如果类型是转入分娩舍 > 10
                    if (type == 10) {
                        // 如果是阳性
                        if (pig.getStatus() == 4) {
                            createFlowProcess(nodesMapByName.get("妊娠检查阳性").getId(), nodesMapByName.get("妊娠检查A结果").getId(), pigEvent, flowInstance, pig);
                        }
                        // 如果是待分娩
                        if (pig.getStatus() == 7) {
                            createFlowProcess(nodesMapByName.get("待分娩").getId(), nodesMapByName.get("妊娠检查A结果").getId(), pigEvent, flowInstance, pig);
                        }
                        // 断奶
                        else if (pig.getStatus() == 9) {
                            createFlowProcess(nodesMapByName.get("断奶").getId(), nodesMapByName.get("妊娠检查A结果").getId(), pigEvent, flowInstance, pig);
                        } else {
                            createFlowProcess(targetNode.getId(), sourceNode.getId(), pigEvent, flowInstance, pig);
                        }
                    }
                    // 1.1 如果是转配种舍 > 12
                    else if (type == 12) {
                        // 如果是阳性
                        if (pig.getStatus() == 4) {
                            createFlowProcess(nodesMapByName.get("妊娠检查阳性").getId(), nodesMapByName.get("妊娠检查A结果").getId(), pigEvent, flowInstance, pig);
                        }
                        // 断奶
                        else if (pig.getStatus() == 9) {
                            createFlowProcess(nodesMapByName.get("断奶").getId(), nodesMapByName.get("妊娠检查A结果").getId(), pigEvent, flowInstance, pig);
                        }
                        else {
                            createFlowProcess(targetNode.getId(), sourceNode.getId(), pigEvent, flowInstance, pig);
                        }
                    }
                    // 2.  如果是妊娠检查  > 11
                    else if (type == 11) {
                        // 如果是阳性
                        if (pig.getStatus() == 4) {
                            createFlowProcess(nodesMapByName.get("妊娠检查阳性").getId(), nodesMapByName.get("妊娠检查A结果").getId(), pigEvent, flowInstance, pig);
                        }
                        // 断奶
                        else if (pig.getStatus() == 9) {
                            createFlowProcess(nodesMapByName.get("断奶").getId(), nodesMapByName.get("妊娠检查A结果").getId(), pigEvent, flowInstance, pig);
                        }
                        // 否则空怀
                        else {
                            createFlowProcess(nodesMapByName.get("空怀").getId(), nodesMapByName.get("妊娠检查A结果").getId(), pigEvent, flowInstance, pig);
                        }
                    }
                    // 3. 如果是断奶事件判断 > 16  或者 仔猪变动 > 18
                    else if (type == 16 || type == 17 || type == 18) {
                        // 哺乳
                        if (pig.getStatus() == 8) {
                            createFlowProcess(nodesMapByName.get("哺乳").getId(), nodesMapByName.get("待分娩").getId(), pigEvent, flowInstance, pig);
                        }
                        // 否则断奶
                        else {
                            createFlowProcess(nodesMapByName.get("断奶").getId(), nodesMapByName.get("断奶事件判断").getId(), pigEvent, flowInstance, pig);
                        }
                    } else {
                        createFlowProcess(targetNode.getId(), sourceNode.getId(), pigEvent, flowInstance, pig);
                    }

                }
            // }
            // 否则处于待配种状态
            // else {
            // createFlowProcess(nodesMapByName.get("待配种").getId(), nodesMapByName.get("开始节点信息").getId(), pigEvent, flowInstance, pig);
            // }
        });
    }

    /**
     * 获取flowData的数据信息
     *
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

    // 创建flowprocess
    private void createFlowProcess(Long definitionId, Long preNodeId, DoctorPigEvent pigEvent, FlowInstance instance, DoctorPigInfoDto pig) {
        if (pigEvent != null) {
            FlowProcess flowProcess = FlowProcess.builder()
                    .flowDefinitionNodeId(definitionId)
                    .preFlowDefinitionNodeId(preNodeId + "")
                    .flowInstanceId(instance.getId())
                    .flowData(getFlowData(pig, pigEvent))
                    .status(FlowProcess.Status.NORMAL.value())
                    .forkNodeId(null)
                    .build();

            jdbcAccess.createFlowProcess(flowProcess);
            flowProcess.setCreatedAt(pigEvent.getEventAt());
            jdbcAccess.updateFlowProcess(flowProcess);
        } else {
            FlowProcess flowProcess = FlowProcess.builder()
                    .flowDefinitionNodeId(definitionId)
                    .preFlowDefinitionNodeId(preNodeId + "")
                    .flowInstanceId(instance.getId())
                    .flowData("{}")
                    .status(FlowProcess.Status.NORMAL.value())
                    .forkNodeId(null)
                    .build();

            jdbcAccess.createFlowProcess(flowProcess);
        }
    }
}
