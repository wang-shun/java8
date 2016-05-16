package io.terminus.doctor.workflow.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.workflow.model.FlowHistoryInstance;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Desc: 流程实例历史相关 DAO 层
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
@Repository
public class FlowHistoryInstanceDao extends MyBatisDao<FlowHistoryInstance> {

    public Long count(Map criteria) {
        return this.sqlSession.selectOne(this.sqlId("count"), criteria);
    }
}
