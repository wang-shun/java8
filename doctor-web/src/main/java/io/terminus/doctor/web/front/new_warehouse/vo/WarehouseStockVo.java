package io.terminus.doctor.web.front.new_warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/22.
 */
@Data
public class WarehouseStockVo {

    private String materialName;

    private BigDecimal balanceQuantity;

    private long balanceMoney;

    private String unit;

    private BigDecimal inQuantity;

    private long inMoney;

    private BigDecimal outQuantity;

    private long outMoney;

}
