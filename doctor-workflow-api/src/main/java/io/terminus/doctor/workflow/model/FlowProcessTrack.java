package io.terminus.doctor.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 流程实例的节点跟踪类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowProcessTrack implements Serializable{
    private static final long serialVersionUID = 7922591128500648319L;

    private Long id;
    /**
     * 节点的定义id
     */
    private Long flowDefinitionNodeId;
    /**
     * 流程实例id
     */
    private Long flowDefinitionId;
    /**
     * 流转数据
     */
    private String flowData;
    /**
     * 当前节点的状态
     * @see io.terminus.doctor.workflow.model.FlowProcess.Status
     */
    private Integer status;
    /**
     * 当前流转处理人
     */
    private String assignee;
    /**
     * 处理人id
     */
    private Long operatorId;
    /**
     * 处理人name
     */
    private String operatorName;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;
}
