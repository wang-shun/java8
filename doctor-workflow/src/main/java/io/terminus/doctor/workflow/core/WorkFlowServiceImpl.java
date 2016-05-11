package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.service.FlowDefinitionService;
import io.terminus.doctor.workflow.service.FlowProcessService;
import io.terminus.doctor.workflow.service.FlowQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 工作流公共服务类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/12
 */
@Service
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
}
