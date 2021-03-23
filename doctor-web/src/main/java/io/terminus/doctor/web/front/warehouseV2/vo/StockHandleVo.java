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

    private Long storageWarehouseId;

    private String storageWarehouseName;

    private Double totalAmount;

    private Double totalQuantity;

    private Integer status;

    private Integer hasInventory;

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

        //单位ID
        private String unitId;

        //入库仓库
        private Long storageWarehouseIds;

        private String storageWarehouseNames;

        //判断猪群是否关闭
        private Integer groupStatus;

        //得到该领料出库的退料入库的数量
        private Integer retreatingCount;

        //判断单据是否已盘点 （陈娟 2018-09-19）
        private Integer isInventory;

        //注解
        private String desc;
    }
}
