package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseUnitOrgReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseUnitOrgWriteService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseVendorWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2017/10/30.
 */
@RestController
@RequestMapping("api/doctor/warehouse/unit")
public class UnitController {

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorWarehouseUnitOrgReadService doctorWarehouseUnitOrgReadService;
    @RpcConsumer
    private DoctorWarehouseUnitOrgWriteService doctorWarehouseUnitOrgWriteService;


    @RequestMapping(method = RequestMethod.GET, value = "all")
    public List<DoctorBasic> query() {
        return RespHelper.or500(doctorWarehouseUnitOrgReadService.list());
    }

    /**
     * 绑定到公司
     *
     * @param unitIds
     * @param orgId
     * @param farmId
     */
    @RequestMapping(method = RequestMethod.POST, value = "org")
    public boolean boundToOrg(@RequestParam String unitIds,        //多个id以,分割
                              @RequestParam(required = false) Long orgId,
                              @RequestParam(required = false) Long farmId) {

        if (null == orgId && null == farmId)
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");
        if (null == orgId) {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            orgId = farm.getOrgId();
        }

        return RespHelper.or500(doctorWarehouseUnitOrgWriteService.boundToOrg(orgId, unitIds));
    }

    /**
     * 查询公司下所绑定的单位
     *
     * @param orgId
     * @param farmId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "org")
    public List<DoctorBasic> queryByOrg(@RequestParam(required = false) Long orgId,
                                        @RequestParam(required = false) Long farmId) {
        return RespHelper.or500(doctorWarehouseUnitOrgReadService.findByOrgId(orgId));
    }

}
