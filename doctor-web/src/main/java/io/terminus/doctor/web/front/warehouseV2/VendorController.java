package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.enums.WarehouseVendorDeleteFlag;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseVendorReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseVendorWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/10/26.
 */
@RestController
@RequestMapping("api/doctor/warehouse/vendor")
public class VendorController {


    @RpcConsumer
    private DoctorWarehouseVendorReadService doctorWarehouseVendorReadService;
    @RpcConsumer
    private DoctorWarehouseVendorWriteService doctorWarehouseVendorWriteService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @RequestMapping(method = RequestMethod.GET)
    public Paging<DoctorWarehouseVendor> query(@RequestParam Integer pageNo,
                                               @RequestParam Integer pageSize,
                                               @RequestParam Map<String, Object> params) {
        return RespHelper.or500(doctorWarehouseVendorReadService.paging(pageNo, pageSize, params));
    }

    @RequestMapping(method = RequestMethod.GET, value = "all")
    public List<DoctorWarehouseVendor> query() {
        return RespHelper.or500(doctorWarehouseVendorReadService.list(new HashMap<>()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public DoctorWarehouseVendor query(@PathVariable Long id) {
        return RespHelper.or500(doctorWarehouseVendorReadService.findById(id));
    }

    @RequestMapping(method = RequestMethod.POST)
    public Long create(@RequestBody @Validated DoctorWarehouseVendor doctorWarehouseVendor, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        return RespHelper.or500(doctorWarehouseVendorWriteService.create(doctorWarehouseVendor));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public boolean update(@RequestBody @Validated DoctorWarehouseVendor doctorWarehouseVendor, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        return RespHelper.or500(doctorWarehouseVendorWriteService.update(doctorWarehouseVendor));
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public boolean delete(@PathVariable Long id) {
        return RespHelper.or500(doctorWarehouseVendorWriteService.logicDelete(id));
    }


    /**
     * 绑定到公司
     *
     * @param vendorIds
     * @param orgId
     * @param farmId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "org")
    public boolean boundToOrg(@RequestParam String vendorIds,      //多个id以,分割
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

        return RespHelper.or500(doctorWarehouseVendorWriteService.boundToOrg(vendorIds, orgId));
    }


    /**
     * 查询公司下的所有厂商
     *
     * @param orgId
     * @param farmId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "org")
    public List<DoctorWarehouseVendor> queryByOrg(@RequestParam(required = false) Long orgId,
                                                  @RequestParam(required = false) Long farmId) {

        if (null == orgId && null == farmId)
            throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");
        if (null == orgId) {
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
            if (null == farm)
                throw new JsonResponseException("farm.not.found");
            orgId = farm.getOrgId();
        }

        return RespHelper.or500(doctorWarehouseVendorReadService.findByOrg(orgId));
    }

}
