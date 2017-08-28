package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseStockStatisticsVo;
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
@RequestMapping("api/doctor/warehouseV2/stock")
public class StockController {


    @RpcConsumer
    private DoctorWarehouseStockWriteService doctorWarehouseStockWriteService;

    @RpcConsumer
    private DoctorWarehouseStockReadService doctorWarehouseStockReadService;

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    @RpcConsumer
    private DoctorGroupWebService doctorGroupWebService;

    @RpcConsumer
    private DoctorWarehouseReportReadService doctorWarehouseReportReadService;

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

        Calendar now = Calendar.getInstance();

        DoctorWarehouseStock stockCriteria = new DoctorWarehouseStock();
        stockCriteria.setWarehouseId(warehouseId);
        if (StringUtils.isNotBlank(materialName))
            stockCriteria.setMaterialName(materialName);
        Response<Paging<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.pagingMergeVendor(pageNo, pageSize, stockCriteria);
        if (!stockResponse.isSuccess())
            throw new JsonResponseException(stockResponse.getError());
        if (null == stockResponse.getResult().getData())
            throw new JsonResponseException("stock.not.found");

        Paging<WarehouseStockStatisticsVo> result = new Paging<>();
        result.setTotal(stockResponse.getResult().getTotal());
        List<WarehouseStockStatisticsVo> vos = new ArrayList<>(stockResponse.getResult().getData().size());
        stockResponse.getResult().getData().forEach(stock -> {
            Response<AmountAndQuantityDto> balanceResponse = doctorWarehouseReportReadService.countMaterialBalance(warehouseId, stock.getMaterialId());
            if (!balanceResponse.isSuccess())
                throw new JsonResponseException(balanceResponse.getError());
            Response<WarehouseStockStatisticsDto> statisticsResponse = doctorWarehouseReportReadService.countMaterialHandleByMaterial(warehouseId, stock.getMaterialId(), now,
                    WarehouseMaterialHandleType.IN,
                    WarehouseMaterialHandleType.OUT,
                    WarehouseMaterialHandleType.TRANSFER_IN,
                    WarehouseMaterialHandleType.TRANSFER_OUT);
            if (!statisticsResponse.isSuccess())
                throw new JsonResponseException(statisticsResponse.getError());

            WarehouseStockStatisticsVo vo = new WarehouseStockStatisticsVo();
            vo.setId(stock.getId());
            vo.setMaterialId(stock.getMaterialId());
            vo.setMaterialName(stock.getMaterialName());
            vo.setUnit(stock.getUnit());

            vo.setOutQuantity(statisticsResponse.getResult().getOut().getQuantity());
            vo.setOutAmount(statisticsResponse.getResult().getOut().getAmount());
            vo.setInAmount(statisticsResponse.getResult().getIn().getAmount());
            vo.setInQuantity(statisticsResponse.getResult().getIn().getQuantity());
            vo.setTransferInAmount(statisticsResponse.getResult().getTransferIn().getAmount());
            vo.setTransferInQuantity(statisticsResponse.getResult().getTransferIn().getQuantity());
            vo.setTransferOutAmount(statisticsResponse.getResult().getTransferOut().getAmount());
            vo.setTransferOutQuantity(statisticsResponse.getResult().getTransferOut().getQuantity());

            vo.setBalanceQuantity(balanceResponse.getResult().getQuantity());
            vo.setBalanceAmount(balanceResponse.getResult().getAmount());
            vos.add(vo);
        });
        result.setData(vos);

        return result;
    }
}
