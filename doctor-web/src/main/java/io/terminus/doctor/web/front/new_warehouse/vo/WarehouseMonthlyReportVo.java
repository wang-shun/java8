package io.terminus.doctor.web.front.new_warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/21.
 */
@Data
public class WarehouseMonthlyReportVo {


    private String materialName;

    private String vendorName;

    private String unit;

    /**
     * 月初余量
     */
    private BigDecimal initialQuantity;

    /**
     * 月初余额
     */
    private Long initialAmount;

    /**
     * 月度入库数量
     */
    private BigDecimal inQuantity;

    /**
     * 月度入库金额
     */
    private Long inAmount;

    /**
     * 月度出库数量
     */
    private BigDecimal outQuantity;

    /**
     * 月度出库金额
     */
    private Long outAmount;

    /**
     * 月末余量
     */
    private BigDecimal balanceQuantity;

    /**
     * 月末余额
     */
    private Long balanceAmount;

}
