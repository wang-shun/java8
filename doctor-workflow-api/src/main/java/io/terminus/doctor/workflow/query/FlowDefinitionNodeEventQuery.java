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
    FlowDefinitionNodeEventQuery id(Long id);
    FlowDefinitionNodeEventQuery name(String name);
    FlowDefinitionNodeEventQuery flowDefinitionId(Long flowDefinitionId);
    FlowDefinitionNodeEventQuery sourceNodeId(Long sourceNodeId);
    FlowDefinitionNodeEventQuery targetNodeId(Long targetNodeId);
    FlowDefinitionNodeEventQuery bean(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    FlowDefinitionNodeEventQuery orderBy(String orderBy);
    FlowDefinitionNodeEventQuery desc();
    FlowDefinitionNodeEventQuery asc();
    Paging<FlowDefinitionNodeEvent> paging(Integer offset, Integer limit);   // 分页方法
    FlowDefinitionNodeEvent single();                                        // 唯一值
    List<FlowDefinitionNodeEvent> list();                                    // 值列表
    long size();                                                             // 数量

    List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    List<FlowDefinitionNodeEvent> findFlowDefinitionNodeEvents(Map criteria);
    FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(FlowDefinitionNodeEvent flowDefinitionNodeEvent);
    FlowDefinitionNodeEvent findFlowDefinitionNodeEventSingle(Map criteria);
    Paging<FlowDefinitionNodeEvent> findFlowDefinitionNodeEventsPaging(Map criteria, Integer offset, Integer limit);
    long findFlowDefinitionNodeEventsSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程事件连线 query 其他查询方法 /////////////////////////////
    ///////////////////////////////////////////////////////////////

}
