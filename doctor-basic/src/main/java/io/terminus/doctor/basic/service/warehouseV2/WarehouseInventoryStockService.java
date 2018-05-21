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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2018/4/21.
 */
@Slf4j
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

        for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail : stockDto.getDetails()) {
            BigDecimal stockQuantity;
            if (historyBill) {
                Date newDate = warehouseInventoryManager.buildNewHandleDate(stockDto.getHandleDate()).getTime();
                stockQuantity = warehouseInventoryManager.getHistoryQuantityInclude(newDate, stockDto.getWarehouseId(), detail.getMaterialId());
            } else
                stockQuantity = doctorWarehouseStockManager.getStock(wareHouse.getId(), detail.getMaterialId()).orElseGet(() -> {
                    DoctorWarehouseStock stock = new DoctorWarehouseStock();
                    stock.setQuantity(new BigDecimal(0));
                    return stock;
                }).getQuantity();

            int c = detail.getQuantity().compareTo(stockQuantity);
            if (c > 0) {
                //盘盈单
                if (detail.getUnitPrice() == null)
                    throw new ServiceException("inventory.material.handle.unit.price.not.null");

                profitInventory.put(detail, detail.getQuantity().subtract(stockQuantity));
            } else if (c < 0)
                deficitInventory.put(detail, stockQuantity.subtract(detail.getQuantity()));
            else
                log.info("material[{}] at {} have same quantity with stock", detail.getMaterialId(), stockDto.getHandleDate(), detail.getQuantity());
        }

        if (profitInventory.isEmpty() && deficitInventory.isEmpty())
            throw new ServiceException("inventory.quantity.all.equals");

        DoctorWarehouseStockHandle profitInventoryStockHandle = null, deficitInventoryStockHandle = null;

        if (!profitInventory.isEmpty())
            profitInventoryStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.INVENTORY_PROFIT, null);
        if (!deficitInventory.isEmpty())
            deficitInventoryStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.INVENTORY_DEFICIT, null);

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
        return stockDto.getDetails();
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
    public void changed(Map<WarehouseStockInventoryDto.WarehouseStockInventoryDetail,
            DoctorWarehouseMaterialHandle> changed,
                        DoctorWarehouseStockHandle stockHandle,
                        WarehouseStockInventoryDto stockDto,
                        DoctorWareHouse wareHouse) {

        boolean needCreateStockHandle = false;//是否需要新增单据
        for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail k : changed.keySet()) {

            DoctorWarehouseMaterialHandle v = changed.get(k);

            BigDecimal inventoryQuantity;//之前盘点的数量
            if (v.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue()))
                inventoryQuantity = v.getBeforeStockQuantity().add(v.getQuantity());
            else
                inventoryQuantity = v.getBeforeStockQuantity().subtract(v.getQuantity());

            if (inventoryQuantity.compareTo(k.getQuantity()) != 0) {
                if ((v.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())
                        && k.getQuantity().compareTo(v.getBeforeStockQuantity()) < 0)
                        || (v.getType().equals(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue())
                        && k.getQuantity().compareTo(v.getBeforeStockQuantity()) > 0)) {
                    //盘盈改成盘亏或盘亏改成盘盈
                    needCreateStockHandle = true;
                }
            }
        }

        DoctorWarehouseStockHandle newStockHandle = null;
        if (needCreateStockHandle) {
            if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue()))
                newStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.INVENTORY_DEFICIT, null);
            else
                newStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.INVENTORY_PROFIT, null);
        }

        for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail : changed.keySet()) {

            DoctorWarehouseMaterialHandle materialHandle = changed.get(detail);

            materialHandle.setRemark(detail.getRemark());
            materialHandle.setSettlementDate(stockDto.getSettlementDate());

            boolean changeHandleDate = !DateUtil.inSameDate(stockHandle.getHandleDate(), stockDto.getHandleDate().getTime());

            BigDecimal beforeStockQuantity;
            if (changeHandleDate) {
                //如果更改了操作日期，那么该笔明细之前的库存量需要重新计算
                beforeStockQuantity = warehouseInventoryManager.getHistoryQuantityInclude(warehouseInventoryManager.buildNewHandleDate(stockDto.getHandleDate()).getTime(), materialHandle.getWarehouseId(), materialHandle.getMaterialId());
            } else
                beforeStockQuantity = materialHandle.getBeforeStockQuantity();

            log.info("before stock quantity is :{}", beforeStockQuantity);

            BigDecimal inventoryQuantity;//之前盘点的数量
            if (materialHandle.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue()))
                inventoryQuantity = beforeStockQuantity.add(materialHandle.getQuantity());
            else
                inventoryQuantity = beforeStockQuantity.subtract(materialHandle.getQuantity());


            if (detail.getQuantity().compareTo(inventoryQuantity) != 0
                    || changeHandleDate) {

                //库存100，盘亏，change 3，97；盘盈 5，105=+8
                //库存100，之前是盘亏3，现在是盘亏5=-2
                //100，之前是盘亏3，现在是盘亏1=+2
                //100，之前是盘盈3，现在是盘亏5=-8
                //100，之前是盘盈3，现在是盘盈5=+2
                //100，之前是盘盈3，现在是盘盈1=-2
                BigDecimal stockChangedQuantity = null;
                if (materialHandle.getType().equals(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue())
                        && detail.getQuantity().compareTo(beforeStockQuantity) > 0) {
                    //盘亏改盘盈
                    if (detail.getUnitPrice() == null)
                        throw new ServiceException("inventory.material.handle.unit.price.not.null");

                    materialHandle.setType(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
                    materialHandle.setStockHandleId(newStockHandle.getId());
                    materialHandle.setUnitPrice(detail.getUnitPrice());

                    stockChangedQuantity = materialHandle.getQuantity().add(detail.getQuantity().subtract(beforeStockQuantity));

                    materialHandle.setQuantity(detail.getQuantity().subtract(beforeStockQuantity));
                    log.info("change deficit to profit.new stock quantity is {}", detail.getQuantity());
                } else if (materialHandle.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())
                        && detail.getQuantity().compareTo(beforeStockQuantity) < 0) {
                    //盘盈改盘亏
                    materialHandle.setType(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
                    materialHandle.setStockHandleId(newStockHandle.getId());

                    stockChangedQuantity = materialHandle.getQuantity().add(beforeStockQuantity.subtract(detail.getQuantity()));
                    stockChangedQuantity = stockChangedQuantity.negate();

                    materialHandle.setQuantity(beforeStockQuantity.subtract(detail.getQuantity()));
                    log.info("change profit to deficit.new stock quantity is {}", detail.getQuantity());
                } else {
                    BigDecimal changedQuantity;
                    if (materialHandle.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())) {
                        //盘盈+10
                        changedQuantity = detail.getQuantity().subtract(beforeStockQuantity);
                        int c = materialHandle.getQuantity().compareTo(changedQuantity);
                        if (c > 0) {//原盘盈+11
                            //入库1
                            stockChangedQuantity = changedQuantity.subtract(materialHandle.getQuantity());
                            log.info("profit increase.now profit is {},original profit is {},stock change quantity is {}", changedQuantity, materialHandle.getQuantity(), stockChangedQuantity);
                        } else if (c < 0) {//盘盈+9
                            //出库1
                            stockChangedQuantity = changedQuantity.subtract(materialHandle.getQuantity());
                            log.info("profit decrease.now profit is {},original profit is {},stock change quantity is {}", changedQuantity, materialHandle.getQuantity(), stockChangedQuantity);
                        }
                    } else {
                        //盘亏10
                        changedQuantity = beforeStockQuantity.subtract(detail.getQuantity());
                        int c = materialHandle.getQuantity().compareTo(changedQuantity);
                        if (c > 0) {//原盘亏11
                            //入库1
                            stockChangedQuantity = materialHandle.getQuantity().subtract(changedQuantity);
                            log.info("deficit decrease.now deficit is {},original deficit is {},stock change quantity is {}", changedQuantity, materialHandle.getQuantity(), stockChangedQuantity);
                        } else if (c < 0) {
                            //出库1
                            stockChangedQuantity = changedQuantity.subtract(materialHandle.getQuantity());
                            log.info("deficit increase.now deficit is {},original deficit is {},stock change quantity is {}", changedQuantity, materialHandle.getQuantity(), stockChangedQuantity);
                        }
                    }
                    materialHandle.setQuantity(changedQuantity);
                }

                if (stockChangedQuantity.compareTo(new BigDecimal(0)) > 0)
                    doctorWarehouseStockManager.in(materialHandle.getMaterialId(), stockChangedQuantity, wareHouse);
                else
                    doctorWarehouseStockManager.out(materialHandle.getMaterialId(), stockChangedQuantity.negate(), wareHouse);


                Date recalculateDate = materialHandle.getHandleDate();
                //更改了操作日期
                if (changeHandleDate) {
                    warehouseInventoryManager.buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());
                    if (stockDto.getHandleDate().getTime().before(stockHandle.getHandleDate())) {//事件日期改小了，重算日期采用新的日期
                        recalculateDate = materialHandle.getHandleDate();
                    }
                }
                doctorWarehouseMaterialHandleDao.update(materialHandle);
                warehouseInventoryManager.recalculate(materialHandle, recalculateDate);

            } else {
                doctorWarehouseMaterialHandleDao.update(materialHandle);
            }

        }
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle, WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail, DoctorWarehouseStockHandle stockHandle, WarehouseStockInventoryDto stockDto, DoctorWareHouse wareHouse) {

        throw new UnsupportedOperationException();
    }
}
