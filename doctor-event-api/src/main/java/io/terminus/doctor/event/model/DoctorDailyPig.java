package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Code generated by terminus code gen
 * Desc: 猪数量每天记录表Model类
 * Date: 2017-04-17
 */
@Data
public class DoctorDailyPig implements Serializable {
    private static final long serialVersionUID = -5703859467397171561L;

    private Long id;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 日期
     */
    private Date sumAt;

    /**
     * 配怀母猪头数
     */
    private Integer sowPh;

    /**
     * 产房母猪头数
     */
    private Integer sowCf;

    /**
     * 母猪期初头数
     */
    private Integer sowStart;

    /**
     * 母猪转入
     */
    private Integer sowIn;

    /**
     * 死亡母猪
     */
    private Integer sowDead;

    /**
     * 淘汰母猪
     */
    private Integer sowWeedOut;

    /**
     * 母猪销售
     */
    private Integer sowSale;

    /**
     * 母猪其他减少
     */
    private Integer sowOtherOut;

    /**
     * 母猪期末头数
     */
    private Integer sowEnd;

    /**
     * 公猪期初头数
     */
    private Integer boarStart;

    /**
     * 公猪转入
     */
    private Integer boarIn;

    /**
     * 死亡母猪
     */
    private Integer boarDead;

    /**
     * 淘汰母猪
     */
    private Integer boarWeedOut;

    /**
     * 母猪销售
     */
    private Integer boarSale;

    /**
     * 母猪其他减少
     */
    private Integer boarOtherOut;

    /**
     * 公猪期末头数
     */
    private Integer boarEnd;

    /**
     * 配后备头数
     */
    private Integer mateHb;

    /**
     * 配断奶头数
     */
    private Integer mateDn;

    /**
     * 配返情头数
     */
    private Integer mateFq;

    /**
     * 配流产头数
     */
    private Integer mateLc;

    /**
     * 配阴性头数
     */
    private Integer mateYx;

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
     * 分娩窝数
     */
    private Integer farrowNest;

    /**
     * 总产仔数
     */
    private Integer farrowAll;

    /**
     * 产活仔数
     */
    private Integer farrowLive;

    /**
     * 产健仔数
     */
    private Integer farrowHealth;

    /**
     * 产弱仔数
     */
    private Integer farrowWeak;

    /**
     * 产死胎数
     */
    private Integer farrowDead;

    /**
     * 产畸形仔数
     */
    private Integer farrowJx;

    /**
     * 产木乃伊仔数
     */
    private Integer farrowMny;

    /**
     * 产黑胎仔数
     */
    private Integer farrowBlack;

    /**
     * 产死畸木黑数
     */
    private Integer farrowSjmh;

    /**
     * 出生总重
     */
    private Double farrowWeight;

    /**
     * 出生均重
     */
    private Double farrowAvgWeight;

    /**
     * 断奶窝数
     */
    private Integer weanNest;

    /**
     * 断奶仔猪数
     */
    private Integer weanCount;

    /**
     * 断奶均重
     */
    private Double weanAvgWeight;

    /**
     * 断奶日龄
     */
    private Double weanDayAge;
}
