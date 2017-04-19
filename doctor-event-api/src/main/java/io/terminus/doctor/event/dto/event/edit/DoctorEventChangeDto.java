package io.terminus.doctor.event.dto.event.edit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Date;

/**
 * Created by xjn on 17/4/13.
 * 记录编辑的变化(没有变化时,字段为null)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorEventChangeDto {
    /**
     * 当前猪场id
     */
    private Long farmId;

    /**
     * 目标id
     */
    private Long businessId;

    /**
     * 新事件时间
     */
    private Date newEventAt;

    /**
     * 旧事件时间
     */
    private Date oldEventAt;

    /**
     * 新进场来源
     */
    private Integer source;

    /**
     * 新胎次
     */
    private Integer pigParity;

    /**
     * 猪初生重变化量
     */
    private Double birthWeightChange;

    /**
     * 新品种id
     */
    private Long pigBreedId;
    private Long pigBreedName;

    /**
     * 新品系id
     */
    private Long pigBreedTypeId;
    private Long pigBreedTypeName;

    /**
     * 新猪舍id
     */
    private Long pigBarnId;
    private Long pigBarnName;

    /**
     * 公猪进场类型
     */
    private Integer boarType;

    /**
     * 新配种公猪id
     */
    private Long matingBoarId;
    private String matingBoarCode;
    /**
     * 新的操作人
     */
    private Long newOperatorId;
    private String newOperatorName;
    /**
     * 新配种类型
     * @see io.terminus.doctor.event.enums.MatingType
     */
    private Integer mateType;

    /**
     * 新妊娠检查结果
     * @see io.terminus.doctor.event.enums.PregCheckResult
     */
    private Integer pregCheckResult;

    /**
     * 分娩总重(kg)变化量
     */
    private Double farrowWeightChange;

    /**
     * 活仔数变化量
     */
    private Integer liveCountChange;

    /**
     * 键仔数变化量
     */
    private Integer healthCountChange;

    /**
     * 弱仔数变化量
     */
    private Integer weakCountChange;

    /**
     * 木乃伊数变化量
     */
    private Integer mnyCountChange;

    /**
     * 畸形数变化量
     */
    private Integer jxCountChange;

    /**
     * 死胎数变化量
     */
    private Integer deadCountChange;

    /**
     * 黑胎数变化量
     */
    private Integer blackCountChange;

    /**
     * 仔猪数量变化量
     */
    private Integer pigletsCountChange;

    /**
     * 旧变动类型
     */
    private Long oldChangeTypeId;

    /**
     * 新变动类型,猪群
     */
    private Long changeTypeId;

    /**
     * 数量变化
     */
    private Integer quantityChange;

    /**
     * 变动重量变化量
     */
    private Double weightChange;

    /**
     * 超出重量变化
     */
    private Double overWeightChange;

    /**
     * 变动价格变化量
     */
    private Long priceChange;

    /**
     * 超出价格变化
     */
    private Long overPriceChange;

    /**
     * 新疾病id
     */
    private Long diseaseId;

    /**
     * 新防疫id;
     */
    private Long vaccinationId;

    /**
     * 猪断奶数变化量
     */
    private Integer weanCountChange;

    /**
     * 猪断奶均重变化量
     */
    private Double weanAvgWeightChange;

    /**
     * 猪群初生重变化量
     */
    private Double groupBirthWeightChange;

    /**
     * 猪群公猪变化量
     */
    private Integer groupBoarQtyChange;

    /**
     * 猪群母猪变化量
     */
    private Integer groupSowQtyChange;

    private Integer groupLiveQtyChange;

    private Integer groupHealthyQtyChange;

    private Integer groupWeakQtyChange;

    private Integer groupUnweanQtyChange;

    private Integer groupQuaQtyChange;

    private Integer groupUnqQtyChange;

    /**
     * 旧事件转种猪, 进场pigCode
     */
    private String oldPigCode;

    /**
     * 转种猪, 进场pigCode
     */
    private String pigCode;

    /**
     * 转种猪, 进场母亲耳缺号
     */
    private String motherEarCode;

    /**
     * 转种猪, 进场耳缺号
     */
    private String earCode;

    /**
     * 转种猪, 进场出生日期
     */
    private String birthDate;

    /**
     * 转入猪舍
     */
    private Long toBarnId;
    private String toBarnName;

    /**
     * 均重变化
     */
    private double avgWeightChange;


    /**
     * 旧事件转场的猪场id
     */
    private Long oldToFarmId;

    /**
     * 旧事件转场的猪舍
     */
    private Long oldToGroupId;

    /**
     * 转场的猪场id
     */
    private Long toFarmId;

    /**
     * 转场的猪群id
     */
    private Long toGroupId;

    /**
     * 新备注
     */
    private String remark;

}
