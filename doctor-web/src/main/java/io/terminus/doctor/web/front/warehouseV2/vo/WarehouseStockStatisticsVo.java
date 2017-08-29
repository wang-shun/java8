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
    private long balanceAmount;

    private String unit;

    /**
     * 本月入库数量
     */
    private BigDecimal inQuantity;

    /**
     * 本月入库金额
     */
    private long inAmount;

    /**
     * 本月出库数量
     */
    private BigDecimal outQuantity;

    /**
     * 本月出库金额
     */
    private long outAmount;

    /**
     * 本月调拨出数量
     */
    private BigDecimal transferOutQuantity;

    /**
     * 本月调拨出金额
     */
    private long transferOutAmount;


    /**
     * 本月调拨入数量
     */
    private BigDecimal transferInQuantity;

    /**
     * 本月调拨入金额
     */
    private long transferInAmount;

}
