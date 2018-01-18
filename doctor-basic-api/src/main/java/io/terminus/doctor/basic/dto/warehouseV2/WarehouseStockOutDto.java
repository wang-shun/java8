package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
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
public class WarehouseStockOutDto extends AbstractWarehouseStockDto implements Serializable {

    private static final long serialVersionUID = 9222667660305355019L;

    private Long orgId;

    @Valid
    @NotEmpty(message = "stock.detail.empty", groups = AbstractWarehouseStockDetail.StockDefaultValid.class)
    private List<WarehouseStockOutDetail> details;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WarehouseStockOutDetail extends AbstractWarehouseStockDetail implements Serializable {

        private static final long serialVersionUID = -6161879769774585774L;

//        @NotNull(message = "stock.quantity.null")
//        @DecimalMin(inclusive = false, value = "0", message = "stock.quantity.small.then.zero")
//        private BigDecimal quantity;

        //        @NotNull(message = "stock.unit.price.null")
//        private Long unitPrice;

        /**
         * 饲养员编号
         */
        private Long applyStaffId;

        /**
         * 饲养员名称
         */
        private String applyStaffName;

        /**
         * 领用猪舍
         */
        @NotNull(message = "stock.apply.pig.house.id.null", groups = StockDefaultValid.class)
        private Long applyPigBarnId;

        @NotBlank(message = "stock.apply.pig.house.name.null", groups = StockDefaultValid.class)
        private String applyPigBarnName;

        private Integer pigType;

        /**
         * 领用猪群
         */
        private Long applyPigGroupId;

        private String applyPigGroupName;

    }
}
