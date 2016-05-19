package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowDefinition;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程定义公共查询类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/10
 */
public interface FlowDefinitionQuery {

    ///////////////////////////////////////////////////////////////
    ///// 流程定义 query 公共查询方法 ///////////////////////////////////
    ///////////////////////////////////////////////////////////////
    FlowDefinitionQuery id(Long id);
    FlowDefinitionQuery key(String key);
    FlowDefinitionQuery version(Long version);
    FlowDefinitionQuery status(Integer status);
    FlowDefinitionQuery operatorId(Long operatorId);
    FlowDefinitionQuery operatorName(String operatorName);
    FlowDefinitionQuery bean(FlowDefinition flowDefinition);
    FlowDefinitionQuery orderBy(String orderBy);
    FlowDefinitionQuery desc();
    FlowDefinitionQuery asc();
    Paging<FlowDefinition> paging(Integer offset, Integer limit); // 分页方法
    FlowDefinition single();                                      // 唯一值
    List<FlowDefinition> list();                                  // 值列表
    long size();                                                  // 数量

    List<FlowDefinition> findFlowDefinitions(FlowDefinition flowDefinition);
    List<FlowDefinition> findFlowDefinitions(Map criteria);
    FlowDefinition findFlowDefinitionSingle(FlowDefinition flowDefinition);
    FlowDefinition findFlowDefinitionSingle(Map criteria);
    Paging<FlowDefinition> findFlowDefinitionsPaging(Map criteria, Integer offset, Integer limit);
    long findFlowDefinitionsSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程定义 query 其他方法 ///////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 根据流程定义的key值获取当前最新版本的流程定义
     * @param flowDefinitionKey   流程定义的key值
     * @return
     */
    FlowDefinition getLatestDefinitionByKey(String flowDefinitionKey);

    /**
     * 根据id查询流程定义
     * @param id
     * @return
     */
    FlowDefinition getDefinitionById(Long id);

    /**
     * 获取流程定义列表(状态为normal的)
     * @return
     */
    List<FlowDefinition> getDefinitions();

    /**
     * 根据流程定义key获取列表(状态为normal的)
     * @return
     */
    List<FlowDefinition> getDefinitionsByKey(String flowDefinitionKey);
}
