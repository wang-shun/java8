package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/22.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseStockStatisticsVo {


    private Long id;


    private Long farmId;

    private Long warehouseId;

    private String warehouseName;

    private Integer warehouseType;

    private Long materialId;

    private String materialName;

    /**
     * 余量
     */
    private BigDecimal balanceQuantity;

    /**
     * 余额
     */
    private BigDecimal balanceAmount;

    private String unit;

    private String vendorName;

    private String code;

    private String specification;

    /**
     * 入库数量
     */
    private BigDecimal inQuantity;

    /**
     * 入库金额
     */
    private BigDecimal inAmount;

    /**
     * 出库数量
     */
    private BigDecimal outQuantity;

    /**
     * 出库金额
     */
    private BigDecimal outAmount;

    /**
     * 调拨出数量
     */
    private BigDecimal transferOutQuantity;

    /**
     * 调拨出金额
     */
    private BigDecimal transferOutAmount;


    /**
     * 调拨入数量
     */
    private BigDecimal transferInQuantity;

    /**
     * 调拨入金额
     */
    private BigDecimal transferInAmount;

    /**
     * 盘亏金额
     */
    private BigDecimal inventoryDeficitAmount;

    /**
     * 盘亏数量
     */
    private BigDecimal inventoryDeficitQuantity;

    /**
     * 盘盈金额
     */
    private BigDecimal inventoryProfitAmount;

    /**
     * 盘盈数量
     */
    private BigDecimal inventoryProfitQuantity;

}
