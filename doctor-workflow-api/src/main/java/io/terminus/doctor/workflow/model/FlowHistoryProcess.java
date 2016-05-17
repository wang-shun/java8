package io.terminus.doctor.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 流程实例历史活动节点类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowHistoryProcess implements Serializable {
    private static final long serialVersionUID = -262051676707233121L;

    /**
     * 历史活动节点主键id
     */
    private Long id;
    /**
     * 节点的定义id
     */
    private Long flowDefinitionNodeId;
    /**
     * 上一个流程节点的id, 可能存在多个, 用逗号隔开
     */
    private String preFlowDefinitionNodeId;
    /**
     * 流程实例id
     */
    private Long flowInstanceId;
    /**
     * 流转数据
     */
    private String flowData;
    /**
     * 历史节点流转描述
     */
    private String describe;
    /**
     * 当前节点的状态
     *
     * @see io.terminus.doctor.workflow.model.FlowProcess.Status
     */
    private Integer status;
    /**
     * 当前流转处理人
     */
    private String assignee;
    /**
     * fork节点id, 便于join
     */
    private Long forkNodeId;
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
