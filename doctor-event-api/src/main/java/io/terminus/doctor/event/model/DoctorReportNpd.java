package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-15 20:15:53
 * Created by [ your name ]
 */
@Data
public class DoctorReportNpd implements Serializable {

    private static final long serialVersionUID = 5884879648763331806L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 猪场
     */
    private Long farmId;
    
    /**
     * 报表日期,月度单位
     */
    private Date sumAt;
    
    /**
     * 非生产总天数
     */
    private Integer npd;
    
    /**
     * 存栏。（母猪日存栏-进场未配种母猪数）
     */
    private Integer sowCount;
    
    /**
     * 天数
     */
    private Integer days;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}