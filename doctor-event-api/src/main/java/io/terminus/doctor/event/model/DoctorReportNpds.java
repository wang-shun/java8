package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-25 09:58:17
 * Created by [ your name ]
 */
@Data
public class DoctorReportNpds implements Serializable {

    private static final long serialVersionUID = -3759491719468822831L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 猪场
     */
    private Long farmId;
    
    /**
     * 报表日期
     */
    private Date sumAt;
    
    /**
     * 非生产总天数
     */
    private Integer npd;
    
    /**
     * 统计的母猪的数量
     */
    private Integer sowQuantity;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}