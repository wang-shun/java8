package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 用户服务状态
 * author: 陈增辉
 * Date: 2016-6-6
 */
@Data
public class ServiceReviewOpenDto implements Serializable {
    private static final long serialVersionUID = 5716454958895506633L;

    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户申请服务时填写的"真实姓名"
     */
    private String realName;

    /**
     * 服务类型 1 猪场软件, 2 新融电商, 3 大数据, 4 生猪交易
     * @see io.terminus.doctor.user.model.DoctorServiceReview.Type
     */
    private Integer type;
    
    /**
     * 审核状态 0 未申请, 2 待审核(提交申请), 1 审核通过, -1 审核不通过, -2 冻结申请资格
     * @see io.terminus.doctor.user.model.DoctorServiceReview.Status
     */
    private Integer status;

    /**
     * 服务状态,1-开通,0-未开通
     * @see io.terminus.doctor.user.model.DoctorServiceStatus.Status
     */
    private Integer serviceStatus;

    /**
     * 服务审批不通过或申请资格被冻结的原因
     */
    private String reason;
    
    /**
     * 审批人id
     */
    private Long reviewerId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 修改时间
     */
    private Date updatedAt;

}
