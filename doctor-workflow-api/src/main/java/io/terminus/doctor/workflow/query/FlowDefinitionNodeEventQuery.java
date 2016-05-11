package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程定义事件连线公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public interface FlowDefinitionNodeEventQuery {

    ///////////////////////////////////////////////////////////////
    ///// 流程事件连线 query 公共查询方法 ////////////////////////////
    ///////////////////////////////////////////////////////////////
    public FlowDefinitionNodeEventQuery id(Long id);
    public FlowDefinitionNodeEventQuery name(String name);
    public FlowDefinitionNodeEventQuery flowDefinitionId(Long flowDefinitionId);
    public FlowDefinitionNodeEventQuery sourceNodeId(Long sourceNodeId);
    public FlowDefinitionNodeEventQuery targetNodeId(Long targetNodeId);
    public FlowDefinitionNodeEventQuery bean(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    public FlowDefinitionNodeEventQuery orderBy(String orderBy);
    public FlowDefinitionNodeEventQuery desc();
    public FlowDefinitionNodeEventQuery asc();
    public Paging<FlowDefinitionNodeEvent> paging(Integer offset, Integer limit);   // 分页方法
    public FlowDefinitionNodeEvent single();                                        // 唯一值
    public List<FlowDefinitionNodeEvent> list();                                    // 值列表
    public long size();                                                             // 数量

    public List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    public List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(Map criteria);
    public FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    public FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(Map criteria);
    public Paging<FlowDefinitionNodeEvent> findFlowDefinitionNodeEventsPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowDefinitionNodeEventsSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程事件连线 query 其他查询方法 /////////////////////////////
    ///////////////////////////////////////////////////////////////

}
