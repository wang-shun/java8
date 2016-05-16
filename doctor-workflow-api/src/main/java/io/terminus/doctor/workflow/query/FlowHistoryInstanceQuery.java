package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowHistoryInstance;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程实例历史公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public interface FlowHistoryInstanceQuery {

    ///////////////////////////////////////////////////////////////
    ///// 流程实例历史 query 公共查询方法 ////////////////////////////
    ///////////////////////////////////////////////////////////////
    public FlowHistoryInstanceQuery id(Long id);
    public FlowHistoryInstanceQuery name(String name);
    public FlowHistoryInstanceQuery flowDefinitionId(Long flowDefinitionId);
    public FlowHistoryInstanceQuery flowDefinitionKey(String flowDefinitionKey);
    public FlowHistoryInstanceQuery businessId(Long businessId);
    public FlowHistoryInstanceQuery status(Integer status);
    public FlowHistoryInstanceQuery type(Integer type);
    public FlowHistoryInstanceQuery operatorId(Long operatorId);
    public FlowHistoryInstanceQuery operatorName(String operatorName);
    public FlowHistoryInstanceQuery parentInstanceId(Long parentInstanceId);
    public FlowHistoryInstanceQuery bean(FlowHistoryInstance flowHistoryInstance);
    public FlowHistoryInstanceQuery orderBy(String orderBy);
    public FlowHistoryInstanceQuery desc();
    public FlowHistoryInstanceQuery asc();
    public Paging<FlowHistoryInstance> paging(Integer offset, Integer limit);   // 分页方法
    public FlowHistoryInstance single();                                        // 唯一值
    public List<FlowHistoryInstance> list();                                    // 值列表
    public long size();                                                         // 数量

    public List<FlowHistoryInstance> findFlowHistoryInstances(FlowHistoryInstance flowHistoryInstance);
    public List<FlowHistoryInstance> findFlowHistoryInstances(Map criteria);
    public FlowHistoryInstance findFlowHistoryInstanceSingle(FlowHistoryInstance flowHistoryInstance);
    public FlowHistoryInstance findFlowHistoryInstanceSingle(Map criteria);
    public Paging<FlowHistoryInstance> findFlowHistoryInstancesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowHistoryInstancesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程实例历史 query 其他查询方法 ////////////////////////////
    ///////////////////////////////////////////////////////////////

}
