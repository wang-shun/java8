package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/4/8.
 */
public abstract class AbstractStockManager {

    @Autowired
    private LockRegistry lockRegistry;

    @Autowired
    protected DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    /**
     * 重算
     *
     * @param materialHandle 该笔之后，包括该笔
     * @param newQuantity    修改后的数量
     */
    protected void recalculate(DoctorWarehouseMaterialHandle materialHandle, BigDecimal newQuantity) {

        if (materialHandle.getBeforeStockQuantity().compareTo(newQuantity) < 0)
            throw new ServiceException("");

        BigDecimal newStockQuantity = materialHandle.getBeforeStockQuantity().subtract(newQuantity);

        List<DoctorWarehouseMaterialHandle> needToRecalculate = getMaterialHandleAfter(materialHandle.getWarehouseId(), materialHandle.getId(), materialHandle.getHandleDate());
        for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : needToRecalculate) {
            if (newStockQuantity.compareTo(doctorWarehouseMaterialHandle.getQuantity()) < 0)
                throw new ServiceException("");

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
     * 获取某笔明细之后的明细，不包括该笔
     *
     * @param materialHandleId
     * @return 需要被重算的单据明细
     */
    protected List<DoctorWarehouseMaterialHandle> getMaterialHandleAfter(Long warehouseId, Long materialHandleId, Date handleDate) {

        return doctorWarehouseMaterialHandleDao.findAfter(warehouseId, materialHandleId, handleDate);
    }
}
