package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseInManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/4/24.
 */
@Service
public class WarehouseInStockService extends AbstractWarehouseStockService<WarehouseStockInDto, WarehouseStockInDto.WarehouseStockInDetailDto> {

    @Autowired
    private WarehouseInManager warehouseInManager;

    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        return WarehouseMaterialHandleType.IN;
    }

    @Override
    protected List<WarehouseStockInDto.WarehouseStockInDetailDto> getDetails(WarehouseStockInDto stockDto) {
        return stockDto.getDetails();
    }

    @Override
    protected void create(WarehouseStockInDto stockDto,
                          WarehouseStockInDto.WarehouseStockInDetailDto detail,
                          DoctorWarehouseStockHandle stockHandle,
                          DoctorWareHouse wareHouse) {
        warehouseInManager.create(detail, stockDto, stockHandle, wareHouse);

        doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), wareHouse);
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {
        warehouseInManager.delete(materialHandle);

        DoctorWareHouse wareHouse = new DoctorWareHouse();
        wareHouse.setId(materialHandle.getWarehouseId());
        wareHouse.setWareHouseName(materialHandle.getWarehouseName());
        wareHouse.setFarmId(materialHandle.getFarmId());
        wareHouse.setType(materialHandle.getWarehouseType());
        doctorWarehouseStockManager.out(materialHandle.getMaterialId(), materialHandle.getQuantity(), wareHouse);
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle, WarehouseStockInDto.WarehouseStockInDetailDto detail, DoctorWarehouseStockHandle stockHandle, WarehouseStockInDto stockDto, DoctorWareHouse wareHouse) {

        materialHandle.setRemark(detail.getRemark());
        materialHandle.setSettlementDate(stockDto.getSettlementDate());
        materialHandle.setUnitPrice(detail.getUnitPrice());

        boolean changeHandleDate = !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
        boolean changeQuantity = detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0;

        if (changeQuantity || changeHandleDate) {

            //更改了数量，或更改了操作日期
            if (changeQuantity) {
                BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getQuantity());
                if (changedQuantity.compareTo(new BigDecimal(0)) > 0) {
                    doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity, wareHouse);
                } else {
                    doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity.negate(), wareHouse);
                }
                materialHandle.setQuantity(detail.getQuantity());
//                warehouseInManager.resetUnitPrice(materialHandle, detail.getQuantity());
            }
            Date recalculateDate = materialHandle.getHandleDate();
            if (changeHandleDate) {
                warehouseInManager.buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());
                if (stockDto.getHandleDate().getTime().before(stockHandle.getHandleDate())) {//事件日期改小了，重算日期采用新的日期
                    recalculateDate = materialHandle.getHandleDate();
                }
            }
            doctorWarehouseMaterialHandleDao.update(materialHandle);
            warehouseInManager.recalculate(materialHandle, recalculateDate);

        } else {
            //只更新了备注
            doctorWarehouseMaterialHandleDao.update(materialHandle);
        }
    }
}
