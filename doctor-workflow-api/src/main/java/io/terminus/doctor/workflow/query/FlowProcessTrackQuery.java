package io.terminus.doctor.workflow.query;

import io.terminus.common.model.Paging;
import io.terminus.doctor.workflow.model.FlowProcessTrack;

import java.util.List;
import java.util.Map;

/**
 * Desc: 流程节点跟踪公共查询接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/16
 */
public interface FlowProcessTrackQuery {

    ///////////////////////////////////////////////////////////////
    ///// 流程节点跟踪 query 公共查询方法 ////////////////////////////
    ///////////////////////////////////////////////////////////////
    public FlowProcessTrackQuery id(Long id);
    public FlowProcessTrackQuery flowDefinitionNodeId(Long flowDefinitionNodeId);
    public FlowProcessTrackQuery flowInstanceId(Long flowInstanceId);
    public FlowProcessTrackQuery status(Integer status);
    public FlowProcessTrackQuery assignee(String assignee);
    public FlowProcessTrackQuery operatorId(Long operatorId);
    public FlowProcessTrackQuery operatorName(String operatorName);
    public FlowProcessTrackQuery bean(FlowProcessTrack flowProcessTrack);
    public FlowProcessTrackQuery orderBy(String orderBy);
    public FlowProcessTrackQuery desc();
    public FlowProcessTrackQuery asc();
    public Paging<FlowProcessTrack> paging(Integer offset, Integer limit); // 分页方法
    public FlowProcessTrack single();                                      // 唯一值
    public List<FlowProcessTrack> list();                                  // 值列表
    public long size();                                                    // 数量

    public List<FlowProcessTrack> findFlowProcessTracks(FlowProcessTrack flowProcessTrack);
    public List<FlowProcessTrack> findFlowProcessTracks(Map criteria);
    public FlowProcessTrack findFlowProcessTrackSingle(FlowProcessTrack flowProcessTrack);
    public FlowProcessTrack findFlowProcessTrackSingle(Map criteria);
    public Paging<FlowProcessTrack> findFlowProcessTracksPaging(Map criteria, Integer offset, Integer limit);
    public long findFlowProcessTracksSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程节点跟踪 query 其他方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////

}
