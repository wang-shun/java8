package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseSkuDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseUnitOrg;
import io.terminus.doctor.common.exception.InvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/4/8.
 */
public abstract class AbstractStockManager<T extends AbstractWarehouseStockDetail> {

    @Autowired
    private LockRegistry lockRegistry;

    @Autowired
    protected DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    protected DoctorWarehouseSkuDao doctorWarehouseSkuDao;

    @Autowired
    protected DoctorBasicDao doctorBasicDao;

    /**
     * 重算
     *
     * @param materialHandle 该笔之后，包括该笔
     * @param newQuantity    修改后的数量
     */
    protected void recalculate(DoctorWarehouseMaterialHandle materialHandle, BigDecimal newQuantity) {

        if (materialHandle.getBeforeStockQuantity().compareTo(newQuantity) < 0)
            throw new ServiceException("warehouse.stock.not.enough");

        BigDecimal newStockQuantity = materialHandle.getBeforeStockQuantity().subtract(newQuantity);

        List<DoctorWarehouseMaterialHandle> needToRecalculate = getMaterialHandleAfter(materialHandle.getWarehouseId(), materialHandle.getId(), materialHandle.getHandleDate());
        for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : needToRecalculate) {
            if (newStockQuantity.compareTo(doctorWarehouseMaterialHandle.getQuantity()) < 0)
                throw new ServiceException("warehouse.stock.not.enough");

            doctorWarehouseMaterialHandle.setBeforeStockQuantity(newStockQuantity);
            newStockQuantity = newStockQuantity.subtract(doctorWarehouseMaterialHandle.getQuantity());
        }

        needToRecalculate.forEach(
                m -> {
                    doctorWarehouseMaterialHandleDao.update(m);
                }
        );
    }

    /**
     * 重算
     *
     * @param handleDate      入库类型，是handleDate+00:00:00；出库类型，是handleDate+23:59:59
     * @param historyQuantity 入库类型，是正数；出库类型，是负数
     */
    protected void recalculate(Date handleDate, Long warehouseId, BigDecimal historyQuantity) {

        List<DoctorWarehouseMaterialHandle> needToRecalculate = doctorWarehouseMaterialHandleDao.findAfter(warehouseId, handleDate);

        for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : needToRecalculate) {
            if (historyQuantity.compareTo(doctorWarehouseMaterialHandle.getQuantity()) < 0)
                throw new ServiceException("warehouse.stock.not.enough");

            doctorWarehouseMaterialHandle.setBeforeStockQuantity(historyQuantity);

            if (WarehouseMaterialHandleType.isBigIn(doctorWarehouseMaterialHandle.getType()))
                historyQuantity = historyQuantity.add(doctorWarehouseMaterialHandle.getQuantity());
            else
                historyQuantity = historyQuantity.subtract(doctorWarehouseMaterialHandle.getQuantity());
        }

        needToRecalculate.forEach(
                m -> {
                    doctorWarehouseMaterialHandleDao.update(m);
                }
        );
    }


    /**
     * 获取新增的单据明细
     *
     * @return
     */
    public List<T> getNew(List<DoctorWarehouseMaterialHandle> materialHandles, List<T> details) {

        List<T> needAdd = new ArrayList<>();
        details.forEach(d -> {
            boolean include = false;
            for (DoctorWarehouseMaterialHandle materialHandle : materialHandles) {
                if (materialHandle.getId().equals(d.getMaterialHandleId())) {
                    include = true;
                    break;
                }
            }
            if (!include)
                needAdd.add(d);
        });

        return needAdd;
    }

    public List<DoctorWarehouseMaterialHandle> getDelete(List<DoctorWarehouseMaterialHandle> materialHandles, List<T> details) {

        List<DoctorWarehouseMaterialHandle> needDelete = new ArrayList<>();
        materialHandles.forEach(m -> {
            boolean include = false;
            for (T detail : details) {
                if (m.getId().equals(detail.getMaterialHandleId())) {
                    include = true;
                    break;
                }
            }
            if (!include)
                needDelete.add(m);
        });

        return needDelete;
    }

    public Map<T, DoctorWarehouseMaterialHandle> getUpdate(List<DoctorWarehouseMaterialHandle> materialHandles, List<T> details) {

        Map<T, DoctorWarehouseMaterialHandle> needUpdate = new HashMap<>();

        Map<Long, List<DoctorWarehouseMaterialHandle>> materialHandleMap = materialHandles.stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getId));

        details.forEach(d -> {
            if (materialHandleMap.containsKey(d.getMaterialHandleId())) {
                DoctorWarehouseMaterialHandle materialHandle = materialHandleMap.get(d.getMaterialHandleId()).get(0);
                if (!d.getMaterialId().equals(materialHandle.getMaterialId())
                        || d.getQuantity().compareTo(materialHandle.getQuantity()) != 0
                        || !d.getRemark().equals(materialHandle.getRemark())) {
                    needUpdate.put(d, materialHandle);
                }
            }
        });

        return needUpdate;
    }


    /**
     * 获取某笔明细之后的明细，不包括该笔
     *
     * @param materialHandleId
     * @return 需要被重算的单据明细
     */
    protected List<DoctorWarehouseMaterialHandle> getMaterialHandleAfter(Long warehouseId, Long materialHandleId, Date handleDate) {

        return doctorWarehouseMaterialHandleDao.findAfter(warehouseId, materialHandleId, handleDate);
    }

    protected BigDecimal getHistoryQuantity(Date handleDate, Long warehouseId, Long skuId) {
        //如果是入库，handleDate当日第一笔
        //如果是出库，handleDate当日最后一笔
        return doctorWarehouseMaterialHandleDao.getHistoryStock(warehouseId, skuId, handleDate);
    }

