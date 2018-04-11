package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;
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


    @Override
    public boolean isUnderSettlement(Long orgId) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        boolean locked = lock.tryLock();
        if (locked)
            lock.unlock();

        return locked;
    }

    @Override
    public void settlement(Long orgId, Date settlementDate) {

    }

    @Override
    public void antiSettlement(Long orgId, Date settlementDate) {

    }
}
