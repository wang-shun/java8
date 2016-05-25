package io.terminus.doctor.workflow.base;

import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeEventQuery;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeQuery;
import io.terminus.doctor.workflow.query.FlowDefinitionQuery;
import io.terminus.doctor.workflow.query.FlowHistoryInstanceQuery;
import io.terminus.doctor.workflow.query.FlowHistoryProcessQuery;
import io.terminus.doctor.workflow.query.FlowInstanceQuery;
import io.terminus.doctor.workflow.query.FlowProcessQuery;
import io.terminus.doctor.workflow.query.FlowProcessTrackQuery;
import io.terminus.doctor.workflow.service.FlowDefinitionService;
import io.terminus.doctor.workflow.service.FlowProcessService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desc: 工作流基础测试类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ServiceConfiguration.class)
@Transactional
@Rollback
@ActiveProfiles("test")
public abstract class BaseServiceTest {

    @Autowired
    protected WorkFlowService workFlowService;

    /**
     * 流程定义相关的Service
     */
    protected FlowDefinitionService defService() {
        return workFlowService.getFlowDefinitionService();
    }

    /**
     * 流程流转相关的Service
     */
    protected FlowProcessService processService() {
        return workFlowService.getFlowProcessService();
    }

    /**
     * 流程定义查询相关的Service
     */
    protected FlowDefinitionQuery defQuery() {
        return workFlowService.getFlowQueryService().getFlowDefinitionQuery();
    }

    /**
     * 流程定义 节点 相关的Service
     */
    protected FlowDefinitionNodeQuery nodeQuery() {
        return workFlowService.getFlowQueryService().getFlowDefinitionNodeQuery();
    }

    /**
     * 流程定义节点 事件连线 相关的Service
     */
    protected FlowDefinitionNodeEventQuery transitionQuery() {
        return workFlowService.getFlowQueryService().getFlowDefinitionNodeEventQuery();
    }

    /**
     * 流程 实例 相关的Service
     */
    protected FlowInstanceQuery instanceQuery() {
        return workFlowService.getFlowQueryService().getFlowInstanceQuery();
    }

    /**
     * 流程 活动节点 相关的Service
     */
    protected FlowProcessQuery processQuery() {
        return workFlowService.getFlowQueryService().getFlowProcessQuery();
    }

    /**
     * 流程 活动节点跟踪 相关的Service
     */
    protected FlowProcessTrackQuery processTrackQuery() {
        return workFlowService.getFlowQueryService().getFlowProcessTrackQuery();
    }

    /**
     * 流程 历史实例 相关的Service
     */
    protected FlowHistoryInstanceQuery instanceHisQuery() {
        return workFlowService.getFlowQueryService().getFlowHistoryInstanceQuery();
    }

    /**
     * 流程 历史活动节点 相关的Service
     */
    protected FlowHistoryProcessQuery processHisQuery() {
        return workFlowService.getFlowQueryService().getFlowHistoryProcessQuery();
    }
}
