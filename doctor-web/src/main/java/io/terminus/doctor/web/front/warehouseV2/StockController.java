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
import io.terminus.doctor.basic.model.warehouseV2.*;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorWareHouseReadService;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.front.warehouseV2.dto.StockDto;
import io.terminus.doctor.web.front.warehouseV2.dto.WarehouseDto;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseStockStatisticsVo;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserReadService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * 库存
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

    @RpcConsumer
    private DoctorWarehouseSettlementService doctorWarehouseSettlementService;

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

        setOrgId(stockIn);

        //是否该公司正在结算中
        if (doctorWarehouseSettlementService.isUnderSettlement(stockIn.getOrgId()))
            throw new JsonResponseException("under.settlement");

        //会计年月
        Date settlementDate = doctorWarehouseSettlementService.getSettlementDate(stockIn.getHandleDate().getTime());
        //会计年月已经结算后，不允许新增或编辑单据
        if (doctorWarehouseSettlementService.isSettled(stockIn.getOrgId(), settlementDate))
            throw new JsonResponseException("already.settlement");

        setOperatorName(stockIn);
        stockIn.setSettlementDate(settlementDate);
        Calendar handleDateWithTime = Calendar.getInstance();
        handleDateWithTime.set(stockIn.getHandleDate().get(Calendar.YEAR), stockIn.getHandleDate().get(Calendar.MONTH), stockIn.getHandleDate().get(Calendar.DAY_OF_MONTH));
        stockIn.setHandleDate(handleDateWithTime);

        return RespHelper.or500(doctorWarehouseStockWriteService.in(stockIn));
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

        setOperatorName(stockOut);

        setOrgId(stockOut);

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

        return RespHelper.or500(doctorWarehouseStockWriteService.out(stockOut));
    }


    /**
     * 退料入库
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "refund")
    public Long refund() {
        //TODO 领料入库
        return 0L;
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

        setOperatorName(stockInventory);

        return RespHelper.or500(doctorWarehouseStockWriteService.inventory(stockInventory));
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

        setOperatorName(stockTransfer);

        return RespHelper.or500(doctorWarehouseStockWriteService.transfer(stockTransfer));
    }


    private void setOperatorName(AbstractWarehouseStockDto stockDto) {
        UserProfile user = RespHelper.or500(doctorUserProfileReadService.findProfileByUserId(stockDto.getOperatorId()));
        if (null == user)
            throw new JsonResponseException("user.not.found");
        stockDto.setOperatorName(user.getRealName());
    }

    private void setOrgId(AbstractWarehouseStockDto stockDto) {
        DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(stockDto.getFarmId()));
        if (null == farm)
            throw new JsonResponseException("farm.not.found");
        stockDto.setOrgId(farm.getOrgId());
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


    @Autowired
    private UserReadService userReadService;

    /***********    2018/04/11     *************/
    /**
     * 添加单据明细,事件类型就入库、出库
     *
     * @param stockDtoList
     * @return
     */
    @RequestMapping(value = "/createStock", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean create(@RequestBody @Valid List<StockDto> stockDtoList) {

        if (CollectionUtils.isEmpty(stockDtoList)) {
            throw new JsonResponseException("stock.stockDtoList.not.null");
        }

        StockDto stockDto = stockDtoList.get(0);
//        Response<User> currentUserResponse = userReadService.findById(stockDto.getOperatorId());
//        // 得到creatorId,creatorName
//        User currentUser = currentUserResponse.getResult();
//        if (null == currentUser)
//            throw new JsonResponseException("stock.operator.not.exist");

        //生成流水号(日期格式的时间戳,精确到毫秒)
        String seq = DateUtil.formatDateStringForTimeorder(new Date());
        int handleSubType = stockDto.getHandleSubType();

        /**
         * 单据主表
         */
        DoctorWarehouseStockHandle doctorWarehouseStockHandle =
                DoctorWarehouseStockHandle.builder()
                        .farmId(stockDto.getFarmId()) //猪场Id
                        .warehouseId(stockDto.getWarehouseId()) //仓库id
                        .warehouseName(stockDto.getWarehouseName()) //仓库名称
                        .serialNo(seq) //流水号
                        .handleDate(stockDto.getHandleDate()) //处理日期
                        .handleSubType(handleSubType) //事件子类型
                        .handleType(stockDto.getHandleType()) //事件类型
                        //.operatorName(currentUser.getName()) //创建人名
                        //.operatorId(currentUser.getId()) //创建人id
                        .warehouseType(stockDto.getWarehouseType()) //仓库类型
                        .build();

        Date handlerDate = stockDto.getHandleDate();

        List<DoctorWarehouseMaterialHandle> doctorWarehouseMaterialHandleList =
                new ArrayList<>();

        //支持添加多条单据数据
        for (StockDto sd : stockDtoList) {
            /**
             * 单据明细表
             */
            DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle =
                    DoctorWarehouseMaterialHandle.builder()
                            .farmId(sd.getFarmId())
                            .warehouseId(sd.getWarehouseId())
                            .warehouseType(sd.getWarehouseType())
                            .warehouseName(sd.getWarehouseName())
                            .vendorName(sd.getVendorName())
                            .materialId(sd.getMaterialId())
                            .materialName(sd.getMaterialName())
                            .type(handleSubType)
                            .unitPrice(sd.getUnitPrice())
                            .unit(sd.getUnit())
                            .deleteFlag(1) //表示未删除
                            .beforeStockQuantity(sd.getBeforeStockQuantity()) //当前数量
                            .quantity(sd.getQuantity())
                            .handleDate(handlerDate) //处理日期
                            .handleYear(DateUtil.getYearForDate(handlerDate))
                            .handleMonth(DateUtil.getMonthForDate(handlerDate))
                            //.operatorId(currentUser.getId()) //创建人id
                            //.operatorName(currentUser.getName()) //创建人名
                            .remark(sd.getRemark())
                            .build();
            doctorWarehouseMaterialHandleList.add(doctorWarehouseMaterialHandle);
        }

        List<DoctorWarehouseMaterialHandle> doctorDBRKWarehouseMaterialHandleList =
                new ArrayList<>();
        if (handleSubType == 9) //调拨出库,生成调拨入库单
        {
            for (StockDto sd : stockDtoList) {
                DoctorWarehouseMaterialHandle doctorDBRKWarehouseMaterialHandle =
                        DoctorWarehouseMaterialHandle.builder()
                                .farmId(sd.getDbFarmId())
                                .warehouseId(sd.getDbWarehouseId())
                                .warehouseType(sd.getDbWarehouseType())
                                .warehouseName(sd.getDbWarehouseName())
                                .vendorName(sd.getVendorName())
                                .materialId(sd.getMaterialId())
                                .materialName(sd.getMaterialName())
                                .type(handleSubType)
                                .unitPrice(sd.getUnitPrice())
                                .unit(sd.getUnit())
                                .deleteFlag(1) //表示未删除
                                .beforeStockQuantity(sd.getBeforeStockQuantity()) //当前数量
                                .quantity(sd.getQuantity())
                                .handleDate(handlerDate) //处理日期
                                .handleYear(DateUtil.getYearForDate(handlerDate))
                                .handleMonth(DateUtil.getMonthForDate(handlerDate))
                                //.operatorId(currentUser.getId()) //创建人id
                                //.operatorName(currentUser.getName()) //创建人名
                                .remark(sd.getRemark())
                                .build();
                doctorDBRKWarehouseMaterialHandleList.add(doctorDBRKWarehouseMaterialHandle);
            }
        }

        List<DoctorWarehouseMaterialApply> doctorWarehouseMaterialApplies = new ArrayList<>();

        /**
         * applyType
         * 0 表示按栋舍领用
         * 1 表示按猪群领用
         * 2 表示产房母猪
         */
        if (handleSubType == 6) //领料出库,涉及到猪群或猪舍的物料领用
        {
            for (StockDto sd : stockDtoList) {
                DoctorWarehouseMaterialApply doctorWarehouseMaterialApply = DoctorWarehouseMaterialApply.builder()
                        .farmId(sd.getFarmId())
                        .warehouseId(sd.getWarehouseId())
                        .warehouseType(sd.getWarehouseType())
                        .warehouseName(sd.getWarehouseName())
                        .pigBarnId(sd.getPigBarnId())
                        .pigBarnName(sd.getPigBarnName())
                        .pigGroupId(sd.getPigGroupId())
                        .pigGroupName(sd.getPigGroupName())
                        .materialId(sd.getMaterialId())
                        .applyDate(handlerDate)
                        .applyStaffName(sd.getApplyStaffName())
                        .applyYear(DateUtil.getYearForDate(handlerDate))
                        .applyMonth(DateUtil.getMonthForDate(handlerDate))
                        .materialName(sd.getMaterialName())
                        .type(sd.getMaterialType())
                        .unit(sd.getUnit())
                        .quantity(sd.getQuantity())
                        .unitPrice(sd.getUnitPrice())
                        .applyType(sd.getApplyType())
                        .build();
                doctorWarehouseMaterialApplies.add(doctorWarehouseMaterialApply);
            }
        }

        Response<Long> response = doctorWarehouseStockWriteService.create(doctorWarehouseStockHandle, doctorWarehouseMaterialHandleList,
                doctorDBRKWarehouseMaterialHandleList,
                doctorWarehouseMaterialApplies);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
        return true;
    }

    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    /**
     * 修改单据明细
     *
     * @param stockDtoList
     * @return
     */
    @RequestMapping(value = "/updateStock", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean update(@RequestBody @Valid List<StockDto> stockDtoList) {

        if (CollectionUtils.isEmpty(stockDtoList)) {
            throw new JsonResponseException("stock.stockDtoList.not.null");
        }

        StockDto stockDto = stockDtoList.get(0);
        Response<User> currentUserResponse = userReadService.findById(stockDto.getOperatorId());
        // 得到creatorId,creatorName
        User currentUser = currentUserResponse.getResult();
        if (null == currentUser)
            throw new JsonResponseException("stock.operator.not.exist");

        int handleSubType = stockDto.getHandleSubType();

        /**
         * 单据主表
         */
        DoctorWarehouseStockHandle doctorWarehouseStockHandle =
                DoctorWarehouseStockHandle.builder()
                        .id(stockDto.getStockHandleId())
                        .handleDate(stockDto.getHandleDate()) //处理日期
                        .operatorName(currentUser.getName()) //创建人名
                        .operatorId(currentUser.getId()) //创建人id
                        .build();

        Date handlerDate = stockDto.getHandleDate();

        List<DoctorWarehouseMaterialHandle> doctorWarehouseMaterialHandleList =
                new ArrayList<>();

        //支持添加多条单据数据
        for (StockDto sd : stockDtoList) {
            /**
             * 单据明细表
             */
            DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle =
                    DoctorWarehouseMaterialHandle.builder()
                            .id(sd.getId())
                            .vendorName(sd.getVendorName())
                            .materialId(sd.getMaterialId())
                            .materialName(sd.getMaterialName())
                            .unitPrice(sd.getUnitPrice())
                            .unit(sd.getUnit())
                            .beforeStockQuantity(sd.getBeforeStockQuantity()) //当前数量
                            .quantity(sd.getQuantity())
                            .handleDate(handlerDate) //处理日期
                            .handleYear(DateUtil.getYearForDate(handlerDate))
                            .handleMonth(DateUtil.getMonthForDate(handlerDate))
                            .operatorId(currentUser.getId()) //创建人id
                            .operatorName(currentUser.getName()) //创建人名
                            .remark(sd.getRemark())
                            .build();
            doctorWarehouseMaterialHandleList.add(doctorWarehouseMaterialHandle);
        }

        List<DoctorWarehouseMaterialHandle> doctorDBRKWarehouseMaterialHandleList =
                new ArrayList<>();
        if (handleSubType == 9) //调拨出库,生成调拨入库单
        {
            for (StockDto sd : stockDtoList) {
                DoctorWarehouseMaterialHandle doctorDBRKWarehouseMaterialHandle =
                        DoctorWarehouseMaterialHandle.builder()
                                .id(sd.getOtherTransferHandleId())
                                .farmId(sd.getDbFarmId())
                                .warehouseId(sd.getDbWarehouseId())
                                .warehouseType(sd.getDbWarehouseType())
                                .warehouseName(sd.getDbWarehouseName())
                                .vendorName(sd.getVendorName())
                                .materialId(sd.getMaterialId())
                                .materialName(sd.getMaterialName())
                                .unitPrice(sd.getUnitPrice())
                                .unit(sd.getUnit())
                                .beforeStockQuantity(sd.getBeforeStockQuantity()) //当前数量
                                .quantity(sd.getQuantity())
                                .handleDate(handlerDate) //处理日期
                                .handleYear(DateUtil.getYearForDate(handlerDate))
                                .handleMonth(DateUtil.getMonthForDate(handlerDate))
                                .operatorId(currentUser.getId()) //创建人id
                                .operatorName(currentUser.getName()) //创建人名
                                .remark(sd.getRemark())
                                .build();
                doctorDBRKWarehouseMaterialHandleList.add(doctorDBRKWarehouseMaterialHandle);
            }
        }

        List<DoctorWarehouseMaterialApply> doctorWarehouseMaterialApplies = new ArrayList<>();

        /**
         * applyType
         * 0 表示按栋舍领用
         * 1 表示按猪群领用
         * 2 表示产房母猪
         */
        if (handleSubType == 6) //领料出库,涉及到猪群或猪舍的物料领用
        {
            for (StockDto sd : stockDtoList) {
                DoctorWarehouseMaterialApply apply = new DoctorWarehouseMaterialApply();
                apply.setMaterialHandleId(sd.getId());
                List<DoctorWarehouseMaterialApply> applies = doctorWarehouseMaterialApplyReadService.list(apply).getResult();
                if (!CollectionUtils.isEmpty(applies)) {
                    apply = applies.get(0);
                    DoctorWarehouseMaterialApply doctorWarehouseMaterialApply = DoctorWarehouseMaterialApply.builder()
                            .id(apply.getId())
//                            .farmId(sd.getFarmId())
//                            .warehouseId(sd.getWarehouseId())
//                            .warehouseType(sd.getWarehouseType())
//                            .warehouseName(sd.getWarehouseName())
                            .pigBarnId(sd.getPigBarnId())
                            .pigBarnName(sd.getPigBarnName())
                            .pigGroupId(sd.getPigGroupId())
                            .pigGroupName(sd.getPigGroupName())
                            .materialId(sd.getMaterialId())
                            .applyDate(handlerDate)
                            .applyStaffName(sd.getApplyStaffName())
                            .applyYear(DateUtil.getYearForDate(handlerDate))
                            .applyMonth(DateUtil.getMonthForDate(handlerDate))
                            .materialName(sd.getMaterialName())
                            .type(sd.getMaterialType())
                            .unit(sd.getUnit())
                            .quantity(sd.getQuantity())
                            .unitPrice(sd.getUnitPrice())
                            .applyType(sd.getApplyType())
                            .build();
                    doctorWarehouseMaterialApplies.add(doctorWarehouseMaterialApply);
                }
            }
        }

        Response<Long> response = doctorWarehouseStockWriteService.update(doctorWarehouseStockHandle, doctorWarehouseMaterialHandleList,
                doctorDBRKWarehouseMaterialHandleList,
                doctorWarehouseMaterialApplies);
        if (!response.isSuccess())
            throw new JsonResponseException(response.getError());
        return true;
    }
    /***********    2018/04/11    *************/

}
