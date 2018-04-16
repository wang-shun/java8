package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseOrgSettlementDao;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * 结算服务
 * Created by sunbo@terminus.io on 2018/4/11.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseSettlementServiceImpl implements DoctorWarehouseSettlementService {


    @Autowired
    private LockRegistry lockRegistry;

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehouseOrgSettlementDao doctorWarehouseOrgSettlementDao;

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;


    @Override
    public boolean isUnderSettlement(Long orgId) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        boolean locked = lock.tryLock();
        if (locked)
            lock.unlock();

        return locked;
    }

    @Override
    public boolean isSettled(Long orgId, Date settlementDate) {
        return doctorWarehouseOrgSettlementDao.isSettled(orgId, settlementDate);
    }

    @Override
    public void settlement(Long orgId, List<Long> farmIds, DateTime settlementDate) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        if (!lock.tryLock())
            throw new ServiceException("under.settlement");

        try {
            farmIds.forEach(f -> {

                log.info("start to settlement farm {}", f);

                doctorWareHouseDao.findByFarmId(f).forEach(w -> {

                    //已经结算的仓库
                    List<Long> settledWarehouses = new ArrayList<>();
                    settlement(w.getId(), settlementDate, settledWarehouses);
                });
            });
        } finally {
            lock.unlock();
        }
    }


    /**
     * 结算某一个仓库下某个会计区间的单据明细
     */
    private void settlement(Long warehouseId, DateTime settlementDate, List<Long> settledWarehouses) {

        if (settledWarehouses.contains(warehouseId))
            return;

        log.info("start to settlement warehouse {} at {}-{}", warehouseId, settlementDate.getYear(), settlementDate.getMonthOfYear());

        //获取本仓库该会计年月之前的总库存量和总金额
        AmountAndQuantityDto amountAndQuantityDto = doctorWarehouseMaterialHandleDao.findBalanceByAccountingDate(warehouseId, settlementDate.toDate());

        BigDecimal historyStockQuantity = amountAndQuantityDto.getQuantity();
        Long historyStockAmount = amountAndQuantityDto.getAmount();

        log.debug("before {}-{} stock quantity is {} and amount is {}", settlementDate.getYear(), settlementDate.getMonthOfYear(), historyStockQuantity, historyStockAmount);

        //获取本仓库该会计年月，需要结算的单据明细
        List<DoctorWarehouseMaterialHandle> materialHandles = doctorWarehouseMaterialHandleDao.findByAccountingDate(warehouseId, settlementDate.getYear(), settlementDate.getMonthOfYear());

        for (DoctorWarehouseMaterialHandle m : materialHandles) {

            log.debug("settlement for material handle {},material {},warehouse {},quantity {}", m.getId(), m.getMaterialName(), m.getWarehouseName(), m.getQuantity());

            if (WarehouseMaterialHandleType.isBigIn(m.getType())) {
                //入库类型：采购入库，退料入库，盘盈入库，调拨入库，配方生产入库

                //盘盈单的单价采用上一笔采购入库单的单价
                if (m.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())) {
                    //获取上一笔采购入库单
                    DoctorWarehouseMaterialHandle previousIn = doctorWarehouseMaterialHandleDao.findPrevious(m, WarehouseMaterialHandleType.IN);
                    if (null != previousIn) {
                        log.debug("use previous material handle[purchase in] unit price :{}", previousIn.getUnitPrice());
                        m.setUnitPrice(previousIn.getUnitPrice());
                    }
                } else if (m.getType().equals(WarehouseMaterialHandleType.FORMULA_IN.getValue())) {
                    //配方生产入库，根据出库的总价


                } else if (m.getType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue())

                        || m.getType().equals(WarehouseMaterialHandleType.RETURN.getValue())) {

                    DoctorWarehouseMaterialHandle otherIn = doctorWarehouseMaterialHandleDao.findById(m.getOtherTransferHandleId());
                    if (null == otherIn)
                        throw new ServiceException("material.handle.not.found");
                    if (null != otherIn.getUnitPrice())
                        m.setUnitPrice(otherIn.getUnitPrice());
                    else {
                        settlement(otherIn.getWarehouseId(), settlementDate, settledWarehouses);
                        m.setUnitPrice(doctorWarehouseMaterialHandleDao.findById(m.getOtherTransferHandleId()).getUnitPrice());
                    }
                }

                historyStockQuantity = historyStockQuantity.add(m.getQuantity());
                historyStockAmount = historyStockAmount + m.getUnitPrice();
            } else {
                //出库类型：领料出库，盘亏出库，调拨出库，配方生产出库


                historyStockQuantity = historyStockQuantity.subtract(m.getQuantity());
                historyStockAmount = historyStockAmount - m.getUnitPrice();
            }

            doctorWarehouseMaterialHandleDao.update(m);
        }

        settledWarehouses.add(warehouseId);
    }


    @Override
    @Transactional
    public void antiSettlement(Long orgId, List<Long> farmIds, DateTime settlementDate) {

        farmIds.forEach(f -> {
            //去除该会计年月内的单据明细的单价
            doctorWarehouseMaterialHandleDao.reverseSettlement(f, settlementDate.getYear(), settlementDate.getMonthOfYear());

        });

        doctorWarehouseOrgSettlementDao.delete(orgId);
    }
}
