package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/22.
 */
@Data
public class FarmWarehouseVo {

    private Long id;

    private String name;

    private Integer type;

    private Long managerId;

    private String managerName;

    /**
     * 余额
     */
    private BigDecimal balanceQuantity = new BigDecimal(0);

    /**
     * 余量
     */
    private BigDecimal balanceAmount;

    /**
     * 本月入库数量
     */
    private BigDecimal inQuantity = new BigDecimal(0);

    /**
     * 本月入库金额
     */
    private BigDecimal inAmount;

    /**
     * 本月出库数量
     */
    private BigDecimal outQuantity = new BigDecimal(0);

    /**
     * 本月出库金额
     */
    private BigDecimal outAmount;

    /**
     * 本月调拨出数量
     */
    private BigDecimal transferOutQuantity = new BigDecimal(0);

    /**
     * 本月调拨出金额
     */
    private BigDecimal transferOutAmount;


    /**
     * 本月调拨入数量
     */
    private BigDecimal transferInQuantity = new BigDecimal(0);

    /**
     * 本月调拨入金额
     */
    private BigDecimal transferInAmount;


}
