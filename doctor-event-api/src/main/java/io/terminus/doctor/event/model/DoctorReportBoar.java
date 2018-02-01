package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 15:52:25
 * Created by [ your name ]
 */
@Data
public class DoctorReportBoar implements Serializable {

    private static final long serialVersionUID = -6312399196972221869L;

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
     * 组织名
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
     * 转入数量
     */
    private Integer turnInto;
    
    /**
     * 死亡数量
     */
    private String dead;
    
    /**
     * 淘汰数量
     */
    private String weedOut;
    
    /**
     * 销售数量
     */
    private String sale;
    
    /**
     * 其他减少
     */
    private Integer otherChange;
    
    /**
     * 日均存栏
     */
    private Integer dailyPigCount;
    
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