package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockOutDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseOutManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        return WarehouseMaterialHandleType.OUT;
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
        doctorWarehouseStockManager.out(detail.getMaterialId(), detail.getQuantity(), wareHouse);
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {
        warehouseOutManager.delete(materialHandle);
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle,
                           WarehouseStockOutDto.WarehouseStockOutDetail detail,
                           DoctorWarehouseStockHandle stockHandle,
                           WarehouseStockOutDto stockDto,
                           DoctorWareHouse wareHouse) {

        materialHandle.setRemark(detail.getRemark());

        if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0
                || !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime())) {

            //更改了数量，或更改了操作日期
            if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0) {
                BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getQuantity());
                if (changedQuantity.compareTo(new BigDecimal(0)) > 0) {
                    doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity, wareHouse);
                } else {
                    doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity, wareHouse);
                }
            }

            if (!DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime())) {
                materialHandle.setHandleDate(warehouseOutManager.buildNewHandleDate(WarehouseMaterialHandleType.OUT, stockDto.getHandleDate()));
                doctorWarehouseMaterialHandleDao.update(materialHandle);
            }
            warehouseOutManager.recalculate(materialHandle);

        } else {
            //只更新了备注
            doctorWarehouseMaterialHandleDao.update(materialHandle);
        }

    }

}
