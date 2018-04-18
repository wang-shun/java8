package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 采购入库
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseInManager extends AbstractStockManager<WarehouseStockInDto.WarehouseStockInDetailDto> {


    @Override
    public void create(WarehouseStockInDto.WarehouseStockInDetailDto detail,
                       AbstractWarehouseStockDto stockDto,
                       DoctorWarehouseStockHandle stockHandle,
                       DoctorWareHouse wareHouse) {


        if (!DateUtil.inSameDate(stockHandle.getHandleDate(), new Date())) {
            //需要重算每个明细的beforeStockQuantity
            recalculate(stockHandle.getHandleDate(), wareHouse.getId(), detail.getQuantity());
        }


    }


    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle, Date handleDate) {

    }
}
