package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowHistoryProcess;

import java.util.List;
import java.util.Map;

/**
 * Desc: 历史流程活动节点公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public interface FlowHistoryProcessQuery {

    ///////////////////////////////////////////////////////////////
    ///// 流程历史节点 query 公共查询方法 ////////////////////////////
    ///////////////////////////////////////////////////////////////
    FlowHistoryProcessQuery id(Long id);
    FlowHistoryProcessQuery flowDefinitionNodeId(Long flowDefinitionNodeId);
    FlowHistoryProcessQuery flowInstanceId(Long flowInstanceId);
    FlowHistoryProcessQuery status(Integer status);
    FlowHistoryProcessQuery assignee(String assignee);
    FlowHistoryProcessQuery forkNodeId(Long forkNodeId);
    FlowHistoryProcessQuery operatorId(Long operatorId);
    FlowHistoryProcessQuery operatorName(String operatorName);
    FlowHistoryProcessQuery bean(FlowHistoryProcess flowHistoryProcess);
    FlowHistoryProcessQuery orderBy(String orderBy);
    FlowHistoryProcessQuery desc();
    FlowHistoryProcessQuery asc();
    Paging<FlowHistoryProcess> paging(Integer offset, Integer limit); // 分页方法
    FlowHistoryProcess single();                                      // 唯一值
    List<FlowHistoryProcess> list();                                  // 值列表
    long size();                                                      // 数量

    List<FlowHistoryProcess> findFlowHistoryProcesses(FlowHistoryProcess flowHistoryProcess);
    List<FlowHistoryProcess> findFlowHistoryProcesses(Map criteria);
    FlowHistoryProcess findFlowHistoryProcessSingle(FlowHistoryProcess flowHistoryProcess);
    FlowHistoryProcess findFlowHistoryProcessSingle(Map criteria);
    Paging<FlowHistoryProcess> findFlowHistoryProcessesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowHistoryProcessesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程历史节点 query 其他方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * 根据流程实例id获取流程任务历史
     * @param flowInstanceId    流程实例id
     * @return
     */
    List<FlowHistoryProcess> getHistoryProcess(Long flowInstanceId);
}
