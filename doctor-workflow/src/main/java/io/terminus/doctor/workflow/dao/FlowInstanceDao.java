package io.terminus.doctor.workflow.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.workflow.model.FlowInstance;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Desc: 流程实例相关 DAO 层
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
@Repository
public class FlowInstanceDao extends MyBatisDao<FlowInstance> {

    public Long count(Map criteria) {
        return this.sqlSession.selectOne(this.sqlId("count"), criteria);
    }
}
