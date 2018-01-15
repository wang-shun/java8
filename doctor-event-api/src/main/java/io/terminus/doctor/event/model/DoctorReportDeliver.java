package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 15:54:34
 * Created by [ your name ]
 */
@Data
public class DoctorReportDeliver implements Serializable {

    private static final long serialVersionUID = 666762652165525445L;

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
     * 期末存栏
     */
    private Integer end;
    
    /**
     * 配怀转入
     */
    private Integer sowCfIn;
    
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
     * 断奶转出
     */
    private String sowPhWeanOut;
    
    /**
     * 其他减少
     */
    private Integer otherChange;
    
    /**
     * 前期配种数量
     */
    private Integer earlyMating;
    
    /**
     * 前期分娩窝数
     */
    private Integer earlyNest;
    
    /**
     * 前推分娩率
     */
    private Double earlyNestRate;
    
    /**
     * 后期分娩窝数
     */
    private Integer laterNest;
    
    /**
     * 后推分娩率
     */
    private Double laterNestRate;
    
    /**
     * 分娩窝数
     */
    private String farrowNest;

    /**
     * 产仔总数
     */
    private Integer farrowAll;

    /**
     * 产活仔数
     */
    private Integer farrowLiving;
    
    /**
     * 产健仔数
     */
    private Integer farrowHealth;
    
    /**
     * 产弱仔数
     */
    private Integer farrowWeak;
    
    /**
     * 死胎数
     */
    private Integer farrowDead;
    
    /**
     * 黑木畸数
     */
    private Integer farrowJmh;
    
    /**
     * 窝均产仔数
     */
    private Double pigletCountPerFarrow;
    
    /**
     * 窝均活仔数
     */
    private Double pigletLivingCountPerFarrow;
    
    /**
     * 窝均健仔数
     */
    private Double pigletHealthCountPerFarrow;
    
    /**
     * 窝均弱仔数
     */
    private Double pigletWeakCountPerFarrow;
    
    /**
     * 平均窝重
     */
    private Double avgWeightPerFarrow;
    
    /**
     * 初生重
     */
    private Double firstBornWeight;
    
    /**
     * 期初存栏（仔猪）
     */
    private Integer pigletStart;
    
    /**
     * 其他转入（仔猪）
     */
    private Integer pigletOtherIn;
    
    /**
     * 转场数量（仔猪）
     */
    private Integer pigletChgFarmOut;
    
    /**
     * 转场均重（仔猪）
     */
    private Double pigletChgFarmOutAvgWeight;
    
    /**
     * 转保育数
     */
    private String toNursery;
    
    /**
     * 转保育数均重
     */
    private Double toNurseryAvgWeight;
    
    /**
     * 死亡数量（仔猪）
     */
    private Integer pigletDead;
    
    /**
     * 淘汰数量（仔猪）
     */
    private Integer pigletWeedOut;
    
    /**
     * 其他减少（仔猪）
     */
    private Integer pigletOtherChange;
    
    /**
     * 死淘率（仔猪）
     */
    private Double pigletDeadWeedOutRate;
    
    /**
     * 成活率（仔猪）
     */
    private Double pigletLivingRate;
    
    /**
     * 断奶窝数
     */
    private String weanNest;
    
    /**
     * 断奶仔猪数
     */
    private Integer weanCount;
    
    /**
     * 断奶合格数
     */
    private Integer weanQualifiedCount;
    
    /**
     * 窝均断奶数
     */
    private Double weanCountPerFarrow;
    
    /**
     * 断奶日龄
     */
    private Integer weanDayAge;
    
    /**
     * 断奶均重
     */
    private Double weanWeightPerFarrow;
    
    /**
     * 产房转出均重
     */
    private Double turnOutAvgWeight;
    
    /**
     * 产房转出日龄
     */
    private Integer turnOutDay;
    
    /**
     * 产房转出均重（28日龄（4周龄重））
     */
    private Double turnOutAvgWeight28;
    
    /**
     * 销售数量（仔猪）
     */
    private String pigletSale;
    
    /**
     * 销售均量（仔猪）
     */
    private Double pigletSaleAveWeight;
    
    /**
     * 期末存栏（仔猪）
     */
    private Integer pigletEnd;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}