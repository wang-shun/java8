package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockTransferDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.DoctorWarehouseStockHandleManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;

import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * Created by sunbo@terminus.io on 2018/4/19.
 */
@Slf4j
public abstract class AbstractWarehouseStockService<T extends AbstractWarehouseStockDto, F extends AbstractWarehouseStockDetail> {

    @Autowired
    private LockRegistry lockRegistry;

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;
    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;

    @Autowired
    private DoctorWarehouseStockHandleManager doctorWarehouseStockHandleManager;

    public Response<Long> handle(T stockDto) {

        List<Lock> locks = lockedIfNecessary(stockDto);
        try {

            DoctorWareHouse wareHouse = doctorWareHouseDao.findById(stockDto.getWarehouseId());
            if (null == wareHouse)
                throw new ServiceException("warehouse.not.found");

            DoctorWarehouseStockHandle stockHandle;
            if (null == stockDto.getStockHandleId()) { //新增
                stockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, getMaterialHandleType());

                this.getDetails(stockDto).forEach(detail -> {
                    create(stockDto, detail, stockHandle, wareHouse);
                });
            } else {//编辑
                stockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getStockHandleId());

                //更新单据
                doctorWarehouseStockHandleManager.update(stockDto, stockHandle);
            }

            return Response.ok(stockHandle.getId());

        } finally {
            releaseLocks(locks);
        }
    }

    protected abstract WarehouseMaterialHandleType getMaterialHandleType();

    protected abstract List<F> getDetails(T stockDto);

    protected abstract void create(T stockDto, F detail, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse);

    private List<Lock> lockedIfNecessary(AbstractWarehouseStockDto stockDto) {

        if (stockDto.getStockHandleId() != null && !stockDto.getHandleDate().equals(Calendar.getInstance())) {

            List<Lock> locks = new ArrayList<>();

            log.info("lock for warehouse :{}", stockDto.getWarehouseId());
            Lock lock = lockRegistry.obtain(stockDto.getWarehouseId());
            if (!lock.tryLock())
                throw new JsonResponseException("stock.handle.in.operation");

            locks.add(lock);
            if (stockDto instanceof WarehouseStockTransferDto) {
                Set<Long> transferInWarehouseIds = new HashSet<>();
                ((WarehouseStockTransferDto) stockDto).getDetails().forEach(d -> {
                    transferInWarehouseIds.add(d.getTransferInWarehouseId());
                });

                transferInWarehouseIds.forEach(id -> {
                    log.info("lock for warehouse :{}", id);
                    Lock l = lockRegistry.obtain(id);
                    if (!l.tryLock())
                        throw new JsonResponseException("stock.handle.in.operation");
                    locks.add(l);
                });
            }

            return locks;
        }
        return Collections.emptyList();
    }

    private void releaseLocks(List<Lock> locks) {
        locks.forEach(l -> {
            l.unlock();
        });
    }


}
