package io.terminus.doctor.workflow.core;

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
    public FlowDefinitionService getFlowDefinitionService();

    /**
     * 获取流程流转相关的服务类
     * @return
     */
    public FlowProcessService getFlowProcessService();

    /**
     * 获取流程查询相关的服务类
     * @return
     */
    public FlowQueryService getFlowQueryService();

}
