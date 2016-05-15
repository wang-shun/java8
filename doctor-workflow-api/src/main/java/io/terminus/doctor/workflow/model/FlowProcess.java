package io.terminus.doctor.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 流程实例当前活动节点类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowProcess implements Serializable{
    private static final long serialVersionUID = -262051676707233121L;

    /**
     * 当前活动节点主键id
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
     * 当前节点的状态
     */
    private Integer status;
    /**
     * 当前流转处理人
     */
    private String assignee;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;

    // TODO
    public enum Status {
        /**
         * 正常
         */
        NORMAL(1,"正常"),
        /**
         * 挂起
         */
        END(2,"正常结束"),
        /**
         * 删除
         */
        DELETE(-1,"删除"),
        /**
         * 挂起
         */
        STOPED(-2,"挂起");

        private final int value;
        private final String describe;
        Status(int value,String describe){
            this.value = value;
            this.describe = describe;
        }
        public final int value(){
            return value;
        }
        @Override
        public String toString() {
            return describe;
        }
    }
}
