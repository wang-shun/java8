package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowProcess;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程活动节点公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public interface FlowProcessQuery {

    ///////////////////////////////////////////////////////////////
    ///// 流程活动节点 query 公共查询方法 ////////////////////////////
    ///////////////////////////////////////////////////////////////
    FlowProcessQuery id(Long id);
    FlowProcessQuery flowDefinitionNodeId(Long flowDefinitionNodeId);
    FlowProcessQuery flowInstanceId(Long flowInstanceId);
    FlowProcessQuery status(Integer status);
    FlowProcessQuery assignee(String assignee);
    FlowProcessQuery bean(FlowProcess flowProcess);
    FlowProcessQuery orderBy(String orderBy);
    FlowProcessQuery desc();
    FlowProcessQuery asc();
    Paging<FlowProcess> paging(Integer offset, Integer limit); // 分页方法
    FlowProcess single();                                      // 唯一值
    List<FlowProcess> list();                                  // 值列表
    long size();                                                  // 数量

    List<FlowProcess> findFlowProcesses(FlowProcess flowProcess);
    List<FlowProcess> findFlowProcesses(Map criteria);
    FlowProcess findFlowProcessSingle(FlowProcess flowProcess);
    FlowProcess findFlowProcessSingle(Map criteria);
    Paging<FlowProcess> findFlowProcessesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowProcessesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程活动节点 query 其他方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * 获取当前流程的活动节点, 可能存在fork情况
     * @param flowInstanceId    流程实例id
     * @return
     */
    List<FlowProcess> getCurrentProcesses(Long flowInstanceId);

    /**
     * 获取当前流程的活动节点, 一个业务人员同时只能办理一个节点
     * @param flowInstanceId    流程实例id
     * @param assignee          处理人员
     * @return
     */
    FlowProcess getCurrentProcess(Long flowInstanceId, String assignee);
}
