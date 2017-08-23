package io.terminus.doctor.web.front.new_warehouse;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.WarehouseStockInDto;
import io.terminus.doctor.basic.dto.WarehouseStockInventoryDto;
import io.terminus.doctor.basic.dto.WarehouseStockOutDto;
import io.terminus.doctor.basic.dto.WarehouseStockTransferDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandlerType;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseStockVo;
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
            Response<String> realNameResponse = doctorGroupWebService.findRealName(detail.getApplyPersonId());
            if (!realNameResponse.isSuccess())
                throw new JsonResponseException(realNameResponse.getError());
            detail.setApplyPersonName(realNameResponse.getResult());
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


    @RequestMapping(method = RequestMethod.GET)
    public List<WarehouseStockVo> findStock(@RequestParam Long warehouseId, @RequestParam(required = false) String materialName) {


        DoctorWarehouseStock stockCriteria = new DoctorWarehouseStock();
        stockCriteria.setWarehouseId(warehouseId);
        if (StringUtils.isNotBlank(materialName))
            stockCriteria.setMaterialName(materialName);
        Response<List<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.list(stockCriteria);
        if (!stockResponse.isSuccess())
            throw new JsonResponseException(stockResponse.getError());
        if (null == stockResponse.getResult() || stockResponse.getResult().isEmpty())
            throw new JsonResponseException("stock.not.found");


        Map<String, WarehouseStockVo> vos = new HashMap<>();
        //合并不同供应商
        stockResponse.getResult().forEach(stock -> {

            if (!vos.containsKey(stock.getMaterialName())) {
                WarehouseStockVo vo = new WarehouseStockVo();
                vo.setMaterialName(stock.getMaterialName());
                vo.setUnit(stock.getUnit());
                vo.setBalanceQuantity(stock.getQuantity());
                vos.put(stock.getMaterialName(), vo);
            } else {
                BigDecimal quantity = vos.get(stock.getMaterialName()).getBalanceQuantity();
                vos.get(stock.getMaterialName()).setBalanceQuantity(quantity.add(stock.getQuantity()));
            }
        });


        //本月出库记录
        Calendar now = Calendar.getInstance();
        DoctorWarehouseMaterialHandle handleCriteria = new DoctorWarehouseMaterialHandle();
        handleCriteria.setHandleYear(now.get(Calendar.YEAR));
        handleCriteria.setHandleMonth(now.get(Calendar.MONTH) + 1);
        handleCriteria.setType(WarehouseMaterialHandlerType.OUT.getValue());
        Response<List<DoctorWarehouseMaterialHandle>> outHandleResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
        if (!outHandleResponse.isSuccess())
            throw new JsonResponseException(outHandleResponse.getError());


        //本月出库记录
        handleCriteria.setType(WarehouseMaterialHandlerType.IN.getValue());
        Response<List<DoctorWarehouseMaterialHandle>> inHandleResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
        if (!inHandleResponse.isSuccess())
            throw new JsonResponseException(inHandleResponse.getError());


        return new ArrayList<>(vos.values());
    }
}
