package io.terminus.doctor.basic.model.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * 领用
 * Date: 2018-05-17 10:46:41
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseMaterialApply implements Serializable {

    private static final long serialVersionUID = -8920485322020278197L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 物料处理编号
     */
    private Long materialHandleId;

    /**
     * 公司ID
     */
    private Long orgId;

    /**
     * 猪厂编号
     */
    private Long farmId;

    /**
     * 仓库编号
     */
    private Long warehouseId;

    /**
     * 仓库类型
     * @see io.terminus.doctor.common.enums.WareHouseType
     */
    private Integer warehouseType;

    /**
     * 仓库名
     */
    private String warehouseName;

    /**
     * 领用猪舍编号
     */
    private Long pigBarnId;

    /**
     * 领用猪舍名称
     */
    private String pigBarnName;

    /**
     * 猪舍类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;

    /**
     * 领用猪群编号
     */
    private Long pigGroupId;

    /**
     * 领用猪群名称
     */
    private String pigGroupName;

    /**
     * 物料编号
     */
    private Long materialId;

    /**
     * 领用日期
     */
    private Date applyDate;

    /**
     * 领用人编号
     */
    private Long applyStaffId;

    /**
     * 领用人
     */
    private String applyStaffName;

    /**
     * 领用年
     */
    private Integer applyYear;

    /**
     * 领用月
     */
    private Integer applyMonth;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料类型，易耗品，原料，饲料，药品，饲料
     * @see io.terminus.doctor.common.enums.WareHouseType
     */
    private Integer type;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数量
     */
    private java.math.BigDecimal quantity;

    /**
     * 单价
     */
    private java.math.BigDecimal unitPrice;

    /**
     * 领用类型。0猪舍，1猪群，2母猪
     * @see io.terminus.doctor.basic.enums.WarehouseMaterialApplyType
     */
    private Integer applyType;

    /**
     * 金额
     */
    private java.math.BigDecimal amount;

    /**
     * 会计年月
     */
    private Date settlementDate;

    /**
     * 退料数量
     */
    private java.math.BigDecimal refundQuantity;

    /**
     * 退料金额
     */
    private java.math.BigDecimal refundAmount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}