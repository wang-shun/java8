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

//    /**
//     * 公猪平均存栏
//     */
//    private Integer avgBoarLiveStock;

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
     * 窝均活仔数
     */
    private Double avgFarrowLive;

    /**
     * 窝均健仔数
     */
    private Double avgFarrowHealth;

    /**
     * 窝均弱仔数
     */
    private Double avgFarrowWeak;

    /**
     * 初生均个重
     */
    private Double avgFarrowWeight;

   /**
     * 出生总重
     */
    private Double farrowWeight;

    /**
     * 估算配种分娩率
     */
    private Double mateEstimateFarrowingRate;

    /**
     * 实际配种分娩率
     */
    private Double mateRealFarrowingRate;

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
     * 断奶均重(个体)
     */
    private Double weanAvgWeight;

    /**
     * 断奶日龄(个体)
     */
    private Double weanDayAge;

    /**
     * 种母猪死淘率
     */
    private Double deadSowRate;

    /**
     * 种公猪死淘率
     */
    private Double deadBoarRate;

    /**
     * 产房成活率
     */
    private Double liveFarrowRate;

    /**
     * 保育成活率
     */
    private Double liveNurseryRate;

    /**
     * 育肥成活率
     */
    private Double liveFattenRate;

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

    /**
     * 妊娠期
     */
    private Double pregDate;

    /**
     * 哺乳期
     */
    private Double feedDate;

    /**
     * 年产胎次
     */
    private Double yearFarrowParity;

}
