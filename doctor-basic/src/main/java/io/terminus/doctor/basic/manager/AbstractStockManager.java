package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/4/8.
 */
public abstract class AbstractStockManager<T extends AbstractWarehouseStockDetail, F extends AbstractWarehouseStockDto> {

    @Autowired
    private LockRegistry lockRegistry;

    @Autowired
    protected DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    protected DoctorWarehouseSkuDao doctorWarehouseSkuDao;

    @Autowired
    protected DoctorWarehouseStockDao doctorWarehouseStockDao;

    @Autowired
    protected DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;

    @Autowired
    protected DoctorBasicDao doctorBasicDao;

    /**
     * 重算
     * 该单据明细所在的那一时间之后所有的单据重算
     *
     * @param materialHandle
     */
    public void recalculate(DoctorWarehouseMaterialHandle materialHandle) {

        //历史库存量,不包括该笔单据所在的那一天
        BigDecimal historyQuantity = getHistoryQuantity(materialHandle.getHandleDate(), materialHandle.getWarehouseId(), materialHandle.getMaterialId());

        //重算单据明细，包括该笔单据所在的那一天
        recalculate(materialHandle.getHandleDate(), true, materialHandle.getWarehouseId(), materialHandle.getMaterialId(), historyQuantity);
    }

    public void recalculate(DoctorWarehouseMaterialHandle materialHandle, Date recalculateDate) {

        //历史库存量,不包括该笔单据所在的那一天
        BigDecimal historyQuantity = getHistoryQuantity(recalculateDate, materialHandle.getWarehouseId(), materialHandle.getMaterialId());

        //重算单据明细，包括该笔单据所在的那一天
        recalculate(recalculateDate, true, materialHandle.getWarehouseId(), materialHandle.getMaterialId(), historyQuantity);
    }

