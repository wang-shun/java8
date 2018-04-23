package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockTransferDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.DoctorWarehouseStockHandleManager;
import io.terminus.doctor.basic.manager.DoctorWarehouseStockManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/4/19.
 */
@Slf4j
public abstract class AbstractWarehouseStockService<T extends AbstractWarehouseStockDto, F extends AbstractWarehouseStockDetail> {

    @Autowired
    private LockRegistry lockRegistry;

    @Autowired
    protected DoctorWareHouseDao doctorWareHouseDao;
    @Autowired
    protected DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;
    @Autowired
    protected DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    protected DoctorWarehouseStockHandleManager doctorWarehouseStockHandleManager;
    @Autowired
    protected DoctorWarehouseStockManager doctorWarehouseStockManager;

    public Response<Long> handle(T stockDto) {

        List<Lock> locks = lockedIfNecessary(stockDto);
        try {

            DoctorWareHouse wareHouse = doctorWareHouseDao.findById(stockDto.getWarehouseId());
            if (null == wareHouse)
                throw new ServiceException("warehouse.not.found");

            DoctorWarehouseStockHandle stockHandle;
            if (null == stockDto.getStockHandleId()) {
                //新增
                stockHandle = create(stockDto, wareHouse);
            } else {

                stockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getStockHandleId());

                beforeUpdate(stockDto, stockHandle);
                //编辑
                stockHandle = update(stockDto, wareHouse, stockHandle);
            }

            return Response.ok(stockHandle.getId());

        } finally {
            releaseLocks(locks);
        }
    }

    protected DoctorWarehouseStockHandle create(T stockDto, DoctorWareHouse wareHouse) {
        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, getMaterialHandleType(), null);

        this.getDetails(stockDto).forEach(detail -> {
            create(stockDto, detail, stockHandle, wareHouse);
        });
        return stockHandle;
    }

    protected DoctorWarehouseStockHandle update(T stockDto, DoctorWareHouse wareHouse, DoctorWarehouseStockHandle stockHandle) {

        //之前的明细单
        Map<Long, List<DoctorWarehouseMaterialHandle>> oldMaterialHandles = doctorWarehouseMaterialHandleDao.findByStockHandle(stockDto.getStockHandleId()).stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getId));

        List<F> details = this.getDetails(stockDto);

        oldMaterialHandles.forEach((materialHandleId, oldMaterialHandle) -> {
            boolean include = false;
            for (F detail : details) {
                if (materialHandleId.equals(detail.getMaterialHandleId())) {
                    include = true;
                    break;
                }
            }
            if (!include) { //如果单据中原有的明细未包含在请求参数中，表明这条明细已被删除
                delete(oldMaterialHandle.get(0));
            }
        });

        Map<F, DoctorWarehouseMaterialHandle> changed = new HashMap<>();
        details.forEach(detail -> {
            if (detail.getMaterialHandleId() == null)
                create(stockDto, detail, stockHandle, wareHouse);
            else if (oldMaterialHandles.containsKey(detail.getMaterialHandleId())) {
                DoctorWarehouseMaterialHandle materialHandle = oldMaterialHandles.get(detail.getMaterialHandleId()).get(0);

                if (!materialHandle.getMaterialId().equals(detail.getMaterialId())) {
                    create(stockDto, detail, stockHandle, wareHouse);
                    doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), wareHouse);
                    delete(materialHandle);
                    doctorWarehouseStockManager.out(materialHandle.getMaterialId(), materialHandle.getQuantity(), wareHouse);
                } else {
//                    changed(materialHandle, detail, stockHandle, stockDto, wareHouse);
                    changed.put(detail, materialHandle);
                }
            }
        });

        changed(changed, stockHandle, stockDto, wareHouse);

        //更新单据
        doctorWarehouseStockHandleManager.update(stockDto, stockHandle);
        return stockHandle;
    }

    /**
     * 为了子类自定义实现一些功能
     *
     * @param stockDto
     */
    public void beforeUpdate(T stockDto, DoctorWarehouseStockHandle stockHandle) {

    }

    protected abstract WarehouseMaterialHandleType getMaterialHandleType();

    protected abstract List<F> getDetails(T stockDto);

    protected abstract void create(T stockDto, F detail, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse);

    protected abstract void delete(DoctorWarehouseMaterialHandle materialHandle);


    public void changed(Map<F, DoctorWarehouseMaterialHandle> changed, DoctorWarehouseStockHandle stockHandle,
                        T stockDto,
                        DoctorWareHouse wareHouse) {
        changed.forEach((detail, materialHandle) -> {
            changed(materialHandle, detail, stockHandle, stockDto, wareHouse);
        });

    }

    protected abstract void changed(DoctorWarehouseMaterialHandle materialHandle,
                                    F detail,
                                    DoctorWarehouseStockHandle stockHandle,
                                    T stockDto,
                                    DoctorWareHouse wareHouse);


    private List<Lock> lockedIfNecessary(AbstractWarehouseStockDto stockDto) {

        if (stockDto.getStockHandleId() != null && !stockDto.getHandleDate().equals(Calendar.getInstance())) {

            List<Lock> locks = new ArrayList<>();

            log.info("lock for warehouse :{}", stockDto.getWarehouseId());
            Lock lock = lockRegistry.obtain(stockDto.getWarehouseId().toString());
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
