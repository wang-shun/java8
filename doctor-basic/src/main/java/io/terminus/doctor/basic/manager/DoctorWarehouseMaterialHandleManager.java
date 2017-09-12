package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorWarehouseHandleDetailDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseHandleDetail;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/9/12.
 */
@Component
public class DoctorWarehouseMaterialHandleManager {


    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehouseHandleDetailDao doctorWarehouseHandleDetailDao;


    /**
     * 入库
     */
    @Transactional
    public void in(MaterialHandleContext materialHandleContext) {
        handle(materialHandleContext, WarehouseMaterialHandleType.IN);
    }

    @Transactional
    public DoctorWarehouseMaterialHandle out(MaterialHandleContext materialHandleContext) {
        return handle(materialHandleContext, WarehouseMaterialHandleType.OUT);
    }

    @Transactional
    public DoctorWarehouseMaterialHandle handle(MaterialHandleContext materialHandleContext, WarehouseMaterialHandleType type) {

        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setFarmId(materialHandleContext.getStock().getFarmId());
        materialHandle.setWarehouseId(materialHandleContext.getStock().getWarehouseId());
        materialHandle.setWarehouseName(materialHandleContext.getStock().getWarehouseName());
        materialHandle.setWarehouseType(materialHandleContext.getStock().getWarehouseType());
        materialHandle.setMaterialId(materialHandleContext.getStock().getMaterialId());
        materialHandle.setMaterialName(materialHandleContext.getStock().getMaterialName());
        materialHandle.setUnitPrice(materialHandleContext.getUnitPrice());
        materialHandle.setType(type.getValue());
        materialHandle.setQuantity(materialHandleContext.getQuantity());
        materialHandle.setHandleDate(materialHandleContext.getStockDto().getHandleDate().getTime());
        materialHandle.setOperatorId(materialHandleContext.getStockDto().getOperatorId());
        materialHandle.setOperatorName(materialHandleContext.getStockDto().getOperatorName());
        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
        materialHandle.setUnit(materialHandleContext.getStock().getUnit());
        materialHandle.setVendorName(materialHandleContext.getVendorName());
        materialHandle.setUnit(materialHandleContext.getStock().getUnit());
        materialHandle.setHandleYear(materialHandleContext.getStockDto().getHandleDate().get(Calendar.YEAR));
        materialHandle.setHandleMonth(materialHandleContext.getStockDto().getHandleDate().get(Calendar.MONTH) + 1);

        doctorWarehouseMaterialHandleDao.create(materialHandle);

        for (DoctorWarehousePurchase purchase : materialHandleContext.getPurchases().keySet()) {
            doctorWarehouseHandleDetailDao.create(DoctorWarehouseHandleDetail.builder()
                    .materialHandleId(materialHandle.getId())
                    .materialPurchaseId(purchase.getId())
                    .quantity(materialHandleContext.getPurchases().get(purchase))
                    .handleYear(materialHandleContext.getStockDto().getHandleDate().get(Calendar.YEAR))
                    .handleMonth(materialHandleContext.getStockDto().getHandleDate().get(Calendar.MONTH) + 1)
                    .build());
        }

        return materialHandle;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaterialHandleContext {
        private AbstractWarehouseStockDto stockDto;
        private AbstractWarehouseStockDetail stockDetail;
        private DoctorWarehouseStock stock;
        private Map<DoctorWarehousePurchase, BigDecimal/*quantity*/> purchases;
        private long unitPrice;
        private String vendorName;
        private BigDecimal quantity;
    }
}
