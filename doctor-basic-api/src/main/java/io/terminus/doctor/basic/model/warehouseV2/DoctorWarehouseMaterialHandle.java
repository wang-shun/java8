package io.terminus.doctor.basic.model.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-19 13:53:42
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseMaterialHandle implements Serializable {

    private static final long serialVersionUID = 7613561419164229786L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 库存处理ID
     */
    private Long stockHandleId;

    /**
     * 公司id
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
     *
     * @see io.terminus.doctor.common.enums.WareHouseType
     */
    private Integer warehouseType;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 关联明细单据id
     */
    private Long relMaterialHandleId;

    /**
     * 物料供应商名称
     */
    private String vendorName;

    /**
     * 物料编号
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 处理类别
     *
     * @see io.terminus.doctor.basic.enums.WarehouseMaterialHandleType
     */
    private Integer type;

    /**
     * 单价，单位分
     */
    private Long unitPrice;

    /**
     * 单位
     */
    private String unit;

    /**
     * 删除标志
     *
     * @see io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag
     */
    private Integer deleteFlag;

    /**
     * 之前库存数量
     */
    private java.math.BigDecimal beforeStockQuantity;

    /**
     * 数量
     */
    private java.math.BigDecimal quantity;

    /**
     * 处理日期
     */
    private Date handleDate;

    /**
     * 处理年
     */
    private Integer handleYear;

    /**
     * 处理月
     */
    private Integer handleMonth;

    /**
     * 操作人编号
     */
    private Long operatorId;

    /**
     * 操作人名
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 会计年月
     */
    private Date settlementDate;

    /**
     * 金额
     */
    private java.math.BigDecimal amount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}