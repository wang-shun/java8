package io.terminus.doctor.workflow.core;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.workflow.service.FlowDefinitionService;
import io.terminus.doctor.workflow.service.FlowProcessService;
import io.terminus.doctor.workflow.service.FlowQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return workFlowEngine.buildSynchronizedData().updateData(flowDefinitionKey, businessId);
    }

}
