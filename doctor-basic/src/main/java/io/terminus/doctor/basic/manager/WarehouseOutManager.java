package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * 领料出库
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseOutManager extends AbstractStockManager {


    @Override
    public void create(AbstractWarehouseStockDetail detail,
                       AbstractWarehouseStockDto stockDto,
                       DoctorWarehouseStockHandle stockHandle,
                       DoctorWareHouse wareHouse) {
        if (!DateUtil.inSameDate(stockHandle.getHandleDate(), new Date())) {
            //重算每个单据明细的beforeStockQuantity，并验证每个
            recalculate(stockHandle.getHandleDate(), wareHouse.getId(), detail.getMaterialId(), detail.getQuantity().negate());
        }

        DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(detail, stockDto, stockHandle, wareHouse);
        materialHandle.setType(WarehouseMaterialHandleType.OUT.getValue());

        //出库类型，当天最后一笔
        if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {
            materialHandle.setHandleDate(this.buildNewHandleDate(WarehouseMaterialHandleType.OUT, stockDto.getHandleDate()));
            doctorWarehouseMaterialHandleDao.create(materialHandle);
        }
    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {

    }
}
