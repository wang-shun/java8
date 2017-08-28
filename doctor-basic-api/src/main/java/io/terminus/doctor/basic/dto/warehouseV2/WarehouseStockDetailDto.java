package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by sunbo@terminus.io on 2017/8/10.
 */
@Data
@Deprecated
public class WarehouseStockDetailDto implements Serializable {


    private static final long serialVersionUID = -7778747928304442698L;

    @NotNull(message = "material.id.null", groups = WarehouseStockDto.DefaultWarehouseValid.class)
    private Long materialID;

//    @NotBlank(message = "material.name.blank", groups = WarehouseStockDto.DefaultWarehouseValid.class)
//    private String materialName;

    //    @NotNull(message = "vendor.id.null", groups = WarehouseStockDto.DefaultWarehouseValid.class)
    private Long vendorID;

    @NotBlank(message = "unit.blank", groups = WarehouseStockDto.InWarehouseValid.class)
    private String unit;//单位

    @NotNull(message = "handler.unit.price.null", groups = WarehouseStockDto.InWarehouseValid.class)
    private Long unitPrice;//单价

    @NotNull(message = "handler.number.null", groups = WarehouseStockDto.DefaultWarehouseValid.class)
    @DecimalMin(inclusive = false, value = "0", message = "stock.number.bigger.then.zero")
    private BigDecimal number;//盘点为盘点数量，入库为入库数量，出库为出库数量

    @NotNull(message = "pig.id.null", groups = WarehouseStockDto.OutWarehouseValid.class)
    private Long pigID;

    @NotNull(message = "recipient.id.null", groups = WarehouseStockDto.OutWarehouseValid.class)
    private Long recipientID;


    @NotNull(message = "target.warehouseV2.id.null", groups = WarehouseStockDto.TransferWarehouseValid.class)
    private Long targetWarehouseID;


}
