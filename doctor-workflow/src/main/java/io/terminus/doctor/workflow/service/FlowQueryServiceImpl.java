package io.terminus.doctor.workflow.service;

import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeEventQuery;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeEventQueryImpl;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeQuery;
import io.terminus.doctor.workflow.query.FlowDefinitionNodeQueryImpl;
import io.terminus.doctor.workflow.query.FlowDefinitionQueryImpl;
import io.terminus.doctor.workflow.query.FlowDefinitionQuery;
import io.terminus.doctor.workflow.query.FlowHistoryInstanceQuery;
import io.terminus.doctor.workflow.query.FlowHistoryInstanceQueryImpl;
import io.terminus.doctor.workflow.query.FlowHistoryProcessQuery;
import io.terminus.doctor.workflow.query.FlowHistoryProcessQueryImpl;
import io.terminus.doctor.workflow.query.FlowInstanceQuery;
import io.terminus.doctor.workflow.query.FlowInstanceQueryImpl;
import io.terminus.doctor.workflow.query.FlowProcessQuery;
import io.terminus.doctor.workflow.query.FlowProcessQueryImpl;
import io.terminus.doctor.workflow.query.FlowProcessTrackQuery;
import io.terminus.doctor.workflow.query.FlowProcessTrackQueryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 工作流统一查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/10
 */
@Service
@Slf4j
public class FlowQueryServiceImpl implements FlowQueryService {

    @Autowired
    private WorkFlowEngine workFlowEngine;

    @Override
    public FlowDefinitionQuery getFlowDefinitionQuery() {
        return new FlowDefinitionQueryImpl(workFlowEngine);
    }

    @Override
    public FlowDefinitionNodeQuery getFlowDefinitionNodeQuery() {
        return new FlowDefinitionNodeQueryImpl(workFlowEngine);
    }

    @Override
    public FlowDefinitionNodeEventQuery getFlowDefinitionNodeEventQuery() {
        return new FlowDefinitionNodeEventQueryImpl(workFlowEngine);
    }

    @Override
    public FlowInstanceQuery getFlowInstanceQuery() {
        return new FlowInstanceQueryImpl(workFlowEngine);
    }

    @Override
    public FlowProcessQuery getFlowProcessQuery() {
        return new FlowProcessQueryImpl(workFlowEngine);
    }

    @Override
    public FlowProcessTrackQuery getFlowProcessTrackQuery() {
        return new FlowProcessTrackQueryImpl(workFlowEngine);
    }

    @Override
    public FlowHistoryInstanceQuery getFlowHistoryInstanceQuery() {
        return new FlowHistoryInstanceQueryImpl(workFlowEngine);
    }

    @Override
    public FlowHistoryProcessQuery getFlowHistoryProcessQuery() {
        return new FlowHistoryProcessQueryImpl(workFlowEngine);
    }
}
