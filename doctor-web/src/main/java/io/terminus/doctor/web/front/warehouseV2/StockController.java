package io.terminus.doctor.web.front.warehouseV2;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehouseSkuStatus;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseStockStatisticsVo;
import io.terminus.parana.user.model.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
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
    @RpcConsumer
    private DoctorWareHouseReadService doctorWareHouseReadService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorWarehouseVendorReadService doctorWarehouseVendorReadService;

    @RpcConsumer
    private DoctorBasicReadService doctorBasicReadService;
    @RpcConsumer
    private DoctorBarnReadService doctorBarnReadService;
    @RpcConsumer
    private DoctorOrgReadService doctorOrgReadService;


    @Autowired
    private LockRegistry lockRegistry;

    /**
     * 采购入库
     *
     * @param stockIn
     * @param errors
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "in")
    public Long in(@RequestBody @Validated(AbstractWarehouseStockDetail.StockOtherValid.class) WarehouseStockInDto stockIn, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Response<UserProfile> userResponse = doctorUserProfileReadService.findProfileByUserId(stockIn.getOperatorId());
        if (!userResponse.isSuccess())
            throw new JsonResponseException(userResponse.getError());
        if (null == userResponse.getResult())
            throw new JsonResponseException("user.not.found");
        stockIn.setOperatorName(userResponse.getResult().getRealName());

        Response<Long> response = doctorWarehouseStockWriteService.in(stockIn);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());

        return response.getResult();
    }

    /**
     * 生产领料出库
     *
     * @param stockOut
     * @param errors
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "out")
    public Long out(@RequestBody @Validated(AbstractWarehouseStockDetail.StockOtherValid.class) WarehouseStockOutDto stockOut, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Response<UserProfile> userResponse = doctorUserProfileReadService.findProfileByUserId(stockOut.getOperatorId());
        if (!userResponse.isSuccess())
            throw new JsonResponseException(userResponse.getError());
        if (null == userResponse.getResult())
            throw new JsonResponseException("user.not.found");
        stockOut.setOperatorName(userResponse.getResult().getRealName());


        DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(stockOut.getFarmId()));
        if (null == farm)
            throw new JsonResponseException("farm.not.found");
        stockOut.setOrgId(farm.getOrgId());

        stockOut.getDetails().forEach(detail -> {
            Response<String> realNameResponse = doctorGroupWebService.findRealName(detail.getApplyStaffId());
            if (!realNameResponse.isSuccess())
                throw new JsonResponseException(realNameResponse.getError());
            detail.setApplyStaffName(realNameResponse.getResult());

            DoctorBarn barn = RespHelper.orServEx(doctorBarnReadService.findBarnById(detail.getApplyPigBarnId()));
            if (null == barn)
                throw new InvalidException("barn.not.null", detail.getApplyPigBarnId());
            detail.setPigType(barn.getPigType());
        });

        Response<Long> response = doctorWarehouseStockWriteService.out(stockOut);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());

        return response.getResult();
    }

    /**
     * 盘点
     *
     * @param stockInventory
     * @param errors
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "inventory")
    public Long inventory(@RequestBody @Validated(AbstractWarehouseStockDetail.StockInventoryValid.class) WarehouseStockInventoryDto stockInventory,
                          Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());
        if (null == stockInventory.getStockHandleId() && stockInventory.getDetails().isEmpty())
            throw new JsonResponseException("stock.detail.empty");

        Collections.reverse(stockInventory.getDetails());//倒序，最新的覆盖替换老的
        List<WarehouseStockInventoryDto.WarehouseStockInventoryDetail> removedRepeat = new ArrayList<>();
        for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail : stockInventory.getDetails()) {
            boolean existed = false;
            for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail1 : removedRepeat) {
                if (detail.getMaterialId().equals(detail1.getMaterialId())) {
                    existed = true;
                    break;
                }
            }
            if (!existed)
                removedRepeat.add(detail);
        }
        stockInventory.setDetails(removedRepeat);

        Response<UserProfile> userResponse = doctorUserProfileReadService.findProfileByUserId(stockInventory.getOperatorId());
        if (!userResponse.isSuccess())
            throw new JsonResponseException(userResponse.getError());
        if (null == userResponse.getResult())
            throw new JsonResponseException("user.not.found");
        stockInventory.setOperatorName(userResponse.getResult().getRealName());

        Response<Long> response = doctorWarehouseStockWriteService.inventory(stockInventory);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());

        return response.getResult();
    }

    /**
     * 调拨
     *
     * @param stockTransfer
     * @param errors
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "transfer")
    public Long transfer(@RequestBody @Validated(AbstractWarehouseStockDetail.StockOtherValid.class) WarehouseStockTransferDto stockTransfer, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Collections.reverse(stockTransfer.getDetails());
        List<WarehouseStockTransferDto.WarehouseStockTransferDetail> removedRepeat = new ArrayList<>();
        for (WarehouseStockTransferDto.WarehouseStockTransferDetail detail : stockTransfer.getDetails()) {
            boolean existed = false;
            for (WarehouseStockTransferDto.WarehouseStockTransferDetail detail1 : removedRepeat) {
                if (detail.getMaterialId().equals(detail1.getMaterialId())
                        && detail.getTransferInWarehouseId().equals(detail1.getTransferInWarehouseId())
                        && detail.getQuantity().compareTo(detail1.getQuantity()) == 0) {
                    existed = true;
                    break;
                }
            }
            if (!existed)
                removedRepeat.add(detail);
        }
        stockTransfer.setDetails(removedRepeat);

        Response<UserProfile> userResponse = doctorUserProfileReadService.findProfileByUserId(stockTransfer.getOperatorId());
        if (!userResponse.isSuccess())
            throw new JsonResponseException(userResponse.getError());
        if (null == userResponse.getResult())
            throw new JsonResponseException("user.not.found");
        stockTransfer.setOperatorName(userResponse.getResult().getRealName());

        Response<Long> response = doctorWarehouseStockWriteService.transfer(stockTransfer);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());

        return response.getResult();
    }

    /**
     * 删除库存明细
     *
     * @param id
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public boolean delete(@PathVariable Long id) {
        Response<Boolean> response = doctorWarehouseStockWriteService.delete(id);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
        return true;
    }


    /**
     * 查询库存明细
     *
     * @param warehouseId
     * @param orgId
     * @param materialName
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public Paging<WarehouseStockStatisticsVo> paging(@RequestParam Long warehouseId,
                                                     @RequestParam(required = false) Long orgId,
                                                     @RequestParam(required = false) String materialName,
                                                     @RequestParam(required = false) Integer pageNo,
                                                     @RequestParam(required = false) Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        params.put("warehouseId", warehouseId);

        if (StringUtils.isNotBlank(materialName)) {

            if (null == orgId) {
                DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(warehouseId));
                if (null == wareHouse)
                    throw new JsonResponseException("warehouse.not.found");
                DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(wareHouse.getFarmId()));
                if (null == farm)
                    throw new JsonResponseException("farm.not.found");
                orgId = farm.getOrgId();
            }

            Map<String, Object> skuParams = new HashMap<>();
            skuParams.put("orgId", orgId);
            skuParams.put("status", WarehouseSkuStatus.NORMAL.getValue());
            skuParams.put("nameOrSrmLike", materialName);
            List<Long> skuIds = RespHelper.or500(doctorWarehouseSkuReadService.list(skuParams)).stream().map(DoctorWarehouseSku::getId).collect(Collectors.toList());
            if (skuIds.isEmpty())
                return Paging.empty();
            params.put("skuIds", skuIds);
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

            AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseReportReadService.countMaterialBalance(warehouseId, stock.getSkuId()));
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

                DoctorBasic unit = RespHelper.or500(doctorBasicReadService.findBasicById(Long.parseLong(sku.getUnit())));
                if (null != unit)
                    vo.setUnit(unit.getName());
                vo.setCode(sku.getCode());
                vo.setMaterialName(sku.getName());
                vo.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(sku.getVendorId())));
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

//            vo.setBalanceQuantity(balance.getQuantity());
            vo.setBalanceQuantity(stock.getQuantity());
            vo.setBalanceAmount(balance.getAmount());
            vos.add(vo);
        });
        result.setData(vos);

        return result;
    }


}
