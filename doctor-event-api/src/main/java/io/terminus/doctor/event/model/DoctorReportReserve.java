package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 15:58:35
 * Created by [ your name ]
 */
@Data
public class DoctorReportReserve implements Serializable {

    private static final long serialVersionUID = -3361336703001569729L;

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
     */
    private String dateType;
    
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
     */
    private String orzType;
    
    /**
     * 期初存栏
     */
    private Integer start;
    
    /**
     * 转入数量
     */
    private String turnInto;
    
    /**
     * 转种猪数量
     */
    private String turnSeed;
    
    /**
     * 死亡数量
     */
    private String dead;
    
    /**
     * 淘汰数量
     */
    private String weedOut;
    
    /**
     * 死淘率
     */
    private Double deadWeedOutRate;
    
    /**
     * 转育肥数量
     */
    private String toFatten;
    
    /**
     * 销售数量
     */
    private String sale;
    
    /**
     * 转场数量
     */
    private String chgFarmOut;
    
    /**
     * 其他减少
     */
    private String otherChange;
    
    /**
     * 日均存栏
     */
    private Double dailyLivestockOnHand;
    
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