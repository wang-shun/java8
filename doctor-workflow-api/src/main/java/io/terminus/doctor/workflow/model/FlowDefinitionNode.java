package io.terminus.doctor.workflow.model;

import lombok.Data;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 流程定义节点信息类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/23
 */
@Data
@Builder
public class FlowDefinitionNode implements Serializable{
    private static final long serialVersionUID = -7515055544608408573L;
    /**
     * 节点id
     */
    private Long id;
    /**
     * 流程定义id
     * @see FlowDefinition
     */
    private Long flowDefinitionId;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点类型
     */
    private Integer type;
    /**
     * 当前处理人
     */
    private String assignee;
    /**
     * 节点x轴偏移量
     */
    private Double pointX;
    /**
     * 节点y轴偏移量
     */
    private Double pointY;
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 节点类型
     * TODO
     */
    public static enum Type{

    }
}
