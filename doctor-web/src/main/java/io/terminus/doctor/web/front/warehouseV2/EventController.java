package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialHandleReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialHandleWriteService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseEventExportVo;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseMaterialEventVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    @RpcConsumer
    private DoctorWarehouseMaterialHandleWriteService doctorWarehouseMaterialHandleWriteService;

    @RpcConsumer
    private DoctorWarehouseStockReadService doctorWarehouseStockReadService;
    @RpcConsumer
    private DoctorWarehouseStockWriteService doctorWarehouseStockWriteService;

    @RequestMapping(method = RequestMethod.GET)
//    @JsonView(WarehouseMaterialHandleVo.MaterialHandleEventView.class)
    public Paging<WarehouseMaterialEventVo> paging(
            @RequestParam Long farmId,
            @RequestParam(required = false) Integer type,//1入库2出库
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {


        List<Integer> types = WarehouseMaterialHandleType.getGroupType(type);

        if (null != startDate && null == endDate)
            endDate = new Date();
        if (null != startDate && null != endDate && startDate.after(endDate))
            throw new JsonResponseException("start.date.after.end.date");

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("farmId", farmId);
        criteria.put("startDate", startDate);
        criteria.put("endDate", endDate);
        criteria.put("bigType", types);
        criteria.put("materialId", materialId);
        criteria.put("deleteFlag", WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
        Response<Paging<DoctorWarehouseMaterialHandle>> handleResponse = doctorWarehouseMaterialHandleReadService.advPaging(pageNo, pageSize, criteria);
        if (!handleResponse.isSuccess())
            throw new JsonResponseException(handleResponse.getError());

        List<WarehouseMaterialEventVo> vos = new ArrayList<>(handleResponse.getResult().getData().size());
        for (DoctorWarehouseMaterialHandle handle : handleResponse.getResult().getData()) {

            boolean allowDelete = true;
            if (WarehouseMaterialHandleType.FORMULA_IN.getValue() == handle.getType() || WarehouseMaterialHandleType.FORMULA_OUT.getValue() == handle.getType()) {
                allowDelete = false;
            }
            if (WarehouseMaterialHandleType.IN.getValue() == handle.getType()
                    || WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue() == handle.getType()
                    || WarehouseMaterialHandleType.TRANSFER_IN.getValue() == handle.getType()) {
                Response<List<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.listMergeVendor(DoctorWarehouseStock.builder()
                        .warehouseId(handle.getWarehouseId())
                        .materialId(handle.getMaterialId())
                        .build());
                if (!stockResponse.isSuccess())
                    throw new JsonResponseException(stockResponse.getError());
                if (null == stockResponse.getResult() || stockResponse.getResult().isEmpty())
                    allowDelete = false;
                if (stockResponse.getResult().get(0).getQuantity().compareTo(handle.getQuantity()) < 0)
                    allowDelete = false;
            }

            vos.add(WarehouseMaterialEventVo.builder()
                    .id(handle.getId())
                    .materialName(handle.getMaterialName())
                    .warehouseName(handle.getWarehouseName())
                    .handleDate(handle.getHandleDate())
                    .quantity(handle.getQuantity())
                    .type(handle.getType())
                    .unit(handle.getUnit())
                    .unitPrice(handle.getUnitPrice())
                    .amount(handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue())
                    .vendorName(handle.getVendorName())
                    .allowDelete(allowDelete)
                    .operatorId(handle.getOperatorId())
                    .operatorName(handle.getOperatorName())
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
                       @RequestParam(required = false) Long materialId,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                       HttpServletRequest request,
                       HttpServletResponse response) {

        if (null != startDate && null == endDate)
            endDate = new Date();
        if (null != startDate && null != endDate && startDate.after(endDate))
            throw new JsonResponseException("start.date.after.end.date");

        List<Integer> types = WarehouseMaterialHandleType.getGroupType(type);

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("farmId", farmId);
        criteria.put("startDate", startDate);
        criteria.put("endDate", endDate);
        criteria.put("bigType", types);
        criteria.put("materialId", materialId);

        Response<List<DoctorWarehouseMaterialHandle>> handleResponse = doctorWarehouseMaterialHandleReadService.advList(criteria);
        if (!handleResponse.isSuccess())
            throw new JsonResponseException(handleResponse.getError());

        exporter.export(handleResponse.getResult().stream().map(handle -> {
            WarehouseEventExportVo eventExportVo = new WarehouseEventExportVo();
            eventExportVo.setMaterialName(handle.getMaterialName());
            eventExportVo.setWareHouseName(handle.getWarehouseName());
            eventExportVo.setProviderFactoryName(handle.getVendorName());
            eventExportVo.setUnitName(handle.getUnit());
            eventExportVo.setUnitPrice(handle.getUnitPrice());
            eventExportVo.setEventTime(handle.getHandleDate());
            eventExportVo.setAmount(handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue());
            return eventExportVo;
        }).collect(Collectors.toList()), "web-wareHouse-event", request, response);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public boolean delete(@PathVariable Long id) {


//        Response<DoctorWarehouseMaterialHandle> handleResponse = doctorWarehouseMaterialHandleReadService.findById(id);
//        if (!handleResponse.isSuccess())
//            throw new JsonResponseException(handleResponse.getError());
//        if (null == handleResponse.getResult()) {
//            log.info("物料处理明细不存在,忽略仓库事件删除操作,id[{}]", id);
//            return true;
//        }
//
//        DoctorWarehouseMaterialHandle handle = handleResponse.getResult();
//
//
//        if (WarehouseMaterialHandleType.IN.getValue() == handle.getType().intValue()) {
//
//            WarehouseStockOutDto outDto = new WarehouseStockOutDto();
//            outDto.setFarmId(handle.getFarmId());
//            outDto.setHandleDate(new Date());
//            outDto.setWarehouseId(handle.getWarehouseId());
//
//
//            WarehouseStockOutDto.WarehouseStockOutDetail detail = new WarehouseStockOutDto.WarehouseStockOutDetail();
//            detail.setMaterialId(handle.getMaterialId());
//            detail.setQuantity(handle.getQuantity());
//            detail.setJustOut(true);
//            outDto.setDetails(Collections.singletonList(detail));
//            doctorWarehouseStockWriteService.out(outDto);
//        } else if (WarehouseMaterialHandleType.OUT.getValue() == handle.getType()) {
//            WarehouseStockInDto inDto = new WarehouseStockInDto();
//            inDto.setFarmId(handle.getFarmId());
//            inDto.setWarehouseId(handle.getWarehouseId());
//            inDto.setHandleDate(new Date());
//
//            WarehouseStockInDto.WarehouseStockInDetailDto detail = new WarehouseStockInDto.WarehouseStockInDetailDto();
//            detail.setUnit(handle.getUnit());
//            detail.setUnitPrice(handle.getUnitPrice());
//            detail.setVendorName(handle.getVendorName());
//            detail.setMaterialId(handle.getMaterialId());
//            detail.setQuantity(handle.getQuantity());
//            inDto.setDetails(Collections.singletonList(detail));
//            doctorWarehouseStockWriteService.in(inDto);
//        } else if (WarehouseMaterialHandleType.TRANSFER_IN.getValue() == handle.getType()
//                || WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == handle.getType()) {
//
//            Response<DoctorWarehouseMaterialHandle> otherTransferHandleResponse = doctorWarehouseMaterialHandleReadService.findById(handle.getOtherTrasnferHandleId());
//            if (!otherTransferHandleResponse.isSuccess())
//                throw new JsonResponseException(otherTransferHandleResponse.getError());
//
//            Long transferOutWarehouseId, transferInWarehouseId;
//            if (WarehouseMaterialHandleType.TRANSFER_IN.getValue() == handle.getType()) {
//                transferOutWarehouseId = handle.getWarehouseId();
//                transferInWarehouseId = otherTransferHandleResponse.getResult().getWarehouseId();
//            } else {
//                transferOutWarehouseId = otherTransferHandleResponse.getResult().getWarehouseId();
//                transferInWarehouseId = handle.getWarehouseId();
//            }
//
//            WarehouseStockTransferDto transferDto = new WarehouseStockTransferDto();
//            transferDto.setFarmId(handle.getFarmId());
//            transferDto.setWarehouseId(transferOutWarehouseId);
//            transferDto.setHandleDate(new Date());
//
//            WarehouseStockTransferDto.WarehouseStockTransferDetail detail = new WarehouseStockTransferDto.WarehouseStockTransferDetail();
//            detail.setMaterialId(handle.getMaterialId());
//            detail.setQuantity(handle.getQuantity());
//            detail.setTransferInWarehouseId(transferInWarehouseId);
//            transferDto.setDetails(Collections.singletonList(detail));
//            doctorWarehouseStockWriteService.transfer(transferDto);
//        } else if (WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue() == handle.getType()
//                || WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue() == handle.getType()) {
//
//            Response<List<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.listMergeVendor(DoctorWarehouseStock.builder()
//                    .warehouseId(handle.getWarehouseId())
//                    .materialId(handle.getMaterialId())
//                    .build());
//            if (!stockResponse.isSuccess())
//                throw new JsonResponseException(stockResponse.getError());
//
//            if (null == stockResponse.getResult() || stockResponse.getResult().isEmpty())
//                throw new JsonResponseException("stock.not.found");
//
//
//            BigDecimal newQuantity;
//            if (WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue() == handle.getType()) {
//                newQuantity = stockResponse.getResult().get(0).getQuantity().multiply(handle.getQuantity());
//            } else
//                newQuantity = stockResponse.getResult().get(0).getQuantity().add(handle.getQuantity());
//
//            WarehouseStockInventoryDto inventoryDto = new WarehouseStockInventoryDto();
//            inventoryDto.setFarmId(handle.getFarmId());
//            inventoryDto.setHandleDate(new Date());
//            inventoryDto.setWarehouseId(handle.getWarehouseId());
//
//            WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail = new WarehouseStockInventoryDto.WarehouseStockInventoryDetail();
//            detail.setMaterialId(handle.getMaterialId());
//            detail.setQuantity(newQuantity);
//            inventoryDto.setDetails(Collections.singletonList(detail));
//            doctorWarehouseStockWriteService.inventory(inventoryDto);
//        }
//
//        handle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
//        doctorWarehouseMaterialHandleWriteService.update(handle);
        Response<Boolean> response = doctorWarehouseMaterialHandleWriteService.delete(id);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
        return true;
    }


}

