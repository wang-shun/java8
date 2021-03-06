package io.terminus.doctor.web.front.warehouseV2.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseStatisticsVo {


    private Long id;

    private BigDecimal balanceQuantity;

    private BigDecimal balanceAmount;

    private BigDecimal inQuantity;

    private BigDecimal inAmount;

    private BigDecimal outQuantity;

    private BigDecimal outAmount;
}
