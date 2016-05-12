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
    ///// 流程定义 query 公共查询方法 ///////////////////////////////////
    ///////////////////////////////////////////////////////////////
    public FlowProcessQuery id(Long id);
    public FlowProcessQuery flowDefinitionNodeId(Long flowDefinitionNodeId);
    public FlowProcessQuery flowInstanceId(Long flowInstanceId);
    public FlowProcessQuery status(Integer status);
    public FlowProcessQuery assignee(String assignee);
    public FlowProcessQuery bean(FlowProcess flowProcess);
    public FlowProcessQuery orderBy(String orderBy);
    public FlowProcessQuery desc();
    public FlowProcessQuery asc();
    public Paging<FlowProcess> paging(Integer offset, Integer limit); // 分页方法
    public FlowProcess single();                                      // 唯一值
    public List<FlowProcess> list();                                  // 值列表
    public long size();                                                  // 数量

    public List<FlowProcess> findFlowProcesses(FlowProcess flowProcess);
    public List<FlowProcess> findFlowProcesses(Map criteria);
    public FlowProcess findFlowProcessSingle(FlowProcess flowProcess);
    public FlowProcess findFlowProcessSingle(Map criteria);
    public Paging<FlowProcess> findFlowProcessesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowProcessesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程定义 query 其他方法 ///////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * 获取当前流程的活动节点, 可能存在fork情况
     * @param flowInstanceId    流程实例id
     * @return
     */
    public List<FlowProcess> getCurrentProcesses(Long flowInstanceId);

    /**
     * 获取当前流程的活动节点, 一个业务人员同时只能办理一个节点
     * @param flowInstanceId    流程实例id
     * @param assignee          处理人员
     * @return
     */
    public FlowProcess getCurrentProcess(Long flowInstanceId, String assignee);
}
