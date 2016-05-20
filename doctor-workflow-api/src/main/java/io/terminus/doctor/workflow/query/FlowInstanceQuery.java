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
    FlowInstanceQuery id(Long id);
    FlowInstanceQuery name(String name);
    FlowInstanceQuery flowDefinitionId(Long flowDefinitionId);
    FlowInstanceQuery flowDefinitionKey(String flowDefinitionKey);
    FlowInstanceQuery businessId(Long businessId);
    FlowInstanceQuery status(Integer status);
    FlowInstanceQuery type(Integer type);
    FlowInstanceQuery operatorId(Long operatorId);
    FlowInstanceQuery operatorName(String operatorName);
    FlowInstanceQuery parentInstanceId(Long parentInstanceId);
    FlowInstanceQuery bean(FlowInstance flowInstance);
    FlowInstanceQuery orderBy(String orderBy);
    FlowInstanceQuery desc();
    FlowInstanceQuery asc();
    Paging<FlowInstance> paging(Integer offset, Integer limit);   // 分页方法
    FlowInstance single();                                        // 唯一值
    List<FlowInstance> list();                                    // 值列表
    long size();                                                  // 数量

    List<FlowInstance> findFlowInstances(FlowInstance flowInstance);
    List<FlowInstance> findFlowInstances(Map criteria);
    FlowInstance findFlowInstanceSingle(FlowInstance flowInstance);
    FlowInstance findFlowInstanceSingle(Map criteria);
    Paging<FlowInstance> findFlowInstancesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowInstancesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程实例 query 其他查询方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * 根据流程定义key和业务id获取所有的流程实例(包括子流程)
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @return
     */
    List<FlowInstance> getFlowInstances(String flowDefinitionKey, Long businessId);

    /**
     * 查询是否已经存在的主流程实例, 一个业务id只能启动一种key类型的流程定义, 只能存在一个主流程实例
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @return
     */
    FlowInstance getExistFlowInstance(String flowDefinitionKey, Long businessId);

    /**
     * 查询是否已经存在的子流程实例
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @return
     */
    List<FlowInstance> getExistChildFlowInstance(String flowDefinitionKey, Long businessId);
}
