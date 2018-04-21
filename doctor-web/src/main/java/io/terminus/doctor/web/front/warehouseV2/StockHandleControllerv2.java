package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.core.export.Exporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/doctor/warehouse/receipt")
public class StockHandleControllerv2 {

    @Autowired
    private Exporter exporter;
    @RpcConsumer
    private DoctorWarehouseStockHandleReadService doctorWarehouseStockHandleReadService;
    @RpcConsumer
    private DoctorWarehouseStockHandleWriteService doctorWarehouseStockHandleWriteService;
    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorWareHouseReadService doctorWareHouseReadService;
    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;
    @RpcConsumer
    private DoctorWarehouseVendorReadService doctorWarehouseVendorReadService;
    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    //公司单据数据展示
    @RequestMapping(value = "/companyReport", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<List<List<Map>>> companyReport(@RequestParam(required = false) Long farmId,
                                             @RequestParam(required = false,value = "settlementDateStart") Date settlementDateStart,
                                             @RequestParam(required = false,value = "settlementDateEnd") Date settlementDateEnd){
        if (null != settlementDateStart && null != settlementDateEnd && settlementDateStart.after(settlementDateEnd))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> params = new HashMap<>();
        params.put("farmId",farmId);
        params.put("settlementDateStart",settlementDateStart);
        params.put("settlementDateEnd",settlementDateEnd);
        return doctorWarehouseMaterialHandleReadService.companyReport(params);
    }




}
