package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowInstance;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程实例公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/11
 */
public interface FlowInstanceQuery {

    ///////////////////////////////////////////////////////////////
    ///// 流程实例 query 公共查询方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////
    public FlowInstanceQuery id(Long id);
    public FlowInstanceQuery name(String name);
    public FlowInstanceQuery flowDefinitionId(Long flowDefinitionId);
    public FlowInstanceQuery flowDefinitionKey(String flowDefinitionKey);
    public FlowInstanceQuery businessId(Long businessId);
    public FlowInstanceQuery status(Integer status);
    public FlowInstanceQuery type(Integer type);
    public FlowInstanceQuery operatorId(Long operatorId);
    public FlowInstanceQuery operatorName(String operatorName);
    public FlowInstanceQuery parentInstanceId(Long parentInstanceId);
    public FlowInstanceQuery bean(FlowInstance flowInstance);
    public FlowInstanceQuery orderBy(String orderBy);
    public FlowInstanceQuery desc();
    public FlowInstanceQuery asc();
    public Paging<FlowInstance> paging(Integer offset, Integer limit);   // 分页方法
    public FlowInstance single();                                        // 唯一值
    public List<FlowInstance> list();                                    // 值列表
    public long size();                                                  // 数量

    public List<FlowInstance> findFlowInstances(FlowInstance flowInstance);
    public List<FlowInstance> findFlowInstances(Map criteria);
    public FlowInstance findFlowInstanceSingle(FlowInstance flowInstance);
    public FlowInstance findFlowInstanceSingle(Map criteria);
    public Paging<FlowInstance> findFlowInstancesPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowInstancesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程实例 query 其他查询方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 查询是否已经存在的主流程实例, 一个业务id只能启动一种key类型的流程定义, 只能存在一个主流程实例
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @return
     */
    public FlowInstance getExistFlowInstance(String flowDefinitionKey, Long businessId);
}
