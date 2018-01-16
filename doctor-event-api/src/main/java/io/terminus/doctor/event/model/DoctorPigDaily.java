package io.terminus.doctor.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-12 17:01:12
 * Created by [ your name ]
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorPigDaily implements Serializable {

    private static final long serialVersionUID = 3770317171409388490L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 日期（yyyy-MM-dd）
     */
    private Date sumAt;
    
    /**
     * 所属公司id
     */
    private Long orgId;
    
    /**
     * 所属公司名
     */
    private String orgName;
    
    /**
     * 猪场
     */
    private Long farmId;
    
    /**
     * 猪场名
     */
    private String farmName;
    
    /**
     * 配怀母猪期初头数
     */
    private Integer sowPhStart;
    
    /**
     * 后备转入
     */
    private Integer sowPhReserveIn;
    
    /**
     * 配怀母猪断奶转入
     */
    private Integer sowPhWeanIn;
    
    /**
     * 配怀母猪进场
     */
    private Integer sowPhEntryIn;
    
    /**
     * 转场转入
     */
    private Integer sowPhChgFarmIn;
    
    /**
     * 配怀死亡母猪
     */
    private Integer sowPhDead;
    
    /**
     * 配怀淘汰母猪
     */
    private Integer sowPhWeedOut;
    
    /**
     * 配怀母猪销售
     */
    private Integer sowPhSale;
    
    /**
     * 配怀母猪转场
     */
    private Integer sowPhChgFarm;
    
    /**
     * 配怀母猪其他减少
     */
    private Integer sowPhOtherOut;
    
    /**
     * 配后备
     */
    private Integer mateHb;
    
    /**
     * 配断奶
     */
    private Integer mateDn;
    
    /**
     * 配返情
     */
    private Integer mateFq;
    
    /**
     * 配流产
     */
    private Integer mateLc;
    
    /**
     * 配阴性
     */
    private Integer mateYx;
    
    /**
     * 配种数
     */
    private Integer matingCount;

    /**
     * 配怀舍配种母猪数
     */
    private Integer sowPhMating;

    /**
     * 配怀空怀母猪数
     */
    private Integer sowPhKonghuai;

    /**
     * 配怀怀孕母猪数
     */
    private Integer sowPhPregnant;

    /**
     * 妊娠检查阳性头数
     */
    private Integer pregPositive;
    
    /**
     * 妊娠检查阴性头数
     */
    private Integer pregNegative;
    
    /**
     * 妊娠检查返情头数
     */
    private Integer pregFanqing;
    
    /**
     * 妊娠检查流产头数
     */
    private Integer pregLiuchan;

    /**
     * 断奶母猪7日配种数
     */
    private Integer weanMate;

    /**
     * 断奶母猪7日死淘数
     */
    private Integer weanDeadWeedOut;
    
    /**
     * 配怀母猪期末头数
     */
    private Integer sowPhEnd;
    
    /**
     * 产房母猪期初存栏
     */
    private Integer sowCfStart;
    
    /**
     * 产房母猪期末存栏
     */
    private Integer sowCfEnd;
    
    /**
     * 产房母猪，从配怀转入
     */
    private Integer sowCfIn;
    
    /**
     * 产房母猪其他转入 &#x3D; 转场转入的数量
     */
    private Integer sowCfInFarmIn;
    
    /**
     * 产房母猪死亡数量
     */
    private Integer sowCfDead;
    
    /**
     * 产房母猪淘汰数量
     */
    private Integer sowCfWeedOut;
    
    /**
     * 产房母猪销售数量
     */
    private Integer sowCfSale;
    
    /**
     * 产房母猪转场数量
     */
    private Integer sowCfChgFarm;

    /**
     * 产房母猪其他减少数量
     */
    private Integer sowCfOtherOut;

    /**
     * 前期配种数量
     */
    private Integer earlyMating;

    /**
     * 产房前期分娩窝数
     */
    private Integer earlyFarrowNest;

    /**
     * 后期分娩窝数
     */
    private Integer laterNest;
    
    /**
     * 产房分娩窝数
     */
    private Integer farrowNest;
    
    /**
     * 产房产活仔数
     */
    private Integer farrowLive;
    
    /**
     * 产房产健仔数
     */
    private Integer farrowHealth;
    
    /**
     * 产房产弱仔数
     */
    private Integer farrowWeak;
    
    /**
     * 产房死胎数
     */
    private Integer farrowDead;
    
    /**
     * 产房黑木畸数
     */
    private Integer farrowjmh;
    
    /**
     * 产房窝重之和
     */
    private Double farrowWeight;
    
    /**
     * 断奶窝数
     */
    private Integer weanNest;
    
    /**
     * 断奶合格数
     */
    private Integer weanQualifiedCount;
    
    /**
     * 断奶仔猪数
     */
    private Integer weanCount;
    
    /**
     * 每头猪的断奶日龄之和
     */
    private Integer weanDayAge;
    
    /**
     * 每一窝的断奶均重之和
     */
    private Double weanWeight;
    
    /**
     * 公猪期初存栏
     */
    private Integer boarStart;
    
    /**
     * 公猪转入数量
     */
    private Integer boarIn;
    
    /**
     * 公猪死亡数量
     */
    private Integer boarDead;
    
    /**
     * 公猪淘汰数量
     */
    private Integer boarWeedOut;
    
    /**
     * 公猪销售数量
     */
    private Integer boarSale;
    
    /**
     * 公猪其他减少
     */
    private Integer boarOtherOut;
    
    /**
     * 公猪期末存栏
     */
    private Integer boarEnd;
    
    /**
     * 进场未配种的母猪数
     */
    private Integer sowNotMatingCount;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}