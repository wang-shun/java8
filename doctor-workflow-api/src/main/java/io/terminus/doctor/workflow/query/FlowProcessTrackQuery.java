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
    FlowProcessTrackQuery id(Long id);
    FlowProcessTrackQuery flowDefinitionNodeId(Long flowDefinitionNodeId);
    FlowProcessTrackQuery flowInstanceId(Long flowInstanceId);
    FlowProcessTrackQuery status(Integer status);
    FlowProcessTrackQuery assignee(String assignee);
    FlowProcessTrackQuery operatorId(Long operatorId);
    FlowProcessTrackQuery operatorName(String operatorName);
    FlowProcessTrackQuery bean(FlowProcessTrack flowProcessTrack);
    FlowProcessTrackQuery orderBy(String orderBy);
    FlowProcessTrackQuery desc();
    FlowProcessTrackQuery asc();
    Paging<FlowProcessTrack> paging(Integer offset, Integer limit); // 分页方法
    FlowProcessTrack single();                                      // 唯一值
    List<FlowProcessTrack> list();                                  // 值列表
    long size();                                                    // 数量

    List<FlowProcessTrack> findFlowProcessTracks(FlowProcessTrack flowProcessTrack);
    List<FlowProcessTrack> findFlowProcessTracks(Map criteria);
    FlowProcessTrack findFlowProcessTrackSingle(FlowProcessTrack flowProcessTrack);
    FlowProcessTrack findFlowProcessTrackSingle(Map criteria);
    Paging<FlowProcessTrack> findFlowProcessTracksPaging(Map criteria, Integer offset, Integer limit);
    long findFlowProcessTracksSize(Map criteria);

    ///////////////////////////////////////////////////////////////
    ///// 流程节点跟踪 query 其他方法 ////////////////////////////////
    ///////////////////////////////////////////////////////////////

}
