package io.terminus.doctor.workflow.core;

import io.terminus.common.model.Response;
import io.terminus.doctor.workflow.service.FlowDefinitionService;
import io.terminus.doctor.workflow.service.FlowProcessService;
import io.terminus.doctor.workflow.service.FlowQueryService;

/**
 * Desc: 工作流公共服务类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/12
 */
public interface WorkFlowService {
    /**
     * 获取流程定义相关的服务类
     * @return
     */
    FlowDefinitionService getFlowDefinitionService();

    /**
     * 获取流程流转相关的服务类
     * @return
     */
    FlowProcessService getFlowProcessService();

    /**
     * 获取流程查询相关的服务类
     * @return
     */
    FlowQueryService getFlowQueryService();

    /**
     * 执行定时Task任务, 此方法供job模块运行
     */
    void doTimerSchedule();

    /**
     *更新数据库数据
     * @param flowDefinitionKey
     * @param businessId
     * @return
     */
    Response<Boolean> updateData(String flowDefinitionKey, Long businessId);
}
