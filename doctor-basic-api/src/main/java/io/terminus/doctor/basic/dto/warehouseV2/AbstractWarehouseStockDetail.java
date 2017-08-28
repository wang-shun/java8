package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/20.
 */
@Data
public abstract class AbstractWarehouseStockDetail {

    @NotNull(message = "stock.material.id.null")
    private Long materialId;

    @NotNull(message = "stock.quantity.null")
    @DecimalMin(inclusive = false, value = "0", message = "stock.quantity.small.then.zero")
    private BigDecimal quantity;

}
