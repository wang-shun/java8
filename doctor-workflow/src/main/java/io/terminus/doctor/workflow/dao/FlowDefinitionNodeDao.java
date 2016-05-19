package io.terminus.doctor.workflow.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Desc: 流程定义 节点 DAO层
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/27
 */
@Repository
public class FlowDefinitionNodeDao extends MyBatisDao<FlowDefinitionNode> {

    public Long count(Map criteria) {
        return this.sqlSession.selectOne(this.sqlId("count"), criteria);
    }

}
