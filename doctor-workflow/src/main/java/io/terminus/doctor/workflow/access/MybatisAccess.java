package io.terminus.doctor.workflow.access;

import io.terminus.doctor.workflow.dao.FlowDefinitionDao;
import io.terminus.doctor.workflow.dao.FlowDefinitionNodeDao;
import io.terminus.doctor.workflow.dao.FlowDefinitionNodeEventDao;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Desc: 工作流数据库层统一访问入口, MyBatis实现
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Repository
public class MybatisAccess implements JdbcAccess {

    @Autowired
    private FlowDefinitionDao flowDefinitionDao;

    @Autowired
    private FlowDefinitionNodeDao flowDefinitionNodeDao;

    @Autowired
    private FlowDefinitionNodeEventDao flowDefinitionNodeEventDao;

    /************************************** 流程定义相关 *****************************************************/
    @Override
    public void createFlowDefinition(FlowDefinition flowDefinition) {
        flowDefinitionDao.create(flowDefinition);
    }

    @Override
    public FlowDefinition findFlowDefinitionById(Long id) {
        return flowDefinitionDao.findById(id);
    }

    @Override
    public FlowDefinition findLatestDefinitionByKey(String key) {
        return flowDefinitionDao.findLatestDefinitionByKey(key);
    }

    /************************************** 流程定义 节点 相关 *****************************************************/
    @Override
    public void createFlowDefinitionNode(FlowDefinitionNode flowDefinitionNode) {
        flowDefinitionNodeDao.create(flowDefinitionNode);
    }


    /************************************** 流程定义节点 连接事件 相关 *****************************************************/
    @Override
    public void createFlowDefinitionNodeEvent(FlowDefinitionNodeEvent flowDefinitionNodeEvent) {
        flowDefinitionNodeEventDao.create(flowDefinitionNodeEvent);
    }
}
