package io.terminus.doctor.workflow.service;

import io.terminus.doctor.workflow.query.FlowDefinitionNodeEventQuery;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeQuery;
import io.terminus.doctor.workflow.query.FlowDefinitionQuery;
import io.terminus.doctor.workflow.query.FlowInstanceQuery;
import io.terminus.doctor.workflow.query.FlowProcessQuery;

/**
 * Desc: 工作流统一查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/10
 */
public interface FlowQueryService {
    /**
     * 获取流程定义查询接口
     * @return
     */
    public FlowDefinitionQuery getFlowDefinitionQuery();

    /**
     * 获取流程定义 节点 查询接口
     * @return
     */
    public FlowDefinitionNodeQuery getFlowDefinitionNodeQuery();

    /**
     * 获取流程定义 事件连线 查询接口
     * @return
     */
    public FlowDefinitionNodeEventQuery getFlowDefinitionNodeEventQuery();

    /**
     * 获取流程实例查询接口
     * @return
     */
    public FlowInstanceQuery getFlowInstanceQuery();

    /**
     * 获取流程活动节点查询接口
     * @return
     */
    public FlowProcessQuery getFlowProcessQuery();
}
