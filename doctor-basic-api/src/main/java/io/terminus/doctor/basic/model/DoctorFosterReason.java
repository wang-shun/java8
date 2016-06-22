package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 寄养原因表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-06-22
 */
@Data
public class DoctorFosterReason implements Serializable {
    private static final long serialVersionUID = -5280987587936634306L;

    private Long id;
    
    /**
     * 寄养原因
     */
    private String reason;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 外部id
     */
    private String outId;
    
    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 创建人id
     */
    private Long creatorId;
    
    /**
     * 创建人name
     */
    private String creatorName;
    
    /**
     * 更新人id
     */
    private Long updatorId;
    
    /**
     * 更新人name
     */
    private String updatorName;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;
}
