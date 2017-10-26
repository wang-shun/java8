package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseVendorReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseVendorWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(method = RequestMethod.GET)
    public Paging<DoctorWarehouseVendor> query(@RequestParam Integer pageNo,
                                               @RequestParam Integer pageSize,
                                               @RequestParam Map<String, Object> params) {
        return RespHelper.or500(doctorWarehouseVendorReadService.paging(pageNo, pageSize, params));
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public DoctorWarehouseVendor query(@PathVariable Long id) {
        return RespHelper.or500(doctorWarehouseVendorReadService.findById(id));
    }

    @RequestMapping(method = RequestMethod.POST)
    public Long create(@Validated DoctorWarehouseVendor doctorWarehouseVendor, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        return RespHelper.or500(doctorWarehouseVendorWriteService.create(doctorWarehouseVendor));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public boolean update(@Validated DoctorWarehouseVendor doctorWarehouseVendor, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        return RespHelper.or500(doctorWarehouseVendorWriteService.update(doctorWarehouseVendor));
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public boolean delete(@PathVariable Long id) {
        return RespHelper.or500(doctorWarehouseVendorWriteService.delete(id));
    }
}
