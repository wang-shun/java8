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

    private BigDecimal initialQuantity;

    private Long initialMoney;

    private BigDecimal inQuantity;

    private Long inMoney;

    private BigDecimal outQuantity;

    private Long outMoney;

    private BigDecimal balanceQuantity;

    private Long balanceMoney;

}
