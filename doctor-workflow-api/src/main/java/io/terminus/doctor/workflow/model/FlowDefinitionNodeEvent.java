package io.terminus.doctor.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 流程节点之间的事件触发驱动(连线)
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowDefinitionNodeEvent implements Serializable{
    private static final long serialVersionUID = -5849629810016607854L;
    /**
     * 事件驱动id
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 流程定义id
     * @see FlowDefinition
     */
    private Long flowDefinitionId;
    /**
     * 源节点id
     * @see FlowDefinitionNode
     */
    private Long sourceNodeId;
    /**
     * 事件全类名
     * @see
     */
    private String handler;
    /**
     * 事件驱动表达式
     */
    private String expression;
    /**
     * 目标节点id
     * @see FlowDefinitionNode
     */
    private Long targetNodeId;
    /**
     * 连接描述
     */
    private String describe;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;
}
