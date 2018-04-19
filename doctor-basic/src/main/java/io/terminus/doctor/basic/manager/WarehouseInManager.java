package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

        //获取该笔明细之前的库存量
        BigDecimal historyQuantity = getHistoryQuantity(stockHandle.getHandleDate(), wareHouse.getId(), detail.getMaterialId());

        DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(detail, stockDto, stockHandle, wareHouse);
        materialHandle.setType(WarehouseMaterialHandleType.IN.getValue());
        materialHandle.setBeforeStockQuantity(historyQuantity);
        materialHandle.setUnitPrice(detail.getUnitPrice());
        doctorWarehouseMaterialHandleDao.create(materialHandle);

        historyQuantity = historyQuantity.add(detail.getQuantity());

        //入库类型，当天第一笔
        if (!DateUtil.inSameDate(stockHandle.getHandleDate(), new Date())) {
            //需要重算每个明细的beforeStockQuantity
            recalculate(stockHandle.getHandleDate(), wareHouse.getId(), historyQuantity);
        }
    }


    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle, Date handleDate) {

    }
}
