package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:13:08
 * Created by [ your name ]
 */
@Data
public class DoctorReportEfficiency implements Serializable {

    private static final long serialVersionUID = -8165531246790026979L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 
     */
    private Date sumAt;
    
    /**
     * 
     */
    private String sumAtName;
    
    /**
     * 日期类型，日周月季年
     * @see io.terminus.doctor.event.enums.DateDimension
     */
    private Integer dateType;
    
    /**
     * 组织ID
     */
    private Long orzId;
    
    /**
     * 组织名称
     */
    private String orzName;
    
    /**
     * 组织类型，猪场，公司，集团
     * @see io.terminus.doctor.event.enums.OrzDimension
     */
    private Integer orzType;
    
    /**
     * 非生产天数
     */
    private BigDecimal npd;
    
    /**
     * 年生产胎次
     */
    private BigDecimal birthPerYear;
    
    /**
     * psy
     */
    private BigDecimal psy;
    
    /**
     * 妊娠期
     */
    private Integer pregnancy;
    
    /**
     * 哺乳期
     */
    private Integer lactation;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}