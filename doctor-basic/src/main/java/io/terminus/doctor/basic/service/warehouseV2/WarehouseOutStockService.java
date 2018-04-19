package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockOutDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseOutManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/4/19.
 */
@Service
public class WarehouseOutStockService extends AbstractWarehouseStockService<WarehouseStockOutDto, WarehouseStockOutDto.WarehouseStockOutDetail> {

    @Autowired
    private WarehouseOutManager warehouseOutManager;

    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        return WarehouseMaterialHandleType.IN;
    }

    @Override
    protected List<WarehouseStockOutDto.WarehouseStockOutDetail> getDetails(WarehouseStockOutDto stockDto) {
        return stockDto.getDetails();
    }

    @Override
    protected void create(WarehouseStockOutDto stockDto,
                          WarehouseStockOutDto.WarehouseStockOutDetail detail,
                          DoctorWarehouseStockHandle stockHandle,
                          DoctorWareHouse wareHouse) {

        warehouseOutManager.create(detail, stockDto, stockHandle, wareHouse);
    }
}
