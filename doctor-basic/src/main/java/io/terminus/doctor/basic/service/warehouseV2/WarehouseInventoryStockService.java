package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInventoryDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseInventoryManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sunbo@terminus.io on 2018/4/21.
 */
@Component
public class WarehouseInventoryStockService extends
        AbstractWarehouseStockService<WarehouseStockInventoryDto, WarehouseStockInventoryDto.WarehouseStockInventoryDetail> {

    @Autowired
    private WarehouseInventoryManager warehouseInventoryManager;

    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected DoctorWarehouseStockHandle create(WarehouseStockInventoryDto stockDto, DoctorWareHouse wareHouse) {

        //是否是历史单据
        boolean historyBill = !DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date());

        //盘盈明细单
        Map<WarehouseStockInventoryDto.WarehouseStockInventoryDetail, BigDecimal> profitInventory = new HashMap<>();
        //盘亏明细单
        Map<WarehouseStockInventoryDto.WarehouseStockInventoryDetail, BigDecimal> deficitInventory = new HashMap<>();

        stockDto.getDetails().forEach(detail -> {
            BigDecimal stockQuantity;
            if (historyBill) {
                stockQuantity = warehouseInventoryManager.getHistoryQuantityInclude(stockDto.getHandleDate().getTime(), stockDto.getWarehouseId(), detail.getMaterialId());
            } else
                stockQuantity = doctorWarehouseStockManager.getStock(wareHouse.getId(), detail.getMaterialId()).orElseGet(() -> {
                    DoctorWarehouseStock stock = new DoctorWarehouseStock();
                    stock.setQuantity(new BigDecimal(0));
                    return stock;
                }).getQuantity();

            int c = detail.getQuantity().compareTo(stockQuantity);
            if (c > 0)
                profitInventory.put(detail, detail.getQuantity().subtract(stockQuantity));
            else if (c < 0)
                deficitInventory.put(detail, stockQuantity.subtract(detail.getQuantity()));
        });

        if (profitInventory.isEmpty() && deficitInventory.isEmpty())
            throw new ServiceException("inventory.quantity.all.equals");

        DoctorWarehouseStockHandle profitInventoryStockHandle = null, deficitInventoryStockHandle = null;

        if (!profitInventory.isEmpty())
            profitInventoryStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.INVENTORY_PROFIT);
        if (!deficitInventory.isEmpty())
            deficitInventoryStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.INVENTORY_DEFICIT);

        for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail : profitInventory.keySet()) {
            detail.setQuantity(profitInventory.get(detail));
            warehouseInventoryManager.create(detail, stockDto, profitInventoryStockHandle, wareHouse);
            doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), wareHouse);
        }
        for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail : deficitInventory.keySet()) {
            detail.setQuantity(deficitInventory.get(detail));
            warehouseInventoryManager.create(detail, stockDto, deficitInventoryStockHandle, wareHouse);
            doctorWarehouseStockManager.out(detail.getMaterialId(), detail.getQuantity(), wareHouse);
        }

        if (null != profitInventoryStockHandle)
            return profitInventoryStockHandle;
        else return deficitInventoryStockHandle;
    }

    @Override
    protected List<WarehouseStockInventoryDto.WarehouseStockInventoryDetail> getDetails(WarehouseStockInventoryDto stockDto) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void create(WarehouseStockInventoryDto stockDto, WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {
        warehouseInventoryManager.delete(materialHandle);

        DoctorWareHouse wareHouse = new DoctorWareHouse();
        wareHouse.setId(materialHandle.getWarehouseId());
        wareHouse.setWareHouseName(materialHandle.getWarehouseName());
        doctorWarehouseStockManager.out(materialHandle.getMaterialId(), materialHandle.getQuantity(), wareHouse);
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle, WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail, DoctorWarehouseStockHandle stockHandle, WarehouseStockInventoryDto stockDto, DoctorWareHouse wareHouse) {

        materialHandle.setRemark(detail.getRemark());

        if (detail.getQuantity().compareTo(materialHandle.getBeforeStockQuantity()) != 0
                || !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime())) {

            //更改了数量，或更改了操作日期
            if (detail.getQuantity().compareTo(materialHandle.getBeforeStockQuantity()) != 0) {

                BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getBeforeStockQuantity());
                if (changedQuantity.compareTo(new BigDecimal(0)) < 0)
                    changedQuantity = changedQuantity.negate();

                if (changedQuantity.compareTo(materialHandle.getQuantity()) != 0) {

                }

                if (changedQuantity.compareTo(new BigDecimal(0)) > 0) {

                    doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity, wareHouse);
                } else {
                    doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity, wareHouse);
                }
                materialHandle.setQuantity(detail.getQuantity());
            }
            Date recalculateDate = materialHandle.getHandleDate();
            int days = DateUtil.getDeltaDays(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());
            if (days != 0) {
                materialHandle.setHandleDate(warehouseInventoryManager.buildNewHandleDateForUpdate(WarehouseMaterialHandleType.OUT, stockDto.getHandleDate()));
                doctorWarehouseMaterialHandleDao.update(materialHandle);
                if (days < 0) {//事件日期改小了，重算日期采用新的日期
                    recalculateDate = materialHandle.getHandleDate();
                }
            }
            warehouseInventoryManager.recalculate(materialHandle, recalculateDate);

        } else {
            //只更新了备注
            doctorWarehouseMaterialHandleDao.update(materialHandle);
        }
    }
}
