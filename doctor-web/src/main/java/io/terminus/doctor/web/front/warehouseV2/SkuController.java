package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/10/13.
 */
@RestController
@RequestMapping("api/doctor/warehouse/sku")
public class SkuController {

    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;
    @RpcConsumer
    private DoctorWarehouseSkuWriteService doctorWarehouseSkuWriteService;

    @RequestMapping(method = RequestMethod.GET)
    public Paging<DoctorWarehouseSku> query(@RequestParam(required = false) int pageNo,
                                            @RequestParam(required = false) int pageSize) {
        Map<String, Object> params = new HashMap<>();

        return RespHelper.or500(doctorWarehouseSkuReadService.paging(pageNo, pageSize, params));
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public DoctorWarehouseSku query(@PathVariable Long id) {
        return RespHelper.or500(doctorWarehouseSkuReadService.findById(id));
    }


    @RequestMapping(method = RequestMethod.PUT)
    public boolean edit(DoctorWarehouseSku sku) {
        return RespHelper.or500(doctorWarehouseSkuWriteService.update(sku));
    }


    @RequestMapping(method = RequestMethod.POST)
    public boolean save(DoctorWarehouseSku sku) {
        return null != RespHelper.or500(doctorWarehouseSkuWriteService.create(sku));
    }
}
