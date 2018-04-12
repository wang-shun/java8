package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSettlementService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    /**
     * 结算
     *
     * @param orgId          公司id
     * @param settlementDate 需要结算的会计年月
     */
    @RequestMapping(method = RequestMethod.POST)
    public void settlement(@RequestParam Long orgId,
                           @DateTimeFormat(pattern = "yyyy-MM-dd")
                           @RequestParam LocalDate settlementDate) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        if (!lock.tryLock())
            throw new JsonResponseException("under.settlement");


        //TODO 上个结算周日是否已经结算

        doctorWarehouseSettlementService.settlement(RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(orgId)).stream().map(DoctorFarm::getId).collect(Collectors.toList()),
                settlementDate);

        lock.unlock();
    }


    /**
     * 反结算
     *
     * @param orgId          公司id
     * @param settlementDate 需要反结算的会计年月
     */
    @RequestMapping(method = RequestMethod.POST, value = "anti")
    public void AntiSettlement(@RequestParam Long orgId, @RequestParam Date settlementDate) {

        Lock lock = lockRegistry.obtain("settlement/" + orgId);

        if (!lock.tryLock())
            throw new JsonResponseException("under.settlement");

        doctorWarehouseSettlementService.antiSettlement(orgId, settlementDate);

        lock.unlock();
    }
}
