package io.terminus.doctor.event.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 * 猪群相关报表（组织维度：猪场-猪群类型PigType，时间维度：日）
 */
public class DoctorGroupDaily implements Serializable {
    private static final long serialVersionUID = -395777802984439446L;

    private Long id;

    /**
     * 公司id
     */
    private Long orgId;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 猪群类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer type;

    /**
     * 日期（yyyy-MM-dd）
     */
    private Date sumAt;

    /**
     * 期初
     */
    private Integer start;

    /**
     * 转入数量，转入此类型猪群的数量（在指定期间内转入的数量，包括：外购、保育及育肥转后备的、转场转入的）
     */
    private Integer turnInto;

    /**
     * 转场转入数量
     */
    private Integer chgFarmIn;

    /**
     * 转入总重
     */
    private Double turnIntoWeight;

    /**
     * 转场
     */
    private Integer chgFarm;

    /**
     * 转场总重
     */
    private Double chgFarmWeight;

    /**
     * 销售
     */
    private Integer sale;

    /**
     * 销售总重
     */
    private Double saleWeight;

    /**
     * 死亡
     */
    private Integer dead;

    /**
     * 淘汰
     */
    private Integer weedOut;

    /**
     * 其他变动减少
     */
    private Integer otherChange;

    /**
     * 转保育
     */
    private Integer toNursery;

    /**
     * 转育肥
     */
    private Integer toFatten;

    /**
     * 转育肥总重
     */
    private Double toFattenWeight;

    /**
     * 转后备
     */
    private Integer toHoubei;

    /**
     * 转后备总重
     */
    private Double toHoubeiWeight;

    /**
     * 转种猪
     */
    private Integer turnSeed;

    /**
     * 转出总重
     */
    private Double turnOutWeight;

    /**
     * 原料消耗数量
     */
    private Double material;

    /**
     * 原料消耗金额
     */
    private Long materialAmount;

    /**
     * 饲料消耗数量
     */
    private Double feed;

    /**
     * 饲料消耗金额
     */
    private Long feedAmount;

    /**
     * 药品消耗金额
     */
    private Long medicineAmount;

    /**
     * 疫苗消耗金额
     */
    private Long vaccinationAmount;

    /**
     * 易耗品消耗金额
     */
    private Long consumableAmount;

    /**
     * 期末
     */
    private Integer end;

    private Date updatedAt;
    private Date createdAt;

    /**
     * 版本号
     */
    private Integer version;
}
