package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/20.
 */
@Data
public abstract class AbstractWarehouseStockDetail {

    private Long materialHandleId;

    @NotNull(message = "stock.material.id.null", groups = StockDefaultValid.class)
    private Long materialId;

    @NotNull(message = "stock.quantity.null", groups = StockDefaultValid.class)
    @DecimalMin.List({
            @DecimalMin(value = "0", inclusive = true, message = "stock.quantity.small.then.zero", groups = StockInventoryValid.class),
            @DecimalMin(value = "0", inclusive = false, message = "stock.quantity.small.then.zero", groups = StockOtherValid.class)
    })
    private BigDecimal quantity;

    private String remark;


    public static interface StockDefaultValid {

    }

    public static interface StockInventoryValid extends StockDefaultValid {
    }

    public static interface StockOtherValid extends StockDefaultValid {
    }

}
