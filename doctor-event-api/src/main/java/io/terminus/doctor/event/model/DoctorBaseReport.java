package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:12 2017/4/20
 */
@Data
public class DoctorBaseReport implements Serializable{

    private static final long serialVersionUID = 8428231403969882763L;

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

    /**
     * 产房仔猪饲料消耗数量
     */
    private Double farrowFeed;

    /**
     * 产房仔猪饲料消耗金额
     */
    private Long farrowFeedAmount;

    /**
     * 产房仔猪药品消耗金额
     */
    private Long farrowMedicineAmount;

    /**
     * 产房仔猪疫苗消耗金额
     */
    private Long farrowVaccinationAmount;

    /**
     * 产房仔猪易耗品消耗金额
     */
    private Long farrowConsumableAmount;

    /**
     * 保育猪饲料消耗数量
     */
    private Double nurseryFeed;

    /**
     * 保育猪饲料消耗金额
     */
    private Long nurseryFeedAmount;

    /**
     * 保育猪药品消耗金额
     */
    private Long nurseryMedicineAmount;

    /**
     * 保育猪疫苗消耗金额
     */
    private Long nurseryVaccinationAmount;

    /**
     * 保育猪易耗品消耗金额
     */
    private Long nurseryConsumableAmount;

    /**
     * 育肥猪饲料消耗数量
     */
    private Double fattenFeed;

    /**
     * 育肥猪饲料消耗金额
     */
    private Long fattenFeedAmount;

    /**
     * 育肥猪药品消耗金额
     */
    private Long fattenMedicineAmount;

    /**
     * 育肥猪疫苗消耗金额
     */
    private Long fattenVaccinationAmount;

    /**
     * 育肥猪易耗品消耗金额
     */
    private Long fattenConsumableAmount;

    /**
     * 后备猪饲料消耗数量
     */
    private Double houbeiFeed;

    /**
     * 后备猪饲料消耗金额
     */
    private Long houbeiFeedAmount;

    /**
     * 后备猪药品消耗金额
     */
    private Long houbeiMedicineAmount;

    /**
     * 后备猪疫苗消耗金额
     */
    private Long houbeiVaccinationAmount;

    /**
     * 后备猪易耗品消耗金额
     */
    private Long houbeiConsumableAmount;
}
