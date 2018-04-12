package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseReturnManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * 结算服务
 * Created by sunbo@terminus.io on 2018/4/11.
 */
@Service
@RpcProvider
public class DoctorWarehouseSettlementServiceImpl implements DoctorWarehouseSettlementService {


    @Autowired
    private LockRegistry lockRegistry;

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

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
        return false;
    }

    @Override
    public void settlement(List<Long> farmIds, LocalDate settlementDate) {
        farmIds.forEach(f -> {
            doctorWareHouseDao.findByFarmId(f).forEach(w -> {

                AmountAndQuantityDto amountAndQuantityDto = doctorWarehouseMaterialHandleDao.findBalanceByAccountingDate(w.getId(), settlementDate.getYear(), settlementDate.getMonthValue());

                //获取本仓库改月
                doctorWarehouseMaterialHandleDao.findByAccountingDate(w.getId(), settlementDate.getYear(), settlementDate.getMonthValue()).forEach(
                        m -> {

                        }
                );
            });
        });

    }

    @Override
    public void antiSettlement(Long orgId, Date settlementDate) {

    }
}
