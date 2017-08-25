package io.terminus.doctor.web.front.new_warehouse;

import ch.qos.logback.core.joran.conditional.ElseAction;
import com.fasterxml.jackson.annotation.JsonView;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.service.DoctorWarehouseMaterialHandleReadService;
import io.terminus.doctor.web.core.exceptions.NotLoginException;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseMaterialEventVo;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseMaterialHandleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@Slf4j
@RestController
@RequestMapping("api/doctor/warehouse/event")
public class EventController {

    @Autowired
    private Exporter exporter;

    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;

    //TODO 可能需要加一个逻辑，如果物料处理记录处理的物料是仓库中最后一笔处理记录，则允许出现删除按钮，允许删除
    @RequestMapping(method = RequestMethod.GET)
//    @JsonView(WarehouseMaterialHandleVo.MaterialHandleEventView.class)
    public Paging<WarehouseMaterialEventVo> paging(
            @RequestParam Long farmId,
            @RequestParam(required = false) Integer type,//1入库2出库
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {


        List<Integer> types = new ArrayList<>();
        if (type != null) {
            if (1 == type) {
                types.add(WarehouseMaterialHandleType.IN.getValue());
                types.add(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
                types.add(WarehouseMaterialHandleType.TRANSFER_IN.getValue());
            } else if (2 == type) {
                types.add(WarehouseMaterialHandleType.OUT.getValue());
                types.add(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
                types.add(WarehouseMaterialHandleType.TRANSFER_OUT.getValue());
            } else
                throw new JsonResponseException("warehouse.event.type.not.support");
        }

        if (null != startDate && null == endDate)
            endDate = new Date();
        if (null != startDate && null != endDate && startDate.after(endDate))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("farmId", farmId);
        criteria.put("startDate", startDate);
        criteria.put("endDate", endDate);
        criteria.put("bigType", types);

        Response<Paging<DoctorWarehouseMaterialHandle>> handleResponse = doctorWarehouseMaterialHandleReadService.advPaging(pageNo, pageSize, criteria);
        if (!handleResponse.isSuccess())
            throw new JsonResponseException(handleResponse.getError());

        List<WarehouseMaterialEventVo> vos = new ArrayList<>(handleResponse.getResult().getData().size());
        for (DoctorWarehouseMaterialHandle handle : handleResponse.getResult().getData()) {
            vos.add(WarehouseMaterialEventVo.builder()
                    .id(handle.getId())
                    .materialName(handle.getMaterialName())
                    .warehouseName(handle.getWarehouseName())
                    .handleDate(handle.getHandleDate())
                    .quantity(handle.getQuantity())
                    .type(handle.getType())
                    .unit("")//TODO 数据库添加字段
                    .unitPrice(handle.getUnitPrice())
                    .amount(handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue())
                    .vendorName(handle.getVendorName())
                    .build());
        }

        Paging<WarehouseMaterialEventVo> warehouseMaterialHandleVoPaging = new Paging<>();
        warehouseMaterialHandleVoPaging.setTotal(handleResponse.getResult().getTotal());
        warehouseMaterialHandleVoPaging.setData(vos);
        return warehouseMaterialHandleVoPaging;
    }

    @RequestMapping(method = RequestMethod.GET, value = "export")
    public void export(@RequestParam Long farmId,
                       @RequestParam(required = false) Integer type,//1入库2出库
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                       HttpServletRequest request,
                       HttpServletResponse response) {

        if (null != startDate && null == endDate)
            endDate = new Date();
        if (null != startDate && null != endDate && startDate.after(endDate))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("farmId", farmId);
        criteria.put("startDate", startDate);
        criteria.put("endDate", endDate);
        List<Integer> types = new ArrayList<>();
        if (null != type) {
            if (1 == type) {
                types.add(WarehouseMaterialHandleType.IN.getValue());
                types.add(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
                types.add(WarehouseMaterialHandleType.TRANSFER_IN.getValue());
            } else if (2 == type) {
                types.add(WarehouseMaterialHandleType.OUT.getValue());
                types.add(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
                types.add(WarehouseMaterialHandleType.TRANSFER_OUT.getValue());
            } else
                throw new JsonResponseException("warehouse.event.type.not.support");
        }
        Response<List<DoctorWarehouseMaterialHandle>> handleResponse = doctorWarehouseMaterialHandleReadService.advList(criteria);
        if (!handleResponse.isSuccess())
            throw new JsonResponseException(handleResponse.getError());

        exporter.export(handleResponse.getResult(), "web-material-export", request, response);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public void delete(@PathVariable Long id) {


        Response<DoctorWarehouseMaterialHandle> handleResponse = doctorWarehouseMaterialHandleReadService.findById(id);
        if (!handleResponse.isSuccess())
            throw new JsonResponseException(handleResponse.getError());
        if (null == handleResponse.getResult()) {
            log.info("物料处理明细不存在,忽略仓库事件删除操作,id[{}]", id);
            return;
        }

        //删除出库，直接逻辑删除，stock+库存，purchase+handle_quantity，改handle_flag


        //删除入库，查询入库对应的purchase，purchase的handle_quantity是否大于0，也就是是否已出过库，出过库不允许删除



        //删除盘点

        //删除调拨

        //删除配方生产


        Map<String, Object> criteria = new HashMap<>();
        criteria.put("warehouseId", handleResponse.getResult().getWarehouseId());
        criteria.put("afterHandleDate", handleResponse.getResult().getHandleDate());
        doctorWarehouseMaterialHandleReadService.advList(criteria);
    }


}

