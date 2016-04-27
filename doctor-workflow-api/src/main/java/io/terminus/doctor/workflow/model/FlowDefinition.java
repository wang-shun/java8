package io.terminus.doctor.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 流程定义类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowDefinition implements Serializable {
    private static final long serialVersionUID = 1719062480277024027L;
    /**
     * 流程定义的id
     */
    private Long id;
    /**
     * 流程定义唯一标识, 按照版本号区分
     */
    private String key;
    /**
     * 流程定义名称
     */
    private String name;
    /**
     * 版本号，用于获取最新的流程定义
     */
    private Long version;
    /**
     * 资源文件的名称(通常指流程定义的xml文件)
     */
    private String resourceName;
    /**
     * 流程定义的资源内容(通常是xml中的内容)
     */
    private String resource;
    /**
     * 流程定义的状态(1:正常, -1:删除, -2:禁用)
     * @see Status
     */
    private Integer status;
    /**
     * 发布者(上传流程定义的人)id
     */
    private Long operatorId;
    /**
     * 发布者姓名
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

    /**
     * 状态枚举
     */
    public static enum Status{
        /**
         * 正常
         */
        NORMAL(1,"正常"),
        /**
         * 删除
         */
        DELETE(-1,"删除"),
        /**
         * 禁用
         */
        DISABLE(-2,"禁用");

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
