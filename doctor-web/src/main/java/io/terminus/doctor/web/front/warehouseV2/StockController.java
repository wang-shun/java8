package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.front.controller.UserProfiles;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseStockStatisticsVo;
import io.terminus.parana.user.model.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by sunbo@terminus.io on 2017/8/20.
 */
@RestController
@RequestMapping("api/doctor/warehouse/stock")
public class StockController {

    //TODO 单据编号。猪厂下，年月日是分秒毫秒
    //TODO 物料编号（根据物料+厂家带出来）。记录的时候需要校验

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

        Calendar now = Calendar.getInstance();

        if (StringUtils.isBlank(materialName)) {
            //如果传入的是空，那么将会应用上这个查询条件，导致查不出数据
            materialName = null;
        }

        Response<Paging<DoctorWarehouseStock>> stockResponse = doctorWarehouseStockReadService.paging(pageNo, pageSize, DoctorWarehouseStock.builder()
                .warehouseId(warehouseId)
                .materialName(materialName)
                .build());

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
