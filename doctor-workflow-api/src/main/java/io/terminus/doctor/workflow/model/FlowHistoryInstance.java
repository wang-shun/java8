package io.terminus.doctor.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 历史流程实例类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowHistoryInstance implements Serializable {

    private static final long serialVersionUID = 3137006164403833536L;

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
     * 业务数据
     */
    private String businessData;
    /**
     * 流程实例状态
     * @see io.terminus.doctor.workflow.model.FlowInstance.Status
     */
    private Integer status;
    /**
     * 流程实例类型
     * @see io.terminus.doctor.workflow.model.FlowInstance.Type
     */
    private Integer type;
    /**
     * 历史实例描述
     */
    private String describe;
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
    /**
     * 记录删除的实例id
     */
    private Long externalHistoryId;
}
