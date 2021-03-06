package io.terminus.doctor.web.front.warehouseV2.vo;

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

    private String code;

    private String specification;

    /**
     * 月初余量
     */
    private BigDecimal initialQuantity;

    /**
     * 月初余额
     */
    private BigDecimal initialAmount;

    /**
     * 月度入库数量
     */
    private BigDecimal inQuantity;

    /**
     * 月度入库金额
     */
    private BigDecimal inAmount;

    /**
     * 月度出库数量
     */
    private BigDecimal outQuantity;

    /**
     * 月度出库金额
     */
    private BigDecimal outAmount;

    /**
     * 月末余量
     */
    private BigDecimal balanceQuantity;

    /**
     * 月末余额
     */
    private BigDecimal balanceAmount;

}
