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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by sunbo@terminus.io on 2017/8/24.
 */
@RestController
@RequestMapping("api/doctor/warehouse/event")
public class EventController {

    @Autowired
    private Exporter exporter;

    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;

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
                    .materialName(handle.getMaterialName())
                    .warehouseName(handle.getWarehouseName())
                    .handleDate(handle.getHandleDate())
                    .quantity(handle.getQuantity())
                    .build());
        }

        Paging<WarehouseMaterialEventVo> warehouseMaterialHandleVoPaging = new Paging<>();
        warehouseMaterialHandleVoPaging.setTotal(handleResponse.getResult().getTotal());
        warehouseMaterialHandleVoPaging.setData(vos);
        return warehouseMaterialHandleVoPaging;
    }

    @RequestMapping(method = RequestMethod.POST, value = "export")
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

        Map<String, Object> criteria = new HashMap<>();
//        criteria.put("");
        doctorWarehouseMaterialHandleReadService.list(criteria);
    }


}