//    @Deprecated
//    protected BigDecimal getHistoryQuantity(Date handleDate, Long warehouseId, Long materialHandleId) {
//
//        return doctorWarehouseMaterialHandleDao.getHistoryStock(materialHandleId, warehouseId, handleDate);
//    }


    public abstract void create(T detail,
                                AbstractWarehouseStockDto stockDto,
                                DoctorWarehouseStockHandle stockHandle,
                                DoctorWareHouse wareHouse);


    protected DoctorWarehouseMaterialHandle buildMaterialHandle(T detail,
                                                                AbstractWarehouseStockDto stockDto,
                                                                DoctorWarehouseStockHandle stockHandle,
                                                                DoctorWareHouse wareHouse) {
        DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(detail.getMaterialId());
        if (null == sku) {
            throw new InvalidException("warehouse.sku.not.found", detail.getMaterialId());
        }

        DoctorBasic unit = doctorBasicDao.findById(Long.parseLong(sku.getUnit()));

        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setStockHandleId(stockHandle.getId());
        materialHandle.setOrgId(stockDto.getOrgId());
        materialHandle.setFarmId(stockDto.getFarmId());
        materialHandle.setWarehouseId(stockDto.getWarehouseId());
        materialHandle.setWarehouseType(wareHouse.getType());
        materialHandle.setWarehouseName(wareHouse.getWareHouseName());
        materialHandle.setMaterialId(detail.getMaterialId());
        materialHandle.setMaterialName(sku.getName());
//        materialHandle.setType(WarehouseMaterialHandleType.OUT.getValue());
        materialHandle.setUnit(null == unit ? "" : unit.getName());
        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
//        materialHandle.setBeforeStockQuantity(getHistoryQuantity(stockHandle.getHandleDate(), wareHouse.getId()));
        materialHandle.setQuantity(detail.getQuantity());
        materialHandle.setSettlementDate(stockDto.getSettlementDate());
        materialHandle.setHandleDate(stockHandle.getHandleDate());
        materialHandle.setHandleYear(stockDto.getHandleDate().get(Calendar.YEAR));
        materialHandle.setHandleMonth(stockDto.getHandleDate().get(Calendar.MONTH) + 1);
        materialHandle.setOperatorId(stockDto.getOperatorId());
        materialHandle.setOperatorName(stockDto.getOperatorName());
        materialHandle.setRemark(detail.getRemark());

        return materialHandle;
    }

    public abstract void delete(DoctorWarehouseMaterialHandle materialHandle, Date handleDate);
}
