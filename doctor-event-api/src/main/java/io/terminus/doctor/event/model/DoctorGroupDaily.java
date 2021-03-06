package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 * 猪群相关报表（组织维度：猪场-猪群类型PigType，时间维度：日）
 */
@Data
public class DoctorGroupDaily implements Serializable {
    private static final long serialVersionUID = -395777802984439446L;

    private Long id;
    /**
     * 所属集团id
     */
    private Long groupId;

    private String groupName;
    /**
     * 所属公司id
     */
    private Long orgId;

    private String orgName;

    /**
     * 猪场id
     */
    private Long farmId;

    private String farmName;

    /**
     * 猪群类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;

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
     * 转入总重
     */
    private Double turnIntoWeight;

    /**
     * 转场转入总重
     */
    private Double chgFarmInWeight;

    /**
     * 转入总日龄
     */
    private Integer turnIntoAge;

    /**
     * 转场转入总日龄
     */
    private Integer chgFarmInAge;

    /**
     * 转场转入数量
     */
    private Integer chgFarmIn;

    /**
     * 产房手动操作转入猪群数量
     */
    private Integer deliverHandTurnInto;

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
     * 转保育总重
     */
    private Double toNurseryWeight;

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
     * 转出总重(死淘、销售、转入育肥等所有减少的重量)
     */
    private Double turnOutWeight;

    /**
     * 结束猪群转出实际数量（用于计算保育和育肥转出均重）
     */
    private Integer turnActualCount;

    /**
     * 结束猪群转出实际总重量（用于计算保育70日龄和育肥180日龄转出均重）
     */
    private Double turnActualWeight;

    /**
     * 结束猪群转出实际总日龄
     */
    private Integer turnActualAge;

    /**
     * 结束猪群净增重（用于计算保育和育肥料肉比）
     */
    private Double netWeightGain;

    /**
     * 产房转出总日龄
     */
    private Integer deliverTurnOutAge;

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
