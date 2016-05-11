package io.terminus.doctor.workflow.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.workflow.model.FlowDefinition;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Desc: 流程定义的dao层
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Repository
public class FlowDefinitionDao extends MyBatisDao<FlowDefinition>{

    public Long count(Map criteria) {
        return this.sqlSession.selectOne(this.sqlId("count"), criteria);
    }

}
