package io.terminus.doctor.workflow.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.workflow.model.FlowInstance;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程实例相关 DAO 层
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
@Repository
public class FlowInstanceDao extends MyBatisDao<FlowInstance> {

    /**
     * 根据流程定义key和业务id查询流程实例
     * @param flowDefinitionKey 流程定义key
     * @param businessId        业务id
     * @return
     */
    public List<FlowInstance> findExistFlowInstance(String flowDefinitionKey, Long businessId) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("flowDefinitionKey", flowDefinitionKey);
        criteria.put("businessId", businessId);
        return getSqlSession().selectList(sqlId("list"), criteria);
    }
}
