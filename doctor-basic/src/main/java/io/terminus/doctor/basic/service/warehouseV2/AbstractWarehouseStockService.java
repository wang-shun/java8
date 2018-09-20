package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.DoctorWarehouseStockHandleManager;
import io.terminus.doctor.basic.manager.DoctorWarehouseStockManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.transaction.annotation.Transactional;

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

    private static final ThreadLocal<List<Lock>> stockLocks = new ThreadLocal<>();

    @Transactional
    @ExceptionHandle("stock.handle.fail")
    public Response<InventoryDto> handle(T stockDto) {

        lockedIfNecessary(stockDto);
        try {

            DoctorWareHouse wareHouse = doctorWareHouseDao.findById(stockDto.getWarehouseId());
            if (null == wareHouse)
                throw new ServiceException("warehouse.not.found");

            // 判断新增或编辑的物料是否已盘点 （陈娟 2018-09-18）
            List<F> details = this.getDetails(stockDto);
            Iterator<F> it= details.iterator();
            String str = new String();
            while(it.hasNext()){
                if(null == it.next().getMaterialHandleId()) {// 新增：判斷物料是否盘点，是的話，刪除該物料
                    Date handleDate = stockDto.getHandleDate().getTime();
                    DoctorWarehouseMaterialHandle material = doctorWarehouseMaterialHandleDao.getMaxInventoryDate(stockDto.getWarehouseId(), it.next().getMaterialId(), handleDate);
                    if (material != null) {// 已盘点
                        str = str + material.getMaterialName() + ",";
                        it.remove();
                    }
                }else{ // 编辑：判断物料是否盘点，是的话，物料不修改 （陈娟 2018-09-19)
                    //之前的明细单
                    Map<Long, List<DoctorWarehouseMaterialHandle>> oldMaterialHandles = doctorWarehouseMaterialHandleDao.findByStockHandle(stockDto.getStockHandleId()).stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getId));
                    if (oldMaterialHandles.containsKey(it.next().getMaterialHandleId())) {
                        DoctorWarehouseMaterialHandle materialHandle = oldMaterialHandles.get(it.next().getMaterialHandleId()).get(0);
                        if (!materialHandle.getMaterialId().equals(it.next().getMaterialId())) {// 判断该单据物料是否更改
                            Date handleDate = stockDto.getHandleDate().getTime();
                            DoctorWarehouseMaterialHandle material = doctorWarehouseMaterialHandleDao.getMaxInventoryDate(stockDto.getWarehouseId(), it.next().getMaterialId(), handleDate);
                            if (material != null) {// 已盘点 （物料信息不可更改）
                                str = str + material.getMaterialName() + ",";
                                it.next().setBeforeStockQuantity(materialHandle.getBeforeStockQuantity().toString());
                                it.next().setMaterialId(materialHandle.getMaterialId());
                                it.next().setQuantity(materialHandle.getQuantity());
                                it.next().setRemark(materialHandle.getRemark());
                            }
                        }
                    }
                }
            }

            DoctorWarehouseStockHandle stockHandle;
            if (null == stockDto.getStockHandleId()) {
                //新增
                if(!str.equals("")){
                    str = str + "【已盘点,不可新增】";
                }else{
                    str = "数据已经提交";
                }
                stockHandle = create(stockDto, wareHouse);
            } else {
                if(!str.equals("")){
                    str = str + "【已盘点,不可编辑】";
                }else{
                    str = "数据已经提交";
                }
                stockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getStockHandleId());
                //编辑之前，可以做一些校验等
                beforeUpdate(stockDto, stockHandle);
                //编辑
                stockHandle = update(stockDto, wareHouse, stockHandle);
            }

            InventoryDto inventoryDto = new InventoryDto();
            inventoryDto.setId(stockHandle.getId());
            inventoryDto.setDesc(str);

            return Response.ok(inventoryDto);

        } catch (Throwable e) {
            throw e;
        } finally {
            releaseLocks();
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
        buildNewHandleDateForUpdate(stockHandle, stockDto.getHandleDate());
        doctorWarehouseStockHandleManager.update(stockDto, stockHandle);
        return stockHandle;
    }

    /**
     * 组织新的事件日期
     * 如果是历史日期，参数提交的事件日期+23:59:59
     * 如果是当日，时分秒采用当前时分秒
     *
     * @param stockHandle
     * @param newHandleDate
     */
    public void buildNewHandleDateForUpdate(DoctorWarehouseStockHandle stockHandle, Calendar newHandleDate) {

        if (DateUtil.inSameDate(newHandleDate.getTime(), new Date())) {
            DateTime old = new DateTime(newHandleDate.getTime());
            DateTime newDate = new DateTime().withDate(old.getYear(), old.getMonthOfYear(), old.getDayOfMonth());
            stockHandle.setHandleDate(newDate.toDate());
            return;
        }

        newHandleDate.set(Calendar.HOUR_OF_DAY, 23);
        newHandleDate.set(Calendar.MINUTE, 59);
        newHandleDate.set(Calendar.SECOND, 59);
        newHandleDate.set(Calendar.MILLISECOND, 0);
        stockHandle.setHandleDate(newHandleDate.getTime());
    }

    /**
     * 锁定仓库
     *
     * @param warehouseId
     */
    public void lockWarehouse(Long warehouseId) {
        List<Lock> locks = stockLocks.get();
        boolean isNewLocks = false;
        if (null == locks) {
            locks = new ArrayList<>();
            isNewLocks = true;
        }
        Lock lock = lockRegistry.obtain(warehouseId.toString());
        if (!lock.tryLock())
            throw new JsonResponseException("stock.handle.in.operation");
        locks.add(lock);

        if (isNewLocks)
            stockLocks.set(locks);
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


    private void lockedIfNecessary(AbstractWarehouseStockDto stockDto) {

        //新增/编辑当日单据不需要重算，也就不需要锁
//        if (DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date()))
//            return;
//
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
                Lock l = lockRegistry.obtain(id.toString());
                if (!l.tryLock())
                    throw new JsonResponseException("stock.handle.in.operation");
                locks.add(l);
            });
        } else if (stockDto.getStockHandleId() != null && stockDto instanceof WarehouseFormulaDto) {
            //配方生产新增，不支持新增历史单据，所以新增就不需要锁
            Set<Long> formulaOutWarehouseIds = new HashSet<>();
            ((WarehouseFormulaDto) stockDto).getDetails().forEach(d -> {
                formulaOutWarehouseIds.add(d.getWarehouseId());
            });
            formulaOutWarehouseIds.forEach(id -> {
                log.info("lock for warehouse :{}", id);
                Lock l = lockRegistry.obtain(id.toString());
                if (!l.tryLock())
                    throw new JsonResponseException("stock.handle.in.operation");
                locks.add(l);
            });
        }
        stockLocks.set(locks);
    }

    private void releaseLocks() {
        List<Lock> locks = stockLocks.get();
        if (null != locks) {
            locks.forEach(l -> {
                l.unlock();
            });
            stockLocks.set(null);
        }
    }


}
