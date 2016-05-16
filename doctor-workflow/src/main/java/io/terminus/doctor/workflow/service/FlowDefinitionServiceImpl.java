package io.terminus.doctor.workflow.service;

import com.google.common.base.Throwables;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.Configuration;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.core.WorkFlowException;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.model.FlowInstance;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc: 流程服务类,包括功能如下
 *      1. 流程部署
 *      2. 流程删除
 *      3. 流程定义查询相关
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Slf4j
@Service
public class FlowDefinitionServiceImpl implements FlowDefinitionService {

    @Autowired
    private WorkFlowEngine workFlowEngine;

    private DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    /*********************** 流程 deploy 相关 *****************************************/

    @Override
    public void deploy(String sourceName) {
        deploy(sourceName, null, null);
    }

    @Override
    public void deploy(InputStream inputStream) {
        deploy(inputStream, null, null);
    }

    @Override
    public void deploy(String sourceName, Long operatorId, String operatorName) {
        deploy(this.getClass().getClassLoader().getResourceAsStream(sourceName), sourceName, operatorId, operatorName);
    }

    @Override
    public void deploy(InputStream inputStream, Long operatorId, String operatorName) {
        deploy(inputStream, null, operatorId, operatorName);
    }

    private void deploy(InputStream inputStream, String sourceName, Long operatorId, String operatorName) {
        try {
            Configuration configuration = workFlowEngine.buildConfiguration(inputStream);
            // 1. 存入流程定义对象
            FlowDefinition flowDefinition = configuration.getFlowDefinition();
            flowDefinition.setResourceName(
                    sourceName != null ?
                    sourceName.substring(sourceName.lastIndexOf("/") + 1) :
                    flowDefinition.getKey() + ".xml"
            );
            flowDefinition.setOperatorId(operatorId);
            flowDefinition.setOperatorName(operatorName);
            flowDefinition.setStatus(FlowDefinition.Status.NORMAL.value());
            // 获取最新版本的流程定义
            FlowDefinition latestVersionDefinition = workFlowEngine.buildFlowQueryService().
                    getFlowDefinitionQuery().getLatestDefinitionByKey(flowDefinition.getKey());
            if (latestVersionDefinition == null) {
                flowDefinition.setVersion(0L);
            } else {
                flowDefinition.setVersion(latestVersionDefinition.getVersion() + 1);
            }
            access().createFlowDefinition(flowDefinition);

            // 2. 存入流程节点对象
            List<FlowDefinitionNode> flowDefinitionNodes = configuration.getFlowDefinitionNodes();
            flowDefinitionNodes.forEach(node -> access().createFlowDefinitionNode(node));

            // 3. 存入流程节点连线事件对象
            List<FlowDefinitionNodeEvent> flowDefinitionNodeEvents = configuration.getFlowDefinitionNodeEvents();
            flowDefinitionNodeEvents.forEach(event-> access().createFlowDefinitionNodeEvent(event));

        } catch (Exception e) {
            log.error("[Work Flow Definition] -> 部署流程定义失败, cause by: {}", Throwables.getStackTraceAsString(e));
            throw new WorkFlowException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new WorkFlowException(e);
            }
        }
    }


    /*********************** 流程 delete 相关 *****************************************/

    @Override
    public void delete(Long flowDefinitionId) {
        delete(flowDefinitionId, false);
    }

    @Override
    public void delete(Long flowDefinitionId, boolean cascade) {
        delete(flowDefinitionId, cascade, null, null);
    }

    @Override
    public void delete(Long flowDefinitionId, boolean cascade, Long operatorId, String operatorName) {
        // 1. 停止所有的流程实例
        List<FlowInstance> flowInstances = workFlowEngine.buildFlowQueryService().getFlowInstanceQuery()
                .flowDefinitionId(flowDefinitionId)
                .list();
        if(flowInstances != null && flowInstances.size() > 0) {
            flowInstances.forEach(flowInstance ->
                    workFlowEngine.buildFlowProcessService()
                            .endFlowInstance(
                                    flowInstance.getFlowDefinitionKey(),
                                    flowInstance.getBusinessId(),
                                    cascade,
                                    "流程定义执行删除操作",
                                    operatorId,
                                    operatorName
                            )
            );
        }
        // 2. 删除对应的节点
        List<Long> flowDefinitionNodeIds = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeQuery()
                .flowDefinitionId(flowDefinitionId)
                .list()
                .stream()
                .map(FlowDefinitionNode::getId)
                .collect(Collectors.toList());
        access().deleteFlowDefinitionNode(flowDefinitionNodeIds);

        // 3. 删除对应的连接事件
        List<Long> flowDefinitionNodeEventIds = workFlowEngine.buildFlowQueryService().getFlowDefinitionNodeEventQuery()
                .flowDefinitionId(flowDefinitionId)
                .list()
                .stream()
                .map(FlowDefinitionNodeEvent::getId)
                .collect(Collectors.toList());
        access().deleteFlowDefinitionNodeEvent(flowDefinitionNodeEventIds);

        // 4. 删除流程定义(逻辑删除)
        access().deleteFlowDefinition(flowDefinitionId);
    }

    private JdbcAccess access() {
        return workFlowEngine.buildJdbcAccess();
    }
}
