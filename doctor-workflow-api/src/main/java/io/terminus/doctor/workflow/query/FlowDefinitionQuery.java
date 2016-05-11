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
    public FlowDefinitionQuery id(Long id);
    public FlowDefinitionQuery key(String key);
    public FlowDefinitionQuery version(Long version);
    public FlowDefinitionQuery status(Integer status);
    public FlowDefinitionQuery operatorId(Long operatorId);
    public FlowDefinitionQuery operatorName(String operatorName);
    public FlowDefinitionQuery bean(FlowDefinition flowDefinition);
    public FlowDefinitionQuery orderBy(String orderBy);
    public FlowDefinitionQuery desc();
    public FlowDefinitionQuery asc();
    public Paging<FlowDefinition> paging(Integer offset, Integer limit); // 分页方法
    public FlowDefinition single();                                      // 唯一值
    public List<FlowDefinition> list();                                  // 值列表
    public long size();                                                  // 数量

    public List<FlowDefinition> findFlowDefinitions(FlowDefinition flowDefinition);
    public List<FlowDefinition> findFlowDefinitions(Map criteria);
    public FlowDefinition findFlowDefinitionSingle(FlowDefinition flowDefinition);
    public FlowDefinition findFlowDefinitionSingle(Map criteria);
    public Paging<FlowDefinition> findFlowDefinitionsPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowDefinitionsSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程定义 query 其他方法 ///////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 根据流程定义的key值获取当前最新版本的流程定义
     * @param key
     * @return
     */
    public FlowDefinition findLatestDefinitionByKey(String key);
}
