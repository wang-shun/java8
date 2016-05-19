package io.terminus.doctor.workflow.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Desc: 流程定义节点 连接事件 DAO层
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/27
 */
@Repository
public class FlowDefinitionNodeEventDao extends MyBatisDao<FlowDefinitionNodeEvent> {

    public Long count(Map criteria) {
        return this.sqlSession.selectOne(this.sqlId("count"), criteria);
    }

}
