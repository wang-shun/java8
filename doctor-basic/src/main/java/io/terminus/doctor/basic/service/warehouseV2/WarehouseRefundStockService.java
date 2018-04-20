package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockOutDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockRefundDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseReturnManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/4/20.
 */
@Component
public class WarehouseRefundStockService extends AbstractWarehouseStockService<WarehouseStockRefundDto, WarehouseStockRefundDto.WarehouseStockRefundDetailDto> {

    @Autowired
    private WarehouseReturnManager warehouseReturnManager;


    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        return WarehouseMaterialHandleType.RETURN;
    }

    @Override
    protected List<WarehouseStockRefundDto.WarehouseStockRefundDetailDto> getDetails(WarehouseStockRefundDto stockDto) {
        return stockDto.getDetails();
    }


    @Override
    protected DoctorWarehouseStockHandle create(WarehouseStockRefundDto stockDto, DoctorWareHouse wareHouse) {
        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, getMaterialHandleType());
        warehouseReturnManager.create(stockDto.getDetails(), stockDto, stockHandle, wareHouse);
        return stockHandle;
    }

    @Override
    protected void create(WarehouseStockRefundDto stockDto, WarehouseStockRefundDto.WarehouseStockRefundDetailDto detail, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {
        //忽略
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {
        warehouseReturnManager.delete(materialHandle);
        DoctorWareHouse wareHouse = new DoctorWareHouse();
        wareHouse.setId(materialHandle.getWarehouseId());
        wareHouse.setWareHouseName(materialHandle.getMaterialName());
        doctorWarehouseStockManager.out(materialHandle.getMaterialId(), materialHandle.getQuantity(), wareHouse);
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle, WarehouseStockRefundDto.WarehouseStockRefundDetailDto detail, DoctorWarehouseStockHandle stockHandle, WarehouseStockRefundDto stockDto, DoctorWareHouse wareHouse) {



    }
}
