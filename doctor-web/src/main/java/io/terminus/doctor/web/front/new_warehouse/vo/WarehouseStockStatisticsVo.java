package io.terminus.doctor.web.front.new_warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/22.
 */
@Data
public class WarehouseStockStatisticsVo {


    private Long id;

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

}
