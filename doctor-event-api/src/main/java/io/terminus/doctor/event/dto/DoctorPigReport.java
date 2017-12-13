package io.terminus.doctor.event.dto;

import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/12/13.
 */
public class DoctorPigReport {


    /**
     * 日期（yyyy-MM-dd）
     */
    private Date sumAt;

    /**
     * 猪场
     */
    private Long farmId;

    /**
     * 母猪期初存栏
     */
    private Integer sowStart;


    /**
     * 母猪死亡数量
     */
    private Integer sowDead;


    /**
     * 母猪淘汰数量
     */
    private Integer sowWeedOut;

    /**
     * 基础母猪死淘率
     * （死亡数量+淘汰数量）/基础母猪平均存栏
     */
    private Integer basicSowDeadAndWeedOutRate;

    /**
     * 母猪销售数量
     */
    private Integer sowSale;

    /**
     * 母猪转场数量
     */
    private Integer sowChgFarm;

    /**
     * 母猪其他减少
     */
    private Integer sowOtherOut;


    /**
     * 母猪后备转入
     */
    private Integer sowPhReserveIn;


    /**
     * 母猪其他转入
     * 配怀转入+产房转入
     */
    private Integer sowOtherIn;


    /**
     * 母猪日均存栏
     * 存栏和除以天数
     */
    private Integer sowDailyQuantity;


    /**
     * 母猪期末存栏
     */
    private Integer sowEnd;


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
     * 产房母猪其他转入
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
     * 产房前期配种数量
     */
    private Integer earlyMating;

    /**
     * 产房前期分娩窝数
     * 向前推114天的配种数之合
     */
    private Integer earlyFarrowNest;

    /**
     * 产房前推分娩率
     * 当前分娩窝数/前期配种数量
     */
    private Integer earlyFarrowRate;

    /**
     * 产房后期分娩窝数
     * 向后推114天的分娩窝数之合
     */
    private Integer lateFarrowNest;

    /**
     * 产房后推分娩率
     * 后期分娩窝数/当前配种数
     */
    private Integer lateFarrowRate;


    /**
     * 产房分娩窝数
     */
    private Integer farrowNest;


    /**
     * 产房产仔总数
     */
    private Integer farrowAll;


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
    private Integer farrowSjmh;


    /**
     * 产房窝均产仔数
     * 产仔总数/分娩窝数
     */
    private Integer avgFarrow;

    /**
     * 产房窝均活仔数
     * 产活仔数/分娩窝数
     */
    private Integer avgFarrowLive;

    /**
     * 产房窝均健仔数
     * 产健仔数/分娩窝数
     */
    private Integer avgFarrowHealth;

    /**
     * 产房窝均弱仔数
     * 产健仔数/分娩窝数
     */
    private Integer avgFarrowWeak;


    /**
     * 产房窝重之和
     */
    private Integer weight;

    /**
     * 产房平均窝重
     * 窝重之和/窝数
     */
    private Integer avgWeight;

    /**
     * 产房初生重
     * 窝重之和/产活仔数
     */
    private Integer firstWeight;


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

    private Date updatedAt;
    private Date createdAt;

}
