package io.terminus.doctor.basic.dto.warehouseV2;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/27.
 */
@Data
public class WarehouseFormulaDto extends AbstractWarehouseStockDto implements Serializable {

    private static final long serialVersionUID = 7140313825441452732L;

    private Long feedMaterialId;

//    private DoctorBasicMaterial feedMaterial;

    private DoctorWarehouseSku feedMaterial;

    private BigDecimal feedMaterialQuantity;

    private String feedUnit;

    private String farmName;

    private List<WarehouseFormulaDetail> details;

    @Data
    public static class WarehouseFormulaDetail extends AbstractWarehouseStockDetail implements Serializable {

        private static final long serialVersionUID = 8852999063915768802L;

        private String materialName;    //  原料名称

        private Double percent; //原料配比信息

    }
}
