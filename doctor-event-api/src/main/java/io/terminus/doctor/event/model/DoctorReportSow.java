package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:25:01
 * Created by [ your name ]
 */
@Data
public class DoctorReportSow implements Serializable {

    private static final long serialVersionUID = -5345779441485285320L;

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
     * 销售数量
     */
    private String sale;
    
    /**
     * 转场数量
     */
    private Integer chgFarmOut;
    
    /**
     * 其他减少
     */
    private Integer otherChange;
    
    /**
     * 后备转入
     */
    private Integer houbeiIn;
    
    /**
     * 其他转入
     */
    private Integer otherIn;
    
    /**
     * 日均存栏
     */
    private Integer dailyLivestockOnHand;
    
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