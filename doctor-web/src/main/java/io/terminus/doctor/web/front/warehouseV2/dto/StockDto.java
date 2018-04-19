package io.terminus.doctor.web.front.warehouseV2.dto;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author:Danny
 * @Description:
 * @Date:Create In 17:40 2018/4/11/011
 * @Modified By:
 */
@Getter
@Setter
public class StockDto {

    /**
     * 单据明细id
     */
    private Long id;

    /**
     * 猪场id
     */
    @NotNull(message = "stock.farmId.null")
    private Long farmId;

    /**
     * 仓库id
     */
    @NotNull(message = "stock.warehouseId.null")
    private Long warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 处理日期
     */
    @NotNull(message = "stock.handleDate.null")
    private Date handleDate;

    /**
     * 事件子类型
     * @see io.terminus.doctor.basic.enums.WarehouseMaterialHandleSubType
     */
    @NotNull(message = "stock.handleSubType.null")
    private Integer handleSubType;

    /**
     * 事件类型
     * @see io.terminus.doctor.basic.enums.WarehouseMaterialHandleType
     */
    @NotNull(message = "stock.handleType.null")
    private Integer handleType;

    /**
     * 流水号
     */
    private String serialNo;

    /**
     * 仓库类型
     */
    @NotNull(message = "stock.warehouseType.null")
    private Integer warehouseType;

    /**
     * 物料供应商名称
     */
    @NotNull(message = "stock.vendorName.null")
    private String vendorName;

    /**
     * 物料编号
     */
    @NotNull(message = "stock.materialId.null")
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 单价
     */
    @NotNull(message = "stock.unitPrice.null")
    private BigDecimal unitPrice;

    /**
     * 单位
     */
    @NotNull(message = "stock.unit.null")
    private String unit;

    /**
     * 数量
     */
    @NotNull(message = "stock.quantity.null")
    private BigDecimal quantity;

    /**
     * 创建人id
     */
    @NotNull(message = "stock.operatorId.null")
    private Long operatorId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 账面数量(之前库存数量)
     */
    private BigDecimal beforeStockQuantity;

    /**
     * 领用猪舍,领料出库用到
     */
    private Long pigBarnId;

    /**
     * 领用猪舍名称,领料出库用到
     */
    private String pigBarnName;

    /**
     * 领用猪群,领料出库用到
     */
    private Long pigGroupId;

    /**
     * 领用猪群名称,领料出库用到
     */
    private String pigGroupName;

    /**
     * 饲养员,领料出库用到
     */
    private String applyStaffName;

    /**
     * 物料类型,领料出库用到
     */
    private Integer materialType;

    /**
     * 领用类型,领料出库用到
     */
    private Integer applyType;

    /**
     * 调入猪场id,调拨入库用到
     */
    private Long dbFarmId;

    /**
     * 调入仓库id,调拨入库用到
     */
    private Long dbWarehouseId;

    /**
     * 调入仓库名称,调拨入库用到
     */
    private String dbWarehouseName;

    /**
     * 调入仓库类型,调拨入库用到
     */
    private Integer dbWarehouseType;

    /**
     * 单据主表id
     */
    private Long stockHandleId;

    /**
     * 另一条调拨物料处理单的id
     */
    private Long otherTransferHandleId;

}
