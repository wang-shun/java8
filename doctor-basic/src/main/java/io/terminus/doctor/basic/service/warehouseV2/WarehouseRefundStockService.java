package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockRefundDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseReturnManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
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
        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, getMaterialHandleType(), null);
        warehouseReturnManager.create(stockDto.getDetails(), stockDto, stockHandle, wareHouse);

        stockDto.getDetails().forEach(detail -> {
            doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), wareHouse);
        });

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

        materialHandle.setRemark(detail.getRemark());

        DoctorWarehouseMaterialHandle outMaterialHandle = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());

        if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0
                || !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime())) {

            //更改了数量，或更改了操作日期

            if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0) {
                //可退数量
                BigDecimal alreadyRefundQuantity = doctorWarehouseMaterialHandleDao.countQuantityAlreadyRefund(materialHandle.getRelMaterialHandleId());
                if (outMaterialHandle.getQuantity().subtract(alreadyRefundQuantity).compareTo(detail.getQuantity()) < 0)
                    throw new ServiceException("");

                BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getQuantity());
                if (changedQuantity.compareTo(new BigDecimal(0)) > 0) {
                    doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity, wareHouse);
                } else {
                    doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity.negate(), wareHouse);
                }
                materialHandle.setQuantity(detail.getQuantity());
            }

            Date recalculateDate = materialHandle.getHandleDate();
            int days = DateUtil.getDeltaDays(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
            if (days != 0) {
                if (DateUtil.getDeltaDays(outMaterialHandle.getHandleDate(), stockDto.getHandleDate().getTime()) < 0)
                    throw new ServiceException("");

                warehouseReturnManager.buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());

                if (days < 0) {//事件日期改小了，重算日期采用新的日期
                    recalculateDate = materialHandle.getHandleDate();
                }
            }
            doctorWarehouseMaterialHandleDao.update(materialHandle);
            warehouseReturnManager.recalculate(materialHandle, recalculateDate);

        } else {
            //只更新了备注
            doctorWarehouseMaterialHandleDao.update(materialHandle);
        }
    }
}
