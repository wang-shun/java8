package io.terminus.doctor.event.dto.report.common;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/6/8.
 * 集团报表
 */
@Data
public class DoctorCliqueReportDto implements Serializable{
    private static final long serialVersionUID = -1992589291884077647L;

    /**
     * 猪场名称(横向报表可用)
     */
    private String farmName;

    /**
     * 月份(纵向报表可用)
     */
    private String month;

    /**
     * 母猪平均存栏
     */
    private Integer avgSowLiveStock;

    /**
     * 公猪平均存栏
     */
    private Integer avgBoarLiveStock;

    /**
     * 配种总数
     */
    private Integer mateCount;

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
     * 断奶七天配种率
     */
    private Double mateInSeven;

    /**
     * 估算受胎率
     */
    private Double mateEstimatePregRate;

    /**
     * 实际受胎率
     */
    private Double mateRealPregRate;

    /**
     * 妊娠检查总数
     */
    private Integer pregCount;

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
     * 产畸木黑数
     */
    private Integer farrowJmh;

   /**
     * 出生总重
     */
    private Double farrowWeight;

    /**
     * 断奶窝数
     */
    private Integer weanNest;

    /**
     * 断奶仔猪数
     */
    private Integer weanCount;

    /**
     * 窝均断奶数
     */
    private Double nestAvgWean;

    /**
     * 断奶均重
     */
    private Double weanAvgWeight;

    /**
     * 断奶日龄
     */
    private Double weanDayAge;

    /**
     * 产房死淘率
     */
    private Double deadFarrowRate;

    /**
     * 保育死淘率
     */
    private Double deadNurseryRate;

    /**
     * 育肥死淘率
     */
    private Double deadFattenRate;

    /**
     * 后备销售
     */
    private Integer hpSale;

    /**
     * 母猪销售
     */
    private Integer sowSale;

    /**
     * 公猪销售
     */
    private Integer boarSale;

    /**
     * 产房销售
     */
    private Integer cfSale;

    /**
     * 育肥销售
     */
    private Integer yfSale;

    /**
     * 非生产天数
     */
    private Double npd;

    /**
     * psy
     */
    private Double psy;


}
