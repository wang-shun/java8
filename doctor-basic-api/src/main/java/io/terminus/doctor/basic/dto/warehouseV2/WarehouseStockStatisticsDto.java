package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

/**
 * Created by sunbo@terminus.io on 2017/8/26.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseStockStatisticsDto {


    /**
     * 入库金额和数量
     */
    private AmountAndQuantityDto in;

    /**
     * 出库金额和数量
     */
    private AmountAndQuantityDto out;


    /**
     * 盘盈金额和数量
     */
    private AmountAndQuantityDto inventoryProfit;


    /**
     * 盘亏金额和数量
     */
    private AmountAndQuantityDto inventoryDeficit;

    /**
     * 调拨入金额和数量
     */
    private AmountAndQuantityDto transferIn;

    /**
     * 调拨出金额和数量
     */
    private AmountAndQuantityDto transferOut;

}
