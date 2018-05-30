package io.terminus.doctor.web.front.warehouseV2;

import com.alibaba.dubbo.rpc.RpcException;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSettlementService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
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

    @RequestMapping(method = RequestMethod.GET, value = "date")
    public Date getSettlementDate() {
        return doctorWarehouseSettlementService.getSettlementDate(new Date());
    }

    /**
     * 结算
     *
     * @param orgId          公司id
     * @param settlementDate 需要结算的会计年月
     */
    @RequestMapping(method = RequestMethod.POST)
    public Boolean settlement(@RequestParam Long orgId,
                              @DateTimeFormat(pattern = "yyyy-MM")
                              @RequestParam Date settlementDate) {

        if (doctorWarehouseSettlementService.isUnderSettlement(orgId))
            throw new ServiceException("under.settlement");
        if (doctorWarehouseSettlementService.isSettled(orgId, settlementDate))
            throw new ServiceException("already.settlement");

        List<Long> farmIds = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(orgId)).stream().map(DoctorFarm::getId).collect(Collectors.toList());

        try {
            RespHelper.orServEx(doctorWarehouseSettlementService.settlement(orgId, farmIds,
                    settlementDate));
        } catch (RpcException e) {
            throw new JsonResponseException("结算超时");
        }

        return true;
    }

    /**
     * 反结算
     *
     * @param orgId          公司id
     * @param settlementDate 需要反结算的会计年月
     */
    @RequestMapping(method = RequestMethod.POST, value = "anti")
    public Boolean AntiSettlement(@RequestParam Long orgId,
                                  @DateTimeFormat(pattern = "yyyy-MM")
                                  @RequestParam Date settlementDate) {

        if (doctorWarehouseSettlementService.isUnderSettlement(orgId))
            throw new ServiceException("under.settlement");

        RespHelper.orServEx(doctorWarehouseSettlementService.antiSettlement(orgId, RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(orgId)).stream().map(DoctorFarm::getId).collect(Collectors.toList()), settlementDate));
        return true;
    }
}
