package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 退料入库
 * Created by sunbo@terminus.io on 2018/4/20.
 */
@Data
public class WarehouseStockRefundDto extends AbstractWarehouseStockDto implements Serializable {

    private static final long serialVersionUID = 1123295408988897660L;

    private Long outStockHandleId;

    @Valid
    @NotEmpty(message = "stock.detail.empty", groups = AbstractWarehouseStockDetail.StockDefaultValid.class)
    private List<WarehouseStockRefundDto.WarehouseStockRefundDetailDto> details;


    @Data
    public static class WarehouseStockRefundDetailDto extends AbstractWarehouseStockDetail implements Serializable {

        private static final long serialVersionUID = 7670362280319755039L;


        /**
         * 领用猪舍
         */
        private Long applyBarnId;

        /**
         * 领用猪群
         */
        private Long applyGroupId;
    }
}
