package io.terminus.doctor.web.front.new_warehouse;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.WarehouseStockInDto;
import io.terminus.doctor.basic.dto.WarehouseStockInventoryDto;
import io.terminus.doctor.basic.dto.WarehouseStockOutDto;
import io.terminus.doctor.basic.dto.WarehouseStockTransferDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseStockStatisticsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sunbo@terminus.io on 2017/8/20.
 */
@RestController
@RequestMapping("api/doctor/warehouse/stock")
public class StockController {


    @RpcConsumer
    private DoctorWarehouseStockWriteService doctorWarehouseStockWriteService;

    @RpcConsumer
    private DoctorWarehouseStockReadService doctorWarehouseStockReadService;

    @RpcConsumer
    private DoctorWarehousePurchaseReadService doctorWarehousePurchaseReadService;

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;

    @RpcConsumer
    private DoctorWarehouseMaterialStockReadService doctorWarehouseMaterialStockReadService;
    @RpcConsumer
    private DoctorGroupWebService doctorGroupWebService;

    @RequestMapping(method = RequestMethod.PUT, value = "in")
    public void in(@RequestBody @Validated WarehouseStockInDto stockIn, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());
        Response<Boolean> response = doctorWarehouseStockWriteService.in(stockIn);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "out")
    public void out(@RequestBody @Validated WarehouseStockOutDto stockOut, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());


        stockOut.getDetails().forEach(detail -> {
            Response<String> realNameResponse = doctorGroupWebService.findRealName(detail.getApplyStaffId());
            if (!realNameResponse.isSuccess())
                throw new JsonResponseException(realNameResponse.getError());
            detail.setApplyStaffName(realNameResponse.getResult());
        });

        Response<Boolean> response = doctorWarehouseStockWriteService.out(stockOut);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "inventory")
    public void inventory(@RequestBody @Validated WarehouseStockInventoryDto stockInventory, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Response<Boolean> response = doctorWarehouseStockWriteService.inventory(stockInventory);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "transfer")
    public void transfer(@RequestBody @Validated WarehouseStockTransferDto stockTransfer, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Response<Boolean> response = doctorWarehouseStockWriteService.transfer(stockTransfer);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public void delete(@PathVariable Long id) {
        Response<Boolean> response = doctorWarehouseStockWriteService.delete(id);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
    }


    @RequestMapping(method = RequestMethod.GET)
    public Paging<WarehouseStockStatisticsVo> paging(@RequestParam Long warehouseId,
                                                     @RequestParam(required = false) String materialName,
                                                     @RequestParam(required = false) Integer pageNo,
                                                     @RequestParam(required = false) Integer pageSize) {

        //本月出库记录
        Calendar now = Calendar.getInstance();
        DoctorWarehouseMaterialHandle handleCriteria = new DoctorWarehouseMaterialHandle();
        handleCriteria.setHandleYear(now.get(Calendar.YEAR));
        handleCriteria.setHandleMonth(now.get(Calendar.MONTH) + 1);
        handleCriteria.setType(WarehouseMaterialHandleType.OUT.getValue());
        handleCriteria.setWarehouseId(warehouseId);
        Response<List<DoctorWarehouseMaterialHandle>> outHandleResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
        if (!outHandleResponse.isSuccess())
            throw new JsonResponseException(outHandleResponse.getError());
        Map<Long/*materialId*/, BigDecimal> totalOutQuantity = new HashMap<>();
        Map<Long, Long> totalOutAmount = new HashMap<>();
        for (DoctorWarehouseMaterialHandle handle : outHandleResponse.getResult()) {
            if (!totalOutQuantity.containsKey(handle.getMaterialId()))
                totalOutQuantity.put(handle.getMaterialId(), handle.getQuantity());
            else {
                BigDecimal quantity = totalOutQuantity.get(handle.getMaterialId());
                totalOutQuantity.put(handle.getMaterialId(), quantity.add(handle.getQuantity()));
            }
            if (!totalOutAmount.containsKey(handle.getMaterialId()))
                totalOutAmount.put(handle.getMaterialId(), handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue());
            else {
                long amount = totalOutAmount.get(handle.getMaterialId());
                totalOutAmount.put(handle.getMaterialId(), handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue() + amount);
            }
        }


        //本月入库记录
        handleCriteria.setType(WarehouseMaterialHandleType.IN.getValue());
        Response<List<DoctorWarehouseMaterialHandle>> inHandleResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
        if (!inHandleResponse.isSuccess())
            throw new JsonResponseException(inHandleResponse.getError());
        Map<Long/*materialId*/, BigDecimal> totalInQuantity = new HashMap<>();
        Map<Long/*materialId*/, Long> totalInAmount = new HashMap<>();
        for (DoctorWarehouseMaterialHandle handle : inHandleResponse.getResult()) {
            if (!totalInQuantity.containsKey(handle.getMaterialId()))
                totalInQuantity.put(handle.getMaterialId(), handle.getQuantity());
            else {
                BigDecimal quantity = totalInQuantity.get(handle.getMaterialId());
                totalInQuantity.put(handle.getMaterialId(), quantity.add(handle.getQuantity()));
            }
            if (!totalInAmount.containsKey(handle.getMaterialId()))
                totalInAmount.put(handle.getMaterialId(), handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue());
            else {
                long amount = totalInAmount.get(handle.getMaterialId());
                totalInAmount.put(handle.getMaterialId(), handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue() + amount);
            }
        }


        DoctorWarehouseStock stockCriteria = new DoctorWarehouseStock();
        stockCriteria.setWarehouseId(warehouseId);
        if (StringUtils.isNotBlank(materialName))
            stockCriteria.setMaterialName(materialName);
        Response<Paging<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.pagingMergeVendor(pageNo, pageSize, stockCriteria);
        if (!stockResponse.isSuccess())
            throw new JsonResponseException(stockResponse.getError());
        if (null == stockResponse.getResult().getData() || stockResponse.getResult().getData().isEmpty())
            throw new JsonResponseException("stock.not.found");

        Paging<WarehouseStockStatisticsVo> result = new Paging<>();
        result.setTotal(stockResponse.getResult().getTotal());
        List<WarehouseStockStatisticsVo> vos = new ArrayList<>(stockResponse.getResult().getData().size());
        stockResponse.getResult().getData().forEach(stock -> {
            WarehouseStockStatisticsVo vo = new WarehouseStockStatisticsVo();
            vo.setMaterialId(stock.getMaterialId());
            vo.setMaterialName(stock.getMaterialName());
            vo.setUnit(stock.getUnit());

            vo.setOutQuantity(totalOutQuantity.get(stock.getMaterialId()));
            if (!totalOutAmount.containsKey(stock.getMaterialId()))
                vo.setOutAmount(0);
            else
                vo.setOutAmount(totalOutAmount.get(stock.getMaterialId()));
            vo.setInQuantity(totalInQuantity.get(stock.getMaterialId()));
            if (!totalInAmount.containsKey(stock.getMaterialId()))
                vo.setInAmount(0);
            else
                vo.setInAmount(totalInAmount.get(stock.getMaterialId()));

            vo.setBalanceQuantity(stock.getQuantity());
            vo.setBalanceAmount(vo.getInAmount() - vo.getOutAmount());
            vos.add(vo);
        });
        result.setData(vos);

        return result;
    }
}
