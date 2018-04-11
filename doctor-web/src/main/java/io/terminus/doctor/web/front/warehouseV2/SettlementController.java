package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSettlementService;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.locks.Lock;

/**
 * 结算服务
 * Created by sunbo@terminus.io on 2018/4/10.
 */
@RestController
@RequestMapping("api/doctor/warehouse/settlement")
public class SettlementController {

    @Autowired
    private LockRegistry lockRegistry;

    @RpcConsumer
    private DoctorWarehouseSettlementService doctorWarehouseSettlementService;

    /**
     * 结算
     *
     * @param orgId          公司id
     * @param settlementDate 需要结算的会计年月
     */
    @RequestMapping(method = RequestMethod.POST)
    public Response<Boolean> settlement(@RequestParam Long orgId,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd")
                                        @RequestParam Date settlementDate) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        if (!lock.tryLock())
            return Response.fail("under.settlement");

        doctorWarehouseSettlementService.settlement(orgId, settlementDate);

        lock.unlock();

        return Response.ok(true);
    }


    /**
     * 反结算
     *
     * @param orgId          公司id
     * @param settlementDate 需要反结算的会计年月
     */
    @RequestMapping(method = RequestMethod.POST, value = "anti")
    public Response<Boolean> AntiSettlement(@RequestParam Long orgId, @RequestParam Date settlementDate) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        if (!lock.tryLock())
            return Response.fail("under.settlement");

        doctorWarehouseSettlementService.antiSettlement(orgId, settlementDate);

        lock.unlock();

        return Response.ok(true);
    }
}
