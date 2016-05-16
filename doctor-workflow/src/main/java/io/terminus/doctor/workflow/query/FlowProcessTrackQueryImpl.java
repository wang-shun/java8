package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
import io.terminus.doctor.workflow.model.FlowProcessTrack;
import io.terminus.doctor.workflow.utils.BeanHelper;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程节点跟踪公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/16
 */
public class FlowProcessTrackQueryImpl implements FlowProcessTrackQuery {

    private FlowProcessTrack flowProcessTrack;

    private WorkFlowEngine workFlowEngine;
    private JdbcAccess jdbcAccess;
    private String orderBy;
    private String desc;

    public FlowProcessTrackQueryImpl(WorkFlowEngine workFlowEngine) {
        this.workFlowEngine = workFlowEngine;
        this.jdbcAccess = workFlowEngine.buildJdbcAccess();
        this.flowProcessTrack = new FlowProcessTrack();
    }

    @Override
    public FlowProcessTrackQuery id(Long id) {
        flowProcessTrack.setId(id);
        return this;
    }

    @Override
    public FlowProcessTrackQuery flowDefinitionNodeId(Long flowDefinitionNodeId) {
        flowProcessTrack.setFlowDefinitionNodeId(flowDefinitionNodeId);
        return this;
    }

    @Override
    public FlowProcessTrackQuery flowInstanceId(Long flowInstanceId) {
        flowProcessTrack.setFlowInstanceId(flowInstanceId);
        return this;
    }

    @Override
    public FlowProcessTrackQuery status(Integer status) {
        flowProcessTrack.setStatus(status);
        return this;
    }

    @Override
    public FlowProcessTrackQuery assignee(String assignee) {
        flowProcessTrack.setAssignee(assignee);
        return this;
    }

    @Override
    public FlowProcessTrackQuery operatorId(Long operatorId) {
        flowProcessTrack.setOperatorId(operatorId);
        return this;
    }

    @Override
    public FlowProcessTrackQuery operatorName(String operatorName) {
        flowProcessTrack.setOperatorName(operatorName);
        return this;
    }

    @Override
    public FlowProcessTrackQuery bean(FlowProcessTrack flowProcessTrack) {
        this.flowProcessTrack = flowProcessTrack;
        return this;
    }

    @Override
    public FlowProcessTrackQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public FlowProcessTrackQuery desc() {
        this.desc = "true";
        return this;
    }

    @Override
    public FlowProcessTrackQuery asc() {
        this.desc = null;
        return this;
    }

    private Map getConditionMap(){
        Map criteria = BeanHelper.bean2Map(this.flowProcessTrack, true);
        criteria.put("orderBy", orderBy);
        criteria.put("desc", desc);
        return criteria;
    }

    @Override
    public Paging<FlowProcessTrack> paging(Integer offset, Integer limit) {
        return jdbcAccess.findFlowProcessTracksPaging(getConditionMap(), offset, limit);
    }

    @Override
    public FlowProcessTrack single() {
        return jdbcAccess.findFlowProcessTrackSingle(getConditionMap());
    }

    @Override
    public List<FlowProcessTrack> list() {
        return jdbcAccess.findFlowProcessTracks(getConditionMap());
    }

    @Override
    public long size() {
        return jdbcAccess.findFlowProcessesSize(getConditionMap());
    }

    @Override
    public List<FlowProcessTrack> findFlowProcessTracks(FlowProcessTrack flowProcessTrack) {
        return jdbcAccess.findFlowProcessTracks(flowProcessTrack);
    }

    @Override
    public List<FlowProcessTrack> findFlowProcessTracks(Map criteria) {
        return jdbcAccess.findFlowProcessTracks(criteria);
    }

    @Override
    public FlowProcessTrack findFlowProcessTrackSingle(FlowProcessTrack flowProcessTrack) {
        return jdbcAccess.findFlowProcessTrackSingle(flowProcessTrack);
    }

    @Override
    public FlowProcessTrack findFlowProcessTrackSingle(Map criteria) {
        return jdbcAccess.findFlowProcessTrackSingle(criteria);
    }

    @Override
    public Paging<FlowProcessTrack> findFlowProcessTracksPaging(Map criteria, Integer offset, Integer limit) {
        return jdbcAccess.findFlowProcessTracksPaging(criteria, offset, limit);
    }

    @Override
    public long findFlowProcessTracksSize(Map criteria) {
        return jdbcAccess.findFlowProcessTracksSize(criteria);
    }
}
