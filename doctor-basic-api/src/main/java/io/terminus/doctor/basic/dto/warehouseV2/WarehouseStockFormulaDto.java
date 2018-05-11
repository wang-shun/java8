package io.terminus.doctor.basic.dto.warehouseV2;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/4/21.
 */
public class WarehouseStockFormulaDto extends AbstractWarehouseStockDto implements Serializable {

    private static final long serialVersionUID = -7130721769914380700L;

    @Valid
    @NotEmpty(message = "stock.detail.empty", groups = AbstractWarehouseStockDetail.StockDefaultValid.class)
    private List<WarehouseStockFormulaDetail> details;

    public static class WarehouseStockFormulaDetail extends AbstractWarehouseStockDetail implements Serializable {

        private static final long serialVersionUID = 4662817408999322033L;
    }
}