    /**
     * 重算
     *
     * @param handleDate      入库类型，是handleDate+00:00:00；出库类型，是handleDate+23:59:59
     * @param historyQuantity 历史库存量
     */
    public void recalculate(Date handleDate, boolean includeHandleDate, Long warehouseId, Long skuId, BigDecimal historyQuantity) {

        List<DoctorWarehouseMaterialHandle> needToRecalculate = doctorWarehouseMaterialHandleDao.findAfter(warehouseId, skuId, handleDate, includeHandleDate);

        for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : needToRecalculate) {
            if (WarehouseMaterialHandleType.isBigOut(doctorWarehouseMaterialHandle.getType())
                    && historyQuantity.compareTo(doctorWarehouseMaterialHandle.getQuantity()) < 0)
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
     * 构建事件日期
     * 新的事件日期只包含年月日，对于入库类型+00:00:00；对于出库类型23:59:59
     *
     * @param handleType    事件类型
     * @param newHandleDate 新的事件日期
     * @return
     */
    public Date buildNewHandleDate(WarehouseMaterialHandleType handleType, Calendar newHandleDate) {

//        if (WarehouseMaterialHandleType.isBigIn(handleType.getValue())) {
//            newHandleDate.set(Calendar.HOUR_OF_DAY, 0);
//            newHandleDate.set(Calendar.MINUTE, 0);
//            newHandleDate.set(Calendar.SECOND, 0);
//            newHandleDate.set(Calendar.MILLISECOND, 0);
//            return newHandleDate.getTime();
//        } else {
            newHandleDate.set(Calendar.HOUR_OF_DAY, 23);
            newHandleDate.set(Calendar.MINUTE, 59);
            newHandleDate.set(Calendar.SECOND, 59);
            newHandleDate.set(Calendar.MILLISECOND, 0);
            return newHandleDate.getTime();
//        }
    }

    public Date buildNewHandleDateForUpdate(WarehouseMaterialHandleType handleType, Calendar newHandleDate) {

        if (DateUtil.inSameDate(newHandleDate.getTime(), new Date())) {
            DateTime old = new DateTime(newHandleDate.getTime());
            return new DateTime().withDate(old.getYear(), old.getMonthOfYear(), old.getDayOfMonth()).toDate();
        }

//        if (WarehouseMaterialHandleType.isBigIn(handleType.getValue())) {
//            newHandleDate.set(Calendar.HOUR_OF_DAY, 0);
//            newHandleDate.set(Calendar.MINUTE, 0);
//            newHandleDate.set(Calendar.SECOND, 0);
//            newHandleDate.set(Calendar.MILLISECOND, 0);
//            return newHandleDate.getTime();
//        } else {
            newHandleDate.set(Calendar.HOUR_OF_DAY, 23);
            newHandleDate.set(Calendar.MINUTE, 59);
            newHandleDate.set(Calendar.SECOND, 59);
            newHandleDate.set(Calendar.MILLISECOND, 0);
            return newHandleDate.getTime();
//        }
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

    /**
     * 获取删除的单据明细
     *
     * @param materialHandles
     * @param details
     * @return
     */
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
     * 获取需要被重算的单据
     *
     * @param warehouseId 仓库id
     * @param skuId       物料id
     * @param handleDate  需要被重算的事件日期，包括这一天
     * @return 需要被重算的单据明细
     */
//    protected List<DoctorWarehouseMaterialHandle> getMaterialHandleAfter(Long warehouseId, Long skuId, Date handleDate) {
//
//        return doctorWarehouseMaterialHandleDao.findAfter(warehouseId, skuId, handleDate);
//    }

    /**
     * 获取历史某一天之前的库存量
     *
     * @param handleDate  这一时刻之前，不包括这一时刻
     * @param warehouseId 仓库id
     * @param skuId       物料id
     * @return 历史库存量
     */
    public BigDecimal getHistoryQuantity(Date handleDate, Long warehouseId, Long skuId) {
        //如果是入库，handleDate当日第一笔
        //如果是出库，handleDate当日最后一笔
        return doctorWarehouseMaterialHandleDao.getHistoryStock(warehouseId, skuId, handleDate, false);
    }

    /**
     * 获取历史某一时刻之前的库存量
     *
     * @param handleDate  这一时刻之前，包括这一时刻
     * @param warehouseId 仓库id
     * @param skuId       物料id
     * @return 历史库存量
     */
    public BigDecimal getHistoryQuantityInclude(Date handleDate, Long warehouseId, Long skuId) {
        return doctorWarehouseMaterialHandleDao.getHistoryStock(warehouseId, skuId, handleDate, true);
    }

    /**
     * 插入明细单据
     * 如果是入库类型的明细单，handleDate+00:00:00,id正序，排在00:00:00的最后
     * 如果是出库类型的明细单，handleDate+23:59:59,id正序，排在23:59:59的最后
     *
     * @param detail
     * @param stockDto
     * @param stockHandle
     * @param wareHouse
     */
    public abstract void create(T detail,
                                F stockDto,
                                DoctorWarehouseStockHandle stockHandle,
                                DoctorWareHouse wareHouse);

    public void create(List<T> details, F stockDto, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {

        details.forEach(d -> {
            create(d, stockDto, stockHandle, wareHouse);
        });
    }


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
        materialHandle.setUnitPrice(new BigDecimal(0));
        materialHandle.setAmount(new BigDecimal(0));
        materialHandle.setSettlementDate(stockDto.getSettlementDate());
        materialHandle.setHandleDate(stockDto.getHandleDate().getTime());
        materialHandle.setHandleYear(stockDto.getHandleDate().get(Calendar.YEAR));
        materialHandle.setHandleMonth(stockDto.getHandleDate().get(Calendar.MONTH) + 1);
        materialHandle.setOperatorId(stockDto.getOperatorId());
        materialHandle.setOperatorName(stockDto.getOperatorName());
        materialHandle.setRemark(detail.getRemark());

        return materialHandle;
    }

    public abstract void delete(DoctorWarehouseMaterialHandle materialHandle);
}

