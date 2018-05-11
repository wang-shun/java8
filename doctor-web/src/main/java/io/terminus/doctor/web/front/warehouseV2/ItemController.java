package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseItemOrgReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseItemOrgWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物料类目
 * Created by sunbo@terminus.io on 2017/11/2.
 */
@RestController
@RequestMapping("api/doctor/warehouse/item")
public class ItemController {

    @RpcConsumer
    private DoctorWarehouseItemOrgReadService doctorWarehouseItemOrgReadService;
    @RpcConsumer
    private DoctorWarehouseItemOrgWriteService doctorWarehouseItemOrgWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @RequestMapping(method = RequestMethod.POST, value = "org")
    public void boundToOrg(@RequestParam String itemIds,
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

        doctorWarehouseItemOrgWriteService.boundToOrg(itemIds, orgId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "org/id")
    public List<Long> queryByOrg(@RequestParam(required = false) Long orgId,
                                 @RequestParam(required = false) Long farmId) {


        if (null == orgId && null == farmId)
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");
        if (null == orgId) {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            orgId = farm.getOrgId();
        }

        return RespHelper.or500(doctorWarehouseItemOrgReadService.findByOrgId(orgId)).stream().map(DoctorBasicMaterial::getId).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, value = "suggest")
    public List<DoctorBasicMaterial> suggest(@RequestParam Integer type,
                                             @RequestParam(required = false) Long orgId,
                                             @RequestParam(required = false) Long farmId,
                                             @RequestParam(required = false) String name) {

        if (null == orgId && null == farmId)
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");
        if (null == orgId) {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            orgId = farm.getOrgId();
        }

        return RespHelper.or500(doctorWarehouseItemOrgReadService.suggest(type, orgId, name));
    }

}
