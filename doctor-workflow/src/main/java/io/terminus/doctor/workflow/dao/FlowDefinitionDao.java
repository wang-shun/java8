package io.terminus.doctor.workflow.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.workflow.model.FlowDefinition;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程定义的dao层
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Repository
public class FlowDefinitionDao extends MyBatisDao<FlowDefinition>{

    /**
     * 根据key查询最新版本的流程定义
     * @param key
     * @return
     */
    public FlowDefinition findLatestDefinitionByKey(String key) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("key",key);
        criteria.put("status",FlowDefinition.Status.NORMAL.value());
        criteria.put("order","version");
        criteria.put("desc",true);
        List<FlowDefinition> list = getSqlSession().selectList(sqlId("list"), criteria);
        if(list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

}
