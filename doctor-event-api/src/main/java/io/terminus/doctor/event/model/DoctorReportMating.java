package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:18:03
 * Created by [ your name ]
 */
@Data
public class DoctorReportMating implements Serializable {

    private static final long serialVersionUID = -1176306427699990592L;

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
     * 期初存栏
     */
    private Integer start;
    
    /**
     * 后备转入
     */
    private Integer houbeiIn;
    
    /**
     * 断奶转入
     */
    private String sowPhWeanIn;
    
    /**
     * 其他转入
     */
    private Integer otherIn;
    
    /**
     * 死亡数量
     */
    private Integer dead;
    
    /**
     * 淘汰数量
     */
    private Integer weedOut;
    
    /**
     * 销售数量
     */
    private Integer sale;
    
    /**
     * 转场数量
     */
    private Integer chgFarmOut;
    
    /**
     * 其他减少
     */
    private Integer otherChange;
    
    /**
     * 配种数量
     */
    private Integer matingCount;
    
    /**
     * 已配种母猪数
     */
    private Integer matingSowCount;
    
    /**
     * 怀孕母猪数量
     */
    private Integer pregnancySowCount;
    
    /**
     * 空怀母猪数量
     */
    private Integer noPregnancySowCount;
    
    /**
     * 妊检阳性数量
     */
    private String pregPositive;
    
    /**
     * 妊检阴性数量
     */
    private String pregNegative;
    
    /**
     * 妊检返情数量
     */
    private String pregFanqing;
    
    /**
     * 流产数量
     */
    private String pregLiuchan;
    
    /**
     * 断奶7日配种率
     */
    private Double matingRate;
    
    /**
     * 期末存栏
     */
    private Integer end;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}