package io.terminus.doctor.web.front.warehouseV2.vo;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import lombok.Data;

import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/10/31.
 */
@Data
public class StockHandleVo extends DoctorWarehouseStockHandle {

    private String orgName;

    private String farmName;

    private Integer warehouseType;

    private String warehouseManagerName;

//    private String storageWarehouseName;

    private Double totalAmount;

    private Double totalQuantity;

    private List<Detail> details;

    @Data
    public static class Detail extends DoctorWarehouseMaterialHandle {

        private String materialCode;

        private String materialSpecification;

        private String applyPigBarnName;

        private Long applyPigBarnId;

        private String applyPigGroupName;

        private Long applyPigGroupId;

        private String applyStaffName;

        private Long applyStaffId;

        private String transferInFarmName;

        private Long transferInFarmId;

        private String transferInWarehouseName;

        private Long transferInWarehouseId;

        //可退数量
        private Double refundableQuantity;

        //配方入库仓库名称
        private String storageWarehouseName;

    }
}
