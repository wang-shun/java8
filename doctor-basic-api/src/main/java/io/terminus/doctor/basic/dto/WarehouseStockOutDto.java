package io.terminus.doctor.basic.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/20.
 */
@Data
public class WarehouseStockOutDto extends AbstractWarehouseStockDto implements Serializable {

    private static final long serialVersionUID = 9222667660305355019L;

    @Valid
    @NotEmpty(message = "stock.detail.empty")
    private List<WarehouseStockOutDetail> details;

    @Data
    public static class WarehouseStockOutDetail extends AbstractWarehouseStockDetail implements Serializable {

        private static final long serialVersionUID = -6161879769774585774L;

        @NotNull(message = "stock.apply.person.id.null")
        private Long applyStaffId;

        private String applyStaffName;

        @NotNull(message = "stock.apply.pig.house.id.null")
        private Long applyPigBarnId;

        @NotBlank(message = "stock.apply.pig.house.name.null")
        private String applyPigBarnName;

        private Long applyPigGroupId;

        private String applyPigGroupName;
    }
}
