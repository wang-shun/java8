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
    public FlowHistoryProcessQuery id(Long id);
    public FlowHistoryProcessQuery flowDefinitionNodeId(Long flowDefinitionNodeId);
    public FlowHistoryProcessQuery flowInstanceId(Long flowInstanceId);
    public FlowHistoryProcessQuery status(Integer status);
    public FlowHistoryProcessQuery assignee(String assignee);
    public FlowHistoryProcessQuery operatorId(Long operatorId);
    public FlowHistoryProcessQuery operatorName(String operatorName);
    public FlowHistoryProcessQuery bean(FlowHistoryProcess flowHistoryProcess);
    public FlowHistoryProcessQuery orderBy(String orderBy);
    public FlowHistoryProcessQuery desc();
    public FlowHistoryProcessQuery asc();
    public Paging<FlowHistoryProcess> paging(Integer offset, Integer limit); // 分页方法
    public FlowHistoryProcess single();                                      // 唯一值
    public List<FlowHistoryProcess> list();                                  // 值列表
    public long size();                                                      // 数量

    public List<FlowHistoryProcess> findFlowHistoryProcesses(FlowHistoryProcess flowHistoryProcess);
    public List<FlowHistoryProcess> findFlowHistoryProcesses(Map criteria);
    public FlowHistoryProcess findFlowHistoryProcessSingle(FlowHistoryProcess flowHistoryProcess);
    public FlowHistoryProcess findFlowHistoryProcessSingle(Map criteria);
    public Paging<FlowHistoryProcess> findFlowHistoryProcessesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowHistoryProcessesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程历史节点 query 其他方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////
}
