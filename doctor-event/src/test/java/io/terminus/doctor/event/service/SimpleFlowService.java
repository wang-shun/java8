package io.terminus.doctor.event.service;

import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * Desc: 简单流程服务类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/19
 */
@Service
@Slf4j
public class SimpleFlowService {

    @Autowired
    private WorkFlowService workFlowService;

    /**
     * 部署流程
     * @param inputStream
     */
    public void depoly(InputStream inputStream) {
        try{
            workFlowService.getFlowDefinitionService().deploy(inputStream);
        }catch (Exception e) {
            log.error("部署流程失败!!!");
        }
    }

    /**
     * 根据流程定义key获取流程定义
     * @param flowDefinitionKey
     */
    public List<FlowDefinition> getFlowDefinitions(String flowDefinitionKey) {
        return workFlowService.getFlowQueryService()
                .getFlowDefinitionQuery()
                .getDefinitionsByKey(flowDefinitionKey);
    }

    /**
     * 启动一个流程实例
     * @param flowDefinitionKey
     * @param businessId
     */
    public void startFlowInstance(String flowDefinitionKey, Long businessId) {
        try{
            workFlowService.getFlowProcessService().startFlowInstance(flowDefinitionKey, businessId);
        }catch (Exception e) {
            log.error("部署流程实例失败!!!");
        }
    }

    /**
     * 获取当前的主流程实例
     * @param flowDefinitionKey
     * @param businessId
     * @return
     */
    public FlowInstance getCurrentInstance(String flowDefinitionKey, Long businessId) {
        return workFlowService.getFlowQueryService()
                .getFlowInstanceQuery()
                .getExistFlowInstance(flowDefinitionKey, businessId);
    }

    /**
     * 获取当前执行的任务
     * @param flowInstanceId
     * @return
     */
    public List<FlowProcess> getCurrentProcess(Long flowInstanceId) {
        return workFlowService.getFlowQueryService()
                .getFlowProcessQuery()
                .getCurrentProcesses(flowInstanceId);
    }


    /**
     * 执行流程
     * @param flowDefinitionKey
     * @param businessId
     */
    public void execute(String flowDefinitionKey, Long businessId) {
        try {
            workFlowService.getFlowProcessService().getExecutor(flowDefinitionKey, businessId).execute();
        }catch (Exception e) {
            log.error("执行流程失败!!!");
        }
    }

}
