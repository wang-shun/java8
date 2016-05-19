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
    FlowHistoryInstanceQuery id(Long id);
    FlowHistoryInstanceQuery name(String name);
    FlowHistoryInstanceQuery flowDefinitionId(Long flowDefinitionId);
    FlowHistoryInstanceQuery flowDefinitionKey(String flowDefinitionKey);
    FlowHistoryInstanceQuery businessId(Long businessId);
    FlowHistoryInstanceQuery status(Integer status);
    FlowHistoryInstanceQuery type(Integer type);
    FlowHistoryInstanceQuery operatorId(Long operatorId);
    FlowHistoryInstanceQuery operatorName(String operatorName);
    FlowHistoryInstanceQuery parentInstanceId(Long parentInstanceId);
    FlowHistoryInstanceQuery bean(FlowHistoryInstance flowHistoryInstance);
    FlowHistoryInstanceQuery orderBy(String orderBy);
    FlowHistoryInstanceQuery desc();
    FlowHistoryInstanceQuery asc();
    Paging<FlowHistoryInstance> paging(Integer offset, Integer limit);   // 分页方法
    FlowHistoryInstance single();                                        // 唯一值
    List<FlowHistoryInstance> list();                                    // 值列表
    long size();                                                         // 数量

    List<FlowHistoryInstance> findFlowHistoryInstances(FlowHistoryInstance flowHistoryInstance);
    List<FlowHistoryInstance> findFlowHistoryInstances(Map criteria);
    FlowHistoryInstance findFlowHistoryInstanceSingle(FlowHistoryInstance flowHistoryInstance);
    FlowHistoryInstance findFlowHistoryInstanceSingle(Map criteria);
    Paging<FlowHistoryInstance> findFlowHistoryInstancesPaging(Map criteria, Integer offset, Integer limit);
    long findFlowHistoryInstancesSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程实例历史 query 其他查询方法 ////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * 根据流程定义key和业务id获取流程实例历史
     * @param flowDefinitionKey     流程定义key
     * @param businessId            业务id
     * @return
     */
    List<FlowHistoryInstance> getFlowHistoryInstances(String flowDefinitionKey, Long businessId);
}
