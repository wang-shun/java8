package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 盘点
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseInventoryManager extends AbstractStockManager {

    @Override
    public DoctorWarehouseMaterialHandle create(AbstractWarehouseStockDetail detail,
                                                AbstractWarehouseStockDto stockDto,
                                                DoctorWarehouseStockHandle stockHandle,
                                                DoctorWareHouse wareHouse) {

        DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(detail, stockDto, stockHandle, wareHouse);


        boolean profit = stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
        if (profit) {
            materialHandle.setType(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
        } else {
            materialHandle.setType(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
        }


        if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {
            //历史明细单

            materialHandle.setHandleDate(this.buildNewHandleDate(stockDto.getHandleDate()).getTime());

            BigDecimal historyQuantity = getHistoryQuantityInclude(stockDto.getHandleDate().getTime(), wareHouse.getId(), detail.getMaterialId());

            materialHandle.setBeforeStockQuantity(historyQuantity);
            if (profit)
                historyQuantity.add(materialHandle.getQuantity());
            else
                historyQuantity.subtract(materialHandle.getQuantity());

            recalculate(stockDto.getHandleDate().getTime(), false, wareHouse.getId(), materialHandle.getMaterialId(), historyQuantity);
        } else {
            BigDecimal currentQuantity = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(detail.getMaterialId(), wareHouse.getId())
                    .orElse(DoctorWarehouseStock.builder().quantity(new BigDecimal(0)).build())
                    .getQuantity();

            materialHandle.setBeforeStockQuantity(currentQuantity);
        }

        doctorWarehouseMaterialHandleDao.create(materialHandle);
        return materialHandle;
    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {
        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(materialHandle);

        if (!DateUtil.inSameDate(materialHandle.getHandleDate(), new Date())) {
            recalculate(materialHandle);
        }
    }
}
