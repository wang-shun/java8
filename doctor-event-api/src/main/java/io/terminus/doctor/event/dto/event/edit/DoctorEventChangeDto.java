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
    private Long farmId;

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
     * 新胎次
     */
    private Integer pigParity;

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
    private Long matingBoarCode;

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
     * 新变动类型
     */
    private Long pigletsChangeType;

    /**
     * 新变动原因
     */
    private Long pigletsChangeReason;

    /**
     * 变动重量变化量
     */
    private Double pigletsWeightChange;

    /**
     * 变动价格变化量
     */
    private Long pigletsPriceChange;

    /**
     * 断奶数变化量
     */
    private Integer weanCountChange;

    /**
     * 猪重量变化量
     */
    private Double pigWeight;

    /**
     * 新疾病id
     */
    private Long diseaseId;
    private Long diseaseName;

    /**
     * 新防疫id;
     */
    private Long vaccinationId;
    private Long vaccinationName;

    /**
     * 猪群数量变化量
     */
    private Integer groupQuantityChange;

    /**
     * 猪群公猪变化量
     */
    private Integer groupBoarQtyChange;

    /**
     * 猪群母猪变化量
     */
    private Integer groupSowQtyChange;

    /**
     * 猪群断奶重变化量
     */
    private Integer groupWeanWeightChange;

    /**
     * 猪群初生重变化量
     */
    private Integer groupBirthWeightChange;

    /**
     * 猪群
     */
    private Integer groupQuantity;

    private Integer groupLiveQtyChange;

    private Integer groupHealthyQtyChange;

    private Integer groupWeakQtyChange;

    private Integer groupUnweanQtyChange;

    private Integer groupWeanQtyChange;

    private Integer groupQuaQtyChange;

    private Integer groupUnqQtyChange;

    /**
     * 新备注
     */
    private String remark;

}
