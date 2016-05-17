package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 用户服务审批表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Data
public class DoctorServiceReview implements Serializable {
    private static final long serialVersionUID = 8803966503275820224L;

    private Long id;
    
    /**
     * 用户id
     */
    private Long userId;
    
    /**
     * 服务类型 1 猪场软件, 2 新融电商, 3 大数据, 4 生猪交易
     */
    private Integer type;
    
    /**
     * 审核状态 0 未审核, 2 待审核(提交申请) 1 通过，-1 不通过, -2 冻结
     */
    private Integer status;
    
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
