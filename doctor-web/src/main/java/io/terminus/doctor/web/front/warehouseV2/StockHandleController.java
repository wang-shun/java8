package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialHandleReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockHandleReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.web.front.warehouseV2.vo.StockHandleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存操作单据
 * 出库
 * 入库
 * 调拨
 * 盘点
 * Created by sunbo@terminus.io on 2017/10/31.
 */
@RestController
@RequestMapping("api/doctor/warehouse/receipt")
public class StockHandleController {

    @RpcConsumer
    private DoctorWarehouseStockHandleReadService doctorWarehouseStockHandleReadService;
    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;

    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    @RequestMapping(method = RequestMethod.GET)
    public Paging<DoctorWarehouseStockHandle> paging(@RequestParam Long farmId,
                                                     @RequestParam(required = false) Integer pageNo,
                                                     @RequestParam(required = false) Integer pageSize,
                                                     @RequestParam(required = false) Date startDate,
                                                     @RequestParam(required = false) Date endDate,
                                                     @RequestParam(required = false) Long operatorId,
                                                     @RequestParam(required = false) Long warehouseId,
                                                     @RequestParam(required = false) Integer type,
                                                     @RequestParam(required = false) Integer subType) {

        if (null != startDate && null != endDate && startDate.after(endDate))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("warehouseId", warehouseId);
        params.put("operatorId", operatorId);
        params.put("handleType", type);
        params.put("handleSubType", subType);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return RespHelper.or500(doctorWarehouseStockHandleReadService.paging(pageNo, pageSize, params));
    }


    @RequestMapping(method = RequestMethod.GET, value = "{id:\\d+}")
    public StockHandleVo query(@PathVariable Long id) {

        DoctorWarehouseStockHandle stockHandle = RespHelper.or500(doctorWarehouseStockHandleReadService.findById(id));
        if (null == stockHandle)
            return null;

        StockHandleVo vo = new StockHandleVo();
        BeanUtils.copyProperties(stockHandle, vo);

        vo.setDetails(
                RespHelper.or500(doctorWarehouseMaterialHandleReadService.findByStockHandle(stockHandle.getId()))
                        .stream()
                        .map(mh -> {
                            StockHandleVo.Detail detail = new StockHandleVo.Detail();
                            BeanUtils.copyProperties(mh, detail);
                            return detail;
                        })
                        .collect(Collectors.toList()));

        return vo;
    }


    @RequestMapping(method = RequestMethod.GET, value = "export")
    public void export() {

    }


}
