package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/8/20.
 */
@Data
public class AbstractWarehouseStockDto {

    @NotNull(message = "farm.id.null")
    private Long farmId;

    @NotNull(message = "warehouse.stock.handle.date.null")
    private Date handleDate;

    @NotNull(message = "warehouse.id.null")
    private Long warehouseId;

    @NotNull(message = "warehouse.stock.operator.id.null")
    private Long operatorId;

    private String operatorName;
}
