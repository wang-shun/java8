package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.event.ITacker;
import io.terminus.doctor.workflow.model.FlowProcess;

/**
 * Created by xiao on 16/8/19.
 */
public interface TackerExecution {

    /**
     * 获取tacker
     * @param iTackerName
     * @return
     */
    ITacker getITacker(String iTackerName);

    /**
     * 获取flowdata
     * @return
     */
    String getFlowData();

    Long getBusinessId();

    FlowProcess getFlowProcess();

    String getFlowDefinitionKey();
}
