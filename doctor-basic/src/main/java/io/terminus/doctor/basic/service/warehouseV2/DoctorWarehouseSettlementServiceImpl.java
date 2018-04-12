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

        //是否正在结算中
        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        boolean locked = lock.tryLock();

        if (!locked) {
            return true;
        }

        boolean settlement = doctorWarehouseOrgSettlementDao.isSettled(orgId, settlementDate);
        lock.unlock();
        return settlement;
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

        //获取本仓库该会计年月之前的总库存量和总金额,只包括采购入库单
        AmountAndQuantityDto amountAndQuantityDto = doctorWarehouseMaterialHandleDao.findBalanceByAccountingDate(warehouseId, settlementDate.toDate());

        BigDecimal someTimeStockQuantity = amountAndQuantityDto.getQuantity();
        Long someTimeStockAmount = amountAndQuantityDto.getAmount();

        log.debug("before {}-{} stock quantity is {} and amount is {}", settlementDate.getYear(), settlementDate.getMonthOfYear(), someTimeStockQuantity, someTimeStockAmount);

        //获取本仓库改月
        List<DoctorWarehouseMaterialHandle> materialHandles = doctorWarehouseMaterialHandleDao.findByAccountingDate(warehouseId, settlementDate.getYear(), settlementDate.getMonthOfYear());

        for (DoctorWarehouseMaterialHandle m : materialHandles) {

            log.debug("settlement for material handle {},material {},warehouse {},quantity {}", m.getId(), m.getMaterialName(), m.getWarehouseName(), m.getQuantity());

            //盘盈单的单价采用上一笔采购入库单的单价
            if (m.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())) {
                //获取上一笔采购入库单
                DoctorWarehouseMaterialHandle previousIn = doctorWarehouseMaterialHandleDao.findPrevious(m, WarehouseMaterialHandleType.IN);
                if (null != previousIn) {
                    log.debug("use previous material handle[purchase in] unit price :{}", previousIn.getUnitPrice());
                    m.setUnitPrice(previousIn.getUnitPrice());
                }
            } else if (m.getType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue())) {
                //调入的单价是调出的单价
                DoctorWarehouseMaterialHandle transferOut = doctorWarehouseMaterialHandleDao.findById(m.getOtherTransferHandleId());
                if (null == transferOut)
                    throw new ServiceException("");
                if (null != transferOut.getUnitPrice())
                    m.setUnitPrice(transferOut.getUnitPrice());
                else {
                    settlement(transferOut.getWarehouseId(), settlementDate, settledWarehouses);
                    m.setUnitPrice(doctorWarehouseMaterialHandleDao.findById(m.getOtherTransferHandleId()).getUnitPrice());
                }
            } else if (m.getType().equals(WarehouseMaterialHandleType.FORMULA_IN.getValue())) {
                DoctorWarehouseMaterialHandle formulaOut = doctorWarehouseMaterialHandleDao.findById(m.getOtherTransferHandleId());
                if (null == formulaOut)
                    throw new ServiceException("");
                if (null != formulaOut.getUnitPrice())
                    m.setUnitPrice(formulaOut.getUnitPrice());
                else {
                    settlement(formulaOut.getWarehouseId(), settlementDate, settledWarehouses);
                    m.setUnitPrice(doctorWarehouseMaterialHandleDao.findById(m.getOtherTransferHandleId()).getUnitPrice());
                }
            } else {
                m.setUnitPrice(new BigDecimal(someTimeStockAmount).divide(someTimeStockQuantity, 2, BigDecimal.ROUND_HALF_UP).longValue());

                someTimeStockQuantity = someTimeStockQuantity.add(m.getQuantity());
                someTimeStockAmount = someTimeStockAmount + m.getUnitPrice();
            }

            doctorWarehouseMaterialHandleDao.update(m);
        }

        settledWarehouses.add(warehouseId);
    }


    @Override
    public void antiSettlement(Long orgId, Date settlementDate) {

    }
}
