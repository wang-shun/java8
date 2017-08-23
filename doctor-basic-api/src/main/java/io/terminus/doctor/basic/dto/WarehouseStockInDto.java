package io.terminus.doctor.basic.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/20.
 */
@Data
public class WarehouseStockInDto extends AbstractWarehouseStockDto implements Serializable {


    private static final long serialVersionUID = -8604195523049758038L;


    @Valid
    @NotEmpty(message = "stock.detail.empty")
    private List<WarehouseStockInDetailDto> details;

    @Data
    public static class WarehouseStockInDetailDto extends AbstractWarehouseStockDetail implements Serializable {

        private static final long serialVersionUID = 8853214651739196333L;

        private String vendorName;

        @NotBlank(message = "stock.unit.null")
        private String unit;

        @NotNull(message = "stock.unit.price.null")
        private Long unitPrice;

    }
}
