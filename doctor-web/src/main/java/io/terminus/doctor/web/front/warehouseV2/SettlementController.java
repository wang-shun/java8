package io.terminus.doctor.web.front.warehouseV2;

import com.alibaba.dubbo.rpc.RpcException;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialHandleReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSettlementService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import jline.internal.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
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
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @RequestMapping(method = RequestMethod.GET, value = "date")
    public Date getSettlementDate() {
        return doctorWarehouseSettlementService.getSettlementDate(new Date());
    }


    //得到该公司第一笔单据的会计年月，用来结算的时候做判断
    @RequestMapping(method = RequestMethod.GET, value = "/findSettlementDate")
    public Boolean findSettlementDate(@RequestParam Long orgId,
                                       @DateTimeFormat(pattern = "yyyy-MM")
                                       @RequestParam Date settlementDate){
        Date date = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findSettlementDate(orgId));
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if(!DateUtil.toDate(DateUtil.getYearMonth(settlementDate) + "-01").equals(date)){
            Log.info("该公司的第一笔入库单是从"+cal.get(Calendar.YEAR)+"年"+(cal.get(Calendar.MONTH)+1)+"月开始的，请重"+cal.get(Calendar.YEAR)+"年"+(cal.get(Calendar.MONTH)+1)+"月开始结算");
            throw new InvalidException("Please.pay.attention.to.settlement.in.YYYY-MM",cal.get(Calendar.YEAR),(cal.get(Calendar.MONTH)+1),cal.get(Calendar.YEAR),(cal.get(Calendar.MONTH)+1));
        }else{
            return true;
        }
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

        Boolean ff = doctorWarehouseSettlementService.findByOrgId(orgId);
        if(!ff) {
            Date date = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findSettlementDate(orgId));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (DateUtil.toDate(DateUtil.getYearMonth(settlementDate) + "-01").after(date)) {
                Log.info("该公司的第一笔入库单是从" + cal.get(Calendar.YEAR) + "年" + (cal.get(Calendar.MONTH) + 1) + "月开始的，请重" + cal.get(Calendar.YEAR) + "年" + (cal.get(Calendar.MONTH) + 1) + "月开始结算");
                throw new InvalidException("Please.pay.attention.to.settlement.in.YYYY-MM", cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1));
            }
        }

        if (doctorWarehouseSettlementService.isUnderSettlement(orgId))
            throw new ServiceException("under.settlement");
        if (doctorWarehouseSettlementService.isSettled(orgId, settlementDate))
            throw new ServiceException("already.settlement");

        List<Long> farmIds = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(orgId)).stream().map(DoctorFarm::getId).collect(Collectors.toList());

        try {
            RespHelper.orServEx(doctorWarehouseSettlementService.settlement(orgId, farmIds,
                    settlementDate));
        } catch (RpcException e) {
            throw new JsonResponseException("结算量大，计算耗时，请稍后查询结算结果");
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
