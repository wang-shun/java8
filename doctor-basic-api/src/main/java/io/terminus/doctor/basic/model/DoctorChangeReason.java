package io.terminus.doctor.basic.model;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 变动原因表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Data
public class DoctorChangeReason implements Serializable {
    private static final long serialVersionUID = 6395413596819548653L;

    private Long id;

    /**
     * 变动类型id
     */
    @NotNull(message = "changeTypeId.not.null")
    private Long changeTypeId;
    
    /**
     * 变动原因
     */
    @NotEmpty(message = "reason.not.empty")
    private String reason;

    /**
     * 输入码
     */
    private String srm;

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
