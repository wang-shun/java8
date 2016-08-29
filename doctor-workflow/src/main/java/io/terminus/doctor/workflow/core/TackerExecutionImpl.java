package io.terminus.doctor.workflow.core;

import com.google.common.base.Throwables;
import io.terminus.doctor.workflow.event.ITacker;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.StringHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Created by xiao on 16/8/19.
 */
@Slf4j
public class TackerExecutionImpl implements TackerExecution{
    private WorkFlowEngine workFlowEngine;
    private Long businessId;
    private String flowData;
    private String flowDefinitionKey;
    private FlowProcess flowProcess;
    public TackerExecutionImpl(WorkFlowEngine workFlowEngine, Long businessId, String flowData, String flowDefinitionKey, FlowProcess flowProcess) {
        this.workFlowEngine = workFlowEngine;
        this.businessId = businessId;
        this.flowData = flowData;
        this.flowDefinitionKey = flowDefinitionKey;
        this.flowProcess = flowProcess;
    }

    @Override
    public ITacker getITacker(String iTackerName) {
        ITacker iTacker = workFlowEngine.buildContext().get(iTackerName);
        if (iTacker == null) {
            // 获取类的简单名称, 从上下文中获取
            iTacker = workFlowEngine.buildContext().get(
                    StringHelper.uncapitalize(iTackerName.substring(iTackerName.lastIndexOf(".") + 1)));
            if (iTacker == null) {
                try {
                    // 实例化, 并存到上下文
                    iTacker = (ITacker) Class.forName(iTackerName).newInstance();
                    workFlowEngine.buildContext().put(
                            StringHelper.uncapitalize(iTackerName.substring(iTackerName.lastIndexOf(".") + 1)), iTacker);
                } catch (Exception e) {
                    log.error("iTacker not found, iTackerName is {}, cause by {}",
                            iTackerName, Throwables.getStackTraceAsString(e));
                    AssertHelper.throwException("iTacker not found, iTackerName is {}, cause by {}",
                            iTackerName, Throwables.getStackTraceAsString(e));
                }
            }
        }
        return iTacker;
    }

    @Override
    public String getFlowDefinitionKey() {
        return flowDefinitionKey;
    }

    @Override
    public Long getBusinessId() {
        return businessId;
    }

    @Override
    public FlowProcess getFlowProcess() {
        return flowProcess;
    }

    @Override
    public String getFlowData() {
        return flowData;
    }

}
