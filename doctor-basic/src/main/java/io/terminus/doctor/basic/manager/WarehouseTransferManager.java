package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import org.springframework.stereotype.Component;

/**
 * 调拨
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseTransferManager extends AbstractStockManager {


    @Override
    public void create(AbstractWarehouseStockDetail detail, AbstractWarehouseStockDto stockDto, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {

    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {

    }
}
