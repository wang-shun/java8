package io.terminus.doctor.workflow.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.workflow.model.FlowProcess;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Desc: 当前活动流程节点 DAO 层
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/29
 */
@Repository
public class FlowProcessDao extends MyBatisDao<FlowProcess> {

    public Long count(Map criteria) {
        return this.sqlSession.selectOne(this.sqlId("count"), criteria);
    }

}
