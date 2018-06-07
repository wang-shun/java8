package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/20.
 */
@Data
public class WarehouseStockInDto extends AbstractWarehouseStockDto implements Serializable {


    private static final long serialVersionUID = -8604195523049758038L;

    @Valid
    @NotEmpty(message = "stock.detail.empty", groups = AbstractWarehouseStockDetail.StockDefaultValid.class)
    private List<WarehouseStockInDetailDto> details;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class WarehouseStockInDetailDto extends AbstractWarehouseStockDetail implements Serializable {

        private static final long serialVersionUID = 8853214651739196333L;

//        private String vendorName;

//        @NotBlank(message = "stock.unit.null")
//        private String unit;

        @NotNull(message = "stock.unit.price.null", groups = StockDefaultValid.class)
        private BigDecimal unitPrice;


        @NotNull(message = "stock.amount.null", groups = StockDefaultValid.class)
        @DecimalMin(inclusive = false, value = "0", message = "stock.amount.small.then.zero", groups = StockDefaultValid.class)
        private BigDecimal amount;

//        @NotNull(message = "stock.quantity.null")
//        @DecimalMin(inclusive = false, value = "0", message = "stock.quantity.small.then.zero")
//        private BigDecimal quantity;


//        private String specification;

//        private String materialCode;

    }
}
