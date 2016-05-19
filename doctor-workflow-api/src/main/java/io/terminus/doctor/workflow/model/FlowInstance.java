package io.terminus.doctor.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 流程实例类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowInstance implements Serializable{
    private static final long serialVersionUID = 8882592068649941451L;

    /**
     * 流程实例id
     */
    private Long id;
    /**
     * 流程实例名称
     */
    private String name;
    /**
     * 流程定义的id
     */
    private Long flowDefinitionId;
    /**
     * 流程定义的key
     */
    private String flowDefinitionKey;
    /**
     * 业务id
     */
    private Long businessId;
    /**
     * 业务数据, 自定义json格式
     */
    private String businessData;
    /**
     * 流程实例状态
     */
    private Integer status;
    /**
     * 流程实例类型
     */
    private Integer type;
    /**
     * 操作人id
     */
    private Long operatorId;
    /**
     * 操作人姓名
     */
    private String operatorName;
    /**
     * 父流程实例id
     */
    private Long parentInstanceId;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;

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

    public enum Type {
        /**
         * 主流程
         */
        PARENT(1,"主流程"),
        /**
         * 子流程
         */
        CHILD(2,"子流程");

        private final int value;
        private final String describe;
        Type(int value,String describe){
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
