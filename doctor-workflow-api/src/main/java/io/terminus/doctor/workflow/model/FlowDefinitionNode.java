package io.terminus.doctor.workflow.model;

import io.terminus.doctor.workflow.node.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Desc: 流程定义节点信息类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
     * 节点标签名称
     */
    private String nodeName;
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
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 节点类型
     */
    public static enum Type{

        START(1, "开始节点", Node.NODE_START),
        TASK(2, "任务节点", Node.NODE_TASK),
        DECISION(3, "选择节点", Node.NODE_DECISION),
        FORK(4, "并行节点", Node.NODE_FORK),
        JOIN(5, "并行汇聚节点", Node.NODE_JOIN),
        SUBSTART(10, "子流程开始节点", Node.NODE_SUB_START),
        SUBEND(-10, "子流程结束节点", Node.NODE_SUB_END),
        END(-1, "结束节点", Node.NODE_END);

        private final int value;
        private final String describe;
        private final String nodeName;

        Type(int value, String describe, String nodeName){
            this.value = value;
            this.describe = describe;
            this.nodeName = nodeName;
        }
        public int value(){
            return this.value;
        }
        public String nodeName(){
            return this.nodeName;
        }
        @Override
        public String toString() {
            return describe;
        }

        /**
         * 根据枚举值获取当前节点的名称
         * @param value
         * @return
         */
        public static String nodeName(int value) {
            for (Type type : Type.values()) {
                if(type.value() == value) {
                    return type.nodeName();
                }
            }
            throw new IllegalArgumentException("flow.node.type.undefined");
        }

        /**
         * 根据节点名称获取枚举的value值
         * @param nodeName
         * @return
         */
        public static int vlaue(String nodeName) {
            for(Type type : Type.values()) {
                if(Objects.equals(nodeName, type.nodeName())) {
                    return type.value();
                }
            }
            throw new IllegalArgumentException("flow.node.type.undefined");
        }

        /**
         * 根据枚举值获取当前节点的描述
         * @param value
         * @return
         */
        public static String describe(int value) {
            for (Type type : Type.values()) {
                if(type.value() == value) {
                    return type.toString();
                }
            }
            throw new IllegalArgumentException("flow.node.type.undefined");
        }
    }
}
