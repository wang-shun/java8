package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseReportReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteService;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseStockStatisticsVo;
import io.terminus.parana.user.model.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    @RpcConsumer
    private DoctorGroupWebService doctorGroupWebService;

    @RpcConsumer
    private DoctorUserProfileReadService doctorUserProfileReadService;

    @RpcConsumer
    private DoctorWarehouseReportReadService doctorWarehouseReportReadService;
    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;

    @RequestMapping(method = RequestMethod.PUT, value = "in")
    public boolean in(@RequestBody @Validated WarehouseStockInDto stockIn, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Response<UserProfile> userResponse = doctorUserProfileReadService.findProfileByUserId(stockIn.getOperatorId());
        if (!userResponse.isSuccess())
            throw new JsonResponseException(userResponse.getError());
        if (null == userResponse.getResult())
            throw new JsonResponseException("user.not.found");
        stockIn.setOperatorName(userResponse.getResult().getRealName());


        Response<Boolean> response = doctorWarehouseStockWriteService.in(stockIn);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());

        return true;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "out")
    public boolean out(@RequestBody @Validated WarehouseStockOutDto stockOut, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());


        Response<UserProfile> userResponse = doctorUserProfileReadService.findProfileByUserId(stockOut.getOperatorId());
        if (!userResponse.isSuccess())
            throw new JsonResponseException(userResponse.getError());
        if (null == userResponse.getResult())
            throw new JsonResponseException("user.not.found");
        stockOut.setOperatorName(userResponse.getResult().getRealName());

        stockOut.getDetails().forEach(detail -> {
            Response<String> realNameResponse = doctorGroupWebService.findRealName(detail.getApplyStaffId());
            if (!realNameResponse.isSuccess())
                throw new JsonResponseException(realNameResponse.getError());
            detail.setApplyStaffName(realNameResponse.getResult());
        });

        Response<Boolean> response = doctorWarehouseStockWriteService.out(stockOut);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());

        return true;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "inventory")
    public boolean inventory(@RequestBody @Validated WarehouseStockInventoryDto stockInventory, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());


        Response<UserProfile> userResponse = doctorUserProfileReadService.findProfileByUserId(stockInventory.getOperatorId());
        if (!userResponse.isSuccess())
            throw new JsonResponseException(userResponse.getError());
        if (null == userResponse.getResult())
            throw new JsonResponseException("user.not.found");
        stockInventory.setOperatorName(userResponse.getResult().getRealName());

        Response<Boolean> response = doctorWarehouseStockWriteService.inventory(stockInventory);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());

        return true;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "transfer")
    public boolean transfer(@RequestBody @Validated WarehouseStockTransferDto stockTransfer, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Response<UserProfile> userResponse = doctorUserProfileReadService.findProfileByUserId(stockTransfer.getOperatorId());
        if (!userResponse.isSuccess())
            throw new JsonResponseException(userResponse.getError());
        if (null == userResponse.getResult())
            throw new JsonResponseException("user.not.found");
        stockTransfer.setOperatorName(userResponse.getResult().getRealName());

        Response<Boolean> response = doctorWarehouseStockWriteService.transfer(stockTransfer);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());

        return true;
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public boolean delete(@PathVariable Long id) {
        Response<Boolean> response = doctorWarehouseStockWriteService.delete(id);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
        return true;
    }


    @RequestMapping(method = RequestMethod.GET)
    public Paging<WarehouseStockStatisticsVo> paging(@RequestParam Long warehouseId,
                                                     @RequestParam(required = false) String materialName,
                                                     @RequestParam(required = false) Integer pageNo,
                                                     @RequestParam(required = false) Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);
        if (StringUtils.isNotBlank(materialName)) {
            params.put("materialNameLike", materialName);
        }

        Response<Paging<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.paging(pageNo, pageSize, params);

        if (!stockResponse.isSuccess())
            throw new JsonResponseException(stockResponse.getError());
        if (null == stockResponse.getResult().getData())
            throw new JsonResponseException("stock.not.found");

        Calendar now = Calendar.getInstance();


        Map<Long, List<DoctorWarehouseSku>> skuMap = RespHelper.or500(doctorWarehouseSkuReadService.findByIds(stockResponse.getResult().getData().stream().map(DoctorWarehouseStock::getSkuId).collect(Collectors.toList()))).stream().collect(Collectors.groupingBy(DoctorWarehouseSku::getId));

        Paging<WarehouseStockStatisticsVo> result = new Paging<>();
        result.setTotal(stockResponse.getResult().getTotal());
        List<WarehouseStockStatisticsVo> vos = new ArrayList<>(stockResponse.getResult().getData().size());
        stockResponse.getResult().getData().forEach(stock -> {

//            if (!skuMap.containsKey(stock.getSkuId()))
//                throw new InvalidException("warehouse.sku.not.found", stock.getSkuId());

            Response<AmountAndQuantityDto> balanceResponse = doctorWarehouseReportReadService.countMaterialBalance(warehouseId, stock.getSkuId());
            if (!balanceResponse.isSuccess())
                throw new JsonResponseException(balanceResponse.getError());
            Response<WarehouseStockStatisticsDto> statisticsResponse = doctorWarehouseReportReadService.countMaterialHandleByMaterial(warehouseId, stock.getSkuId(), now,
                    WarehouseMaterialHandleType.IN,
                    WarehouseMaterialHandleType.OUT,
                    WarehouseMaterialHandleType.INVENTORY_PROFIT,
                    WarehouseMaterialHandleType.INVENTORY_DEFICIT,
                    WarehouseMaterialHandleType.TRANSFER_IN,
                    WarehouseMaterialHandleType.TRANSFER_OUT,
                    WarehouseMaterialHandleType.FORMULA_IN,
                    WarehouseMaterialHandleType.FORMULA_OUT);
            if (!statisticsResponse.isSuccess())
                throw new JsonResponseException(statisticsResponse.getError());
//            DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(stock.getSkuId()));


            DoctorWarehouseSku sku = skuMap.containsKey(stock.getSkuId()) ? skuMap.get(stock.getSkuId()).get(0) : null;

            WarehouseStockStatisticsVo vo = new WarehouseStockStatisticsVo();
            vo.setId(stock.getId());
            vo.setFarmId(stock.getFarmId());
            vo.setWarehouseId(stock.getWarehouseId());
            vo.setWarehouseName(stock.getWarehouseName());
            vo.setWarehouseType(stock.getWarehouseType());
            vo.setMaterialId(stock.getSkuId());
            vo.setMaterialName(stock.getSkuName());

            if (null != sku) {
                vo.setUnit(sku.getUnit());
                vo.setCode(sku.getCode());
                vo.setVendorName(sku.getVendorName());
                vo.setSpecification(sku.getSpecification());
            }

//            vo.setOutQuantity(statisticsResponse.getResult().getOut().getQuantity());
//            vo.setOutAmount(statisticsResponse.getResult().getOut().getAmount());
//            vo.setInAmount(statisticsResponse.getResult().getIn().getAmount());
//            vo.setInQuantity(statisticsResponse.getResult().getIn().getQuantity());
//            vo.setTransferInAmount(statisticsResponse.getResult().getTransferIn().getAmount());
//            vo.setTransferInQuantity(statisticsResponse.getResult().getTransferIn().getQuantity());
//            vo.setTransferOutAmount(statisticsResponse.getResult().getTransferOut().getAmount());
//            vo.setTransferOutQuantity(statisticsResponse.getResult().getTransferOut().getQuantity());
            vo.setInAmount(statisticsResponse.getResult().getIn().getAmount()
                    + statisticsResponse.getResult().getInventoryProfit().getAmount()
                    + statisticsResponse.getResult().getTransferIn().getAmount()
                    + statisticsResponse.getResult().getFormulaIn().getAmount());
            vo.setInQuantity(statisticsResponse.getResult().getIn().getQuantity()
                    .add(statisticsResponse.getResult().getInventoryProfit().getQuantity())
                    .add(statisticsResponse.getResult().getTransferIn().getQuantity())
                    .add(statisticsResponse.getResult().getFormulaIn().getQuantity()));

            vo.setOutAmount(statisticsResponse.getResult().getOut().getAmount()
                    + statisticsResponse.getResult().getInventoryDeficit().getAmount()
                    + statisticsResponse.getResult().getTransferOut().getAmount()
                    + statisticsResponse.getResult().getFormulaOut().getAmount());
            vo.setOutQuantity(statisticsResponse.getResult().getOut().getQuantity()
                    .add(statisticsResponse.getResult().getInventoryDeficit().getQuantity())
                    .add(statisticsResponse.getResult().getTransferOut().getQuantity())
                    .add(statisticsResponse.getResult().getFormulaOut().getQuantity()));

            vo.setBalanceQuantity(balanceResponse.getResult().getQuantity());
            vo.setBalanceAmount(balanceResponse.getResult().getAmount());
            vos.add(vo);
        });
        result.setData(vos);

        return result;
    }


}
