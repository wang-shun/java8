package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseSkuDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseUnitOrg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * @param handleDate  入库类型，是handleDate+00:00:00；出库类型，是handleDate+23:59:59
     * @param newQuantity 入库类型，是正数；出库类型，是负数
     */
    protected void recalculate(Date handleDate, Long warehouseId, BigDecimal newQuantity) {
        BigDecimal historyQuantity = getHistoryQuantity(handleDate, warehouseId);

        List<DoctorWarehouseMaterialHandle> needToRecalculate = doctorWarehouseMaterialHandleDao.findAfter(warehouseId, handleDate);

        for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : needToRecalculate) {
            if (historyQuantity.compareTo(doctorWarehouseMaterialHandle.getQuantity()) < 0)
                throw new ServiceException("warehouse.stock.not.enough");

            doctorWarehouseMaterialHandle.setBeforeStockQuantity(historyQuantity);
            historyQuantity = historyQuantity.add(doctorWarehouseMaterialHandle.getQuantity());
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
    public List<T> getNew(List<DoctorWarehouseMaterialHandle> materialHandles) {
        return Collections.emptyList();
    }

    public List<DoctorWarehouseMaterialHandle> getDelete(List<DoctorWarehouseMaterialHandle> materialHandles) {
        return Collections.emptyList();
    }

    public Map<AbstractWarehouseStockDetail, DoctorWarehouseMaterialHandle> getUpdate(List<DoctorWarehouseMaterialHandle> materialHandles) {
        return Collections.emptyMap();
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

    protected BigDecimal getHistoryQuantity(Date handleDate, Long warehouseId) {
        return new BigDecimal(0);
    }


    public abstract void create(T detail,
                                AbstractWarehouseStockDto stockDto,
                                DoctorWarehouseStockHandle stockHandle,
                                DoctorWareHouse wareHouse);

    public abstract void delete(DoctorWarehouseMaterialHandle materialHandle, Date handleDate);
}
