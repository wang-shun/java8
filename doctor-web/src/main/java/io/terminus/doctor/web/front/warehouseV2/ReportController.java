package io.terminus.doctor.web.front.warehouseV2;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockStatisticsDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehouseSkuStatus;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.web.front.warehouseV2.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2017/8/9.
 */
@Slf4j
@RestController
@RequestMapping("api/doctor/warehouse/report")
public class ReportController {


    @RpcConsumer
    private DoctorWarehousePurchaseReadService doctorWarehousePurchaseReadService;

    @RpcConsumer
    private DoctorWarehouseStockReadService doctorWarehouseStockReadService;

    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;

    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @RpcConsumer
    private NewDoctorWarehouseReaderService doctorWareHouseReadService;

    @RpcConsumer
    private DoctorGroupReadService doctorGroupReadService;

    @RpcConsumer
    private DoctorWarehouseReportReadService doctorWarehouseReportReadService;
    @RpcConsumer
    private DoctorWarehouseStockMonthlyReadService doctorWarehouseStockMonthlyReadService;
    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;

    /**
     * 仓库报表
     *
     * @param farmId
     * @param date
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<WarehouseReportVo> report(@RequestParam Long farmId,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {

        if (date.after(Calendar.getInstance()))
            throw new JsonResponseException("date.after.now");

        List<WarehouseReportVo> reports = new ArrayList<>();

        Response<List<DoctorWareHouse>> warehouseResponse = doctorWareHouseReadService.findByFarmId(farmId);
        if (!warehouseResponse.isSuccess())
            throw new JsonResponseException(warehouseResponse.getError());
        if (null == warehouseResponse.getResult() || warehouseResponse.getResult().isEmpty()) {
            log.info("猪厂{}下未找到仓库", farmId);
            return Collections.emptyList();
        }

        date.add(Calendar.MONTH, 1); //包含选中的那一月
        for (int i = 0; i < 6; i++) { //最近六个月
            date.add(Calendar.MONTH, -1);
            int month = date.get(Calendar.MONTH) + 1; //默认月份0～11
            int year = date.get(Calendar.YEAR);

            //统计猪厂下每个仓库本月的入库和出库金额
            Response<Map<WarehouseMaterialHandleType, Map<Long, Long>>> inAndOutAmountsResponse = doctorWarehouseMaterialHandleReadService.
                    countWarehouseAmount(DoctorWarehouseMaterialHandle.builder()
                                    .farmId(farmId)
                                    .handleYear(year)
                                    .handleMonth(month)
                                    .build(),
                            WarehouseMaterialHandleType.OUT,
                            WarehouseMaterialHandleType.IN,
                            WarehouseMaterialHandleType.INVENTORY_PROFIT,
                            WarehouseMaterialHandleType.INVENTORY_DEFICIT,
                            WarehouseMaterialHandleType.TRANSFER_IN,
                            WarehouseMaterialHandleType.TRANSFER_OUT,
                            WarehouseMaterialHandleType.FORMULA_IN,
                            WarehouseMaterialHandleType.FORMULA_OUT);

            if (!inAndOutAmountsResponse.isSuccess())
                throw new JsonResponseException(inAndOutAmountsResponse.getError());

            //统计猪厂下每个仓库的当前余额
//            Response<Map<Long, Long>> balanceAmountsResponse = doctorWarehousePurchaseReadService.countWarehouseBalanceAmount(farmId);
//            if (!balanceAmountsResponse.isSuccess())
//                throw new JsonResponseException(balanceAmountsResponse.getError());

            WarehouseReportVo balanceVo = new WarehouseReportVo();
            balanceVo.setMonthAndType(year + "-" + month + "结余");

            WarehouseReportVo inVo = new WarehouseReportVo();
            inVo.setMonthAndType(year + "-" + month + "入库");

            WarehouseReportVo outVo = new WarehouseReportVo();
            outVo.setMonthAndType(year + "-" + month + "出库");

            List<WarehouseReportVo.WarehouseReportMonthDetail> balanceDetails = new ArrayList<>(warehouseResponse.getResult().size());
            List<WarehouseReportVo.WarehouseReportMonthDetail> inDetails = new ArrayList<>(warehouseResponse.getResult().size());
            List<WarehouseReportVo.WarehouseReportMonthDetail> outDetails = new ArrayList<>(warehouseResponse.getResult().size());
            long totalBalance = 0, totalIn = 0, totalOut = 0;
            for (DoctorWareHouse wareHouse : warehouseResponse.getResult()) {
//
//
                AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countWarehouseBalance(wareHouse.getId(), year, month));
                balanceDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                        .name(wareHouse.getWareHouseName())
                        .amount(balance.getAmount())
                        .build());

                long inAmount;
                if (!inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.IN))
                    inAmount = 0;
                else if (!inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.IN).containsKey(wareHouse.getId()))
                    inAmount = 0;
                else
                    inAmount = inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.IN).get(wareHouse.getId());
                inAmount += inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.INVENTORY_PROFIT) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.INVENTORY_PROFIT).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.INVENTORY_PROFIT).get(wareHouse.getId()) : 0 : 0;
                inAmount += inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.TRANSFER_IN) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.TRANSFER_IN).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.TRANSFER_IN).get(wareHouse.getId()) : 0 : 0;

                inAmount += inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.FORMULA_IN) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.FORMULA_IN).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.FORMULA_IN).get(wareHouse.getId()) : 0 : 0;

                inDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                        .name(wareHouse.getWareHouseName())
                        .amount(inAmount)
                        .build());

                long outAmount;
                if (!inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.OUT))
                    outAmount = 0;
                else if (!inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.OUT).containsKey(wareHouse.getId()))
                    outAmount = 0;
                else
                    outAmount = inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.OUT).get(wareHouse.getId());

                outAmount += inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.INVENTORY_DEFICIT) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.INVENTORY_DEFICIT).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.INVENTORY_DEFICIT).get(wareHouse.getId()) : 0 : 0;
                outAmount += inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.TRANSFER_OUT) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.TRANSFER_OUT).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.TRANSFER_OUT).get(wareHouse.getId()) : 0 : 0;
                outAmount += inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.FORMULA_OUT) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.FORMULA_OUT).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.FORMULA_OUT).get(wareHouse.getId()) : 0 : 0;
                outDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                        .name(wareHouse.getWareHouseName())
                        .amount(outAmount)
                        .build());

                totalBalance += balance.getAmount();
                totalIn += inAmount;
                totalOut += outAmount;
            }
            balanceDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                    .name("合计")
                    .amount(totalBalance)
                    .build());
            inDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                    .name("合计")
                    .amount(totalIn)
                    .build());
            outDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                    .name("合计")
                    .amount(totalOut)
                    .build());
            balanceVo.setDetails(balanceDetails);
            inVo.setDetails(inDetails);
            outVo.setDetails(outDetails);
            reports.add(balanceVo);
            reports.add(inVo);
            reports.add(outVo);
        }
        return reports;
    }

    /**
     * 月报
     *
     * @param warehouseId
     * @param date
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "monthly")
    public List<WarehouseMonthlyReportVo> monthlyReport(@RequestParam Long warehouseId,
                                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {


        Calendar lastMonth = Calendar.getInstance();
        lastMonth.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
        lastMonth.add(Calendar.MONTH, -1);
        DoctorWarehouseStock stockCriteria = new DoctorWarehouseStock();
        stockCriteria.setWarehouseId(warehouseId);
        Response<List<DoctorWarehouseStock>> stocksResponse = doctorWarehouseStockReadService.list(stockCriteria);
        if (!stocksResponse.isSuccess())
            throw new JsonResponseException(stocksResponse.getError());

        Map<Long, List<DoctorWarehouseSku>> skuMap = RespHelper.or500(doctorWarehouseSkuReadService.findByIds(stocksResponse.getResult().stream()
                .map(DoctorWarehouseStock::getSkuId).collect(Collectors.toList())))
                .stream().collect(Collectors.groupingBy(DoctorWarehouseSku::getId));

        List<WarehouseMonthlyReportVo> report = new ArrayList<>();
        for (DoctorWarehouseStock stock : stocksResponse.getResult()) {

            Response<AmountAndQuantityDto> balanceResponse = doctorWarehouseReportReadService.countMaterialBalance(warehouseId, stock.getSkuId(), null);
            if (!balanceResponse.isSuccess())
                throw new JsonResponseException(balanceResponse.getError());
            Response<WarehouseStockStatisticsDto> statisticsResponse = doctorWarehouseReportReadService.countMaterialHandleByMaterialVendor(warehouseId, stock.getSkuId(), null, date,
                    WarehouseMaterialHandleType.IN,
                    WarehouseMaterialHandleType.OUT,
                    WarehouseMaterialHandleType.INVENTORY_PROFIT,
                    WarehouseMaterialHandleType.INVENTORY_DEFICIT,
                    WarehouseMaterialHandleType.FORMULA_IN,
                    WarehouseMaterialHandleType.TRANSFER_OUT,
                    WarehouseMaterialHandleType.FORMULA_IN,
                    WarehouseMaterialHandleType.FORMULA_OUT
            );
            if (!statisticsResponse.isSuccess())
                throw new JsonResponseException(statisticsResponse.getError());

//            DoctorWarehouseSku sku = RespHelper.or500(doctorWarehouseSkuReadService.findById(stock.getSkuId()));
//            if (!skuMap.containsKey(stock.getSkuId()))
//                throw new InvalidException("warehouse.sku.not.found", stock.getSkuId());
//            DoctorWarehouseSku sku = skuMap.get(stock.getSkuId()).get(0);

            WarehouseMonthlyReportVo vo = new WarehouseMonthlyReportVo();
            vo.setMaterialName(stock.getSkuName());

            if (skuMap.containsKey(stock.getSkuId())) {
                //TODO warehousevendor
//                vo.setVendorName(skuMap.get(stock.getSkuId()).get(0).getVendorName());
                vo.setUnit(skuMap.get(stock.getSkuId()).get(0).getUnit());
                vo.setSpecification(skuMap.get(stock.getSkuId()).get(0).getSpecification());
                vo.setCode(skuMap.get(stock.getSkuId()).get(0).getCode());
            }

            vo.setBalanceAmount(balanceResponse.getResult().getAmount());
            vo.setBalanceQuantity(balanceResponse.getResult().getQuantity());

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

            AmountAndQuantityDto initialBalance = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countMaterialBalance(warehouseId, stock.getSkuId(), lastMonth.get(Calendar.YEAR), lastMonth.get(Calendar.MONTH) + 1));
            vo.setInitialAmount(initialBalance.getAmount());
            vo.setInitialQuantity(initialBalance.getQuantity());

//            vo.setInQuantity(vo.getBalanceQuantity().add(vo.getOutQuantity()).multiply(vo.getInQuantity()));

            report.add(vo);
        }

        return report;
    }


    /**
     * 物料变动明细
     *
     * @param warehouseId
     * @param date
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "material")
    @JsonView(WarehouseMaterialHandleVo.MaterialHandleReportView.class)
    public List<WarehouseMaterialHandleVo> materialHandleReport(@RequestParam Long warehouseId,
                                                                @RequestParam Long orgId,
                                                                @RequestParam(required = false) String materialName,
                                                                @RequestParam(required = false) Integer type,
                                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {


        Map<String, Object> criteria = new HashMap<>();
        criteria.put("warehouseId", warehouseId);
        criteria.put("handleYear", date.get(Calendar.YEAR));
        criteria.put("handleMonth", date.get(Calendar.MONTH) + 1);
        criteria.put("deleteFlag", WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
        if (null != type) {
            if (4 == type) {
                criteria.put("bigType", Lists.newArrayList(WarehouseMaterialHandleType.TRANSFER_IN.getValue(), WarehouseMaterialHandleType.TRANSFER_OUT.getValue()));
            } else if (3 == type) {
                criteria.put("bigType", Lists.newArrayList(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue(), WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue()));
            } else
                criteria.put("type", type);
        }

        if (StringUtils.isNotBlank(materialName)) {
            Map<String, Object> skuParams = new HashMap<>();
            skuParams.put("orgId", orgId);
            skuParams.put("status", WarehouseSkuStatus.NORMAL.getValue());
            skuParams.put("nameOrSrmLike", materialName);
            List<Long> skuIds = RespHelper.or500(doctorWarehouseSkuReadService.list(skuParams)).stream().map(DoctorWarehouseSku::getId).collect(Collectors.toList());
            if (skuIds.isEmpty())
                return Collections.emptyList();
            criteria.put("skuIds", skuIds);
        }
//        if (StringUtils.isNotBlank(materialName))
//            criteria.put("materialNameLike", materialName);

        Response<List<DoctorWarehouseMaterialHandle>> materialHandleResponse = doctorWarehouseMaterialHandleReadService.advList(criteria);
        if (!materialHandleResponse.isSuccess())
            throw new JsonResponseException(materialHandleResponse.getError());

        Response<List<DoctorWarehouseMaterialApply>> applyResponse = doctorWarehouseMaterialApplyReadService.list(DoctorWarehouseMaterialApply.builder()
                .warehouseId(warehouseId)
                .materialName(StringUtils.isBlank(materialName) ? null : materialName)
                .applyYear(date.get(Calendar.YEAR))
                .applyMonth(date.get(Calendar.MONTH) + 1)
                .build());
        if (!applyResponse.isSuccess())
            throw new JsonResponseException(applyResponse.getError());


        Map<Long/*MaterialHandleId*/, DoctorWarehouseMaterialApply> handleApply = new HashMap<>();
        for (DoctorWarehouseMaterialApply apply : applyResponse.getResult()) {
            handleApply.put(apply.getMaterialHandleId(), apply);
        }

        Map<Long, List<DoctorWarehouseSku>> skuMap = RespHelper.or500(doctorWarehouseSkuReadService.findByIds(materialHandleResponse.getResult().stream().map(DoctorWarehouseMaterialHandle::getMaterialId).collect(Collectors.toList()))).stream().collect(Collectors.groupingBy(DoctorWarehouseSku::getId));

        List<WarehouseMaterialHandleVo> vos = new ArrayList<>(materialHandleResponse.getResult().size());
        for (DoctorWarehouseMaterialHandle handle : materialHandleResponse.getResult()) {

            String pigBarnName, pigGroupName;
            if (!handleApply.containsKey(handle.getId()))
                pigBarnName = pigGroupName = null;
            else {
                pigBarnName = handleApply.get(handle.getId()).getPigBarnName();
                pigGroupName = handleApply.get(handle.getId()).getPigGroupName();
            }

//            if (!skuMap.containsKey(handle.getMaterialId()))
//                throw new InvalidException("warehouse.sku.not.found", handle.getMaterialId());

            WarehouseMaterialHandleVo handleVo = WarehouseMaterialHandleVo.builder()
                    .materialName(handle.getMaterialName())
                    .type(handle.getType())
                    .handleDate(handle.getHandleDate())
                    .quantity(handle.getQuantity())
                    .unitPrice(handle.getUnitPrice())
                    .pigBarnName(pigBarnName)
                    .pigGroupName(pigGroupName)
                    .code(skuMap.get(handle.getMaterialId()).get(0).getCode())
                    //TODO warehousevendor
//                    .vendorName(skuMap.get(handle.getMaterialId()).get(0).getVendorName())
                    .specification(skuMap.get(handle.getMaterialId()).get(0).getSpecification())
                    .unit(skuMap.get(handle.getMaterialId()).get(0).getUnit())
                    .warehouseName(handle.getWarehouseName())
                    .build();

            if (skuMap.containsKey(handle.getMaterialId())) {
                handleVo.setCode(skuMap.get(handle.getMaterialId()).get(0).getCode());
                //TODO warehousevendor
//                handleVo.setVendorName(skuMap.get(handle.getMaterialId()).get(0).getVendorName());
                handleVo.setSpecification(skuMap.get(handle.getMaterialId()).get(0).getSpecification());
                handleVo.setUnit(skuMap.get(handle.getMaterialId()).get(0).getUnit());
            }

            vos.add(handleVo);
        }
        return vos;
    }

    /**
     * 猪舍领用报表
     *
     * @param pigBarnId
     * @param warehouseId
     * @param date
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "pigBarnApply")
    public List<WarehouseMaterialApplyVo> apply(@RequestParam(required = false) Long pigBarnId,
                                                @RequestParam Long warehouseId,
                                                @RequestParam Long orgId,
                                                @RequestParam(required = false) Integer type,
                                                @RequestParam(required = false) String materialName,
                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {


        Map<String, Object> criteria = new HashMap<>();
        criteria.put("applyYear", date.get(Calendar.YEAR));
        criteria.put("applyMonth", date.get(Calendar.MONTH) + 1);
        criteria.put("warehouseId", warehouseId);

//        if (StringUtils.isNotBlank(materialName))
//            criteria.put("materialNameLike", materialName);
        if (null != pigBarnId)
            criteria.put("pigBarnId", pigBarnId);
        if (null != type)
            criteria.put("type", type);

        if (StringUtils.isNotBlank(materialName)) {
            Map<String, Object> skuParams = new HashMap<>();
            skuParams.put("orgId", orgId);
            skuParams.put("status", WarehouseSkuStatus.NORMAL.getValue());
            skuParams.put("nameOrSrmLike", materialName);
            List<Long> skuIds = RespHelper.or500(doctorWarehouseSkuReadService.list(skuParams)).stream().map(DoctorWarehouseSku::getId).collect(Collectors.toList());
            if (skuIds.isEmpty())
                return Collections.emptyList();
            criteria.put("skuIds", skuIds);
        }

        Response<List<DoctorWarehouseMaterialApply>> applyResponse = doctorWarehouseMaterialApplyReadService.list(criteria);
        if (!applyResponse.isSuccess())
            throw new JsonResponseException(applyResponse.getError());

        Map<Long, List<DoctorWarehouseSku>> skuMap = RespHelper.or500(doctorWarehouseSkuReadService.findByIds(applyResponse.getResult().stream().map(DoctorWarehouseMaterialApply::getMaterialId).collect(Collectors.toList()))).stream().collect(Collectors.groupingBy(DoctorWarehouseSku::getId));

        List<WarehouseMaterialApplyVo> vos = new ArrayList<>(applyResponse.getResult().size());
        for (DoctorWarehouseMaterialApply apply : applyResponse.getResult()) {
//            if (!skuMap.containsKey(apply.getMaterialId()))
//                throw new InvalidException("warehouse.sku.not.found");

            WarehouseMaterialApplyVo vo = new WarehouseMaterialApplyVo();
            BeanUtils.copyProperties(apply, vo);
            if (skuMap.containsKey(apply.getMaterialId())) {
                vo.setUnit(skuMap.get(apply.getMaterialId()).get(0).getUnit());
                vo.setCode(skuMap.get(apply.getMaterialId()).get(0).getCode());
                //TODO warehousevendor
//                vo.setVendorName(skuMap.get(apply.getMaterialId()).get(0).getVendorName());
                vo.setSpecification(skuMap.get(apply.getMaterialId()).get(0).getSpecification());
            }
            vos.add(vo);
        }

        return vos;
    }

    /**
     * 猪群领用报表
     *
     * @param warehouseId
     * @param pigGroupId
     * @param materialType
     * @param materialName
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "pigGroupApply")
    public List<WarehousePigGroupApplyVo> pigGroupApply(@RequestParam Long warehouseId,
                                                        @RequestParam Long orgId,
                                                        @RequestParam(required = false) Long pigGroupId,
                                                        @RequestParam(required = false) Integer materialType,
                                                        @RequestParam(required = false) String materialName) {


        Map<String, Object> criteria = new HashMap<>();
//        if (StringUtils.isNotBlank(materialName))
//            criteria.put("materialNameLike", materialName);
        criteria.put("warehouseId", warehouseId);
        criteria.put("type", materialType);
        criteria.put("pigGroupId", pigGroupId);

        if (StringUtils.isNotBlank(materialName)) {
            Map<String, Object> skuParams = new HashMap<>();
            skuParams.put("orgId", orgId);
            skuParams.put("status", WarehouseSkuStatus.NORMAL.getValue());
            skuParams.put("nameOrSrmLike", materialName);
            List<Long> skuIds = RespHelper.or500(doctorWarehouseSkuReadService.list(skuParams)).stream().map(DoctorWarehouseSku::getId).collect(Collectors.toList());
            if (skuIds.isEmpty())
                return Collections.emptyList();
            criteria.put("skuIds", skuIds);
        }

        Response<List<DoctorWarehouseMaterialApply>> applyResponse = doctorWarehouseMaterialApplyReadService.list(criteria);

        if (!applyResponse.isSuccess())
            throw new JsonResponseException(applyResponse.getError());
        if (null == applyResponse.getResult() || applyResponse.getResult().isEmpty()) {
            log.info("未找到物料领用记录,warehouseId[{}],pigGroupId[{}],materialType[{}],materialName[{}]", warehouseId, pigGroupId, materialType, materialName);
            return Collections.emptyList();
        }

        Map<Long, List<DoctorWarehouseSku>> skuMap = RespHelper.or500(doctorWarehouseSkuReadService.findByIds(applyResponse.getResult().stream().map(DoctorWarehouseMaterialApply::getMaterialId).collect(Collectors.toList()))).stream().collect(Collectors.groupingBy(DoctorWarehouseSku::getId));

        List<WarehousePigGroupApplyVo> vos = new ArrayList<>();
        for (DoctorWarehouseMaterialApply apply : applyResponse.getResult()) {
            if (null == apply.getPigGroupId())
                continue;

            Response<DoctorGroup> groupResponse = doctorGroupReadService.findGroupById(apply.getPigGroupId());
            if (!groupResponse.isSuccess())
                throw new JsonResponseException(groupResponse.getError());
            if (null == groupResponse.getResult())
                throw new JsonResponseException("pig.group.not.found");
//            if (!skuMap.containsKey(apply.getMaterialId()))
//                throw new InvalidException("warehouse.sku.not.found", apply.getMaterialId());


            WarehousePigGroupApplyVo applyVo = WarehousePigGroupApplyVo.builder()
                    .pigGroupId(apply.getPigGroupId())
                    .pigGroupName(apply.getPigGroupName())
                    .openDate(groupResponse.getResult().getOpenAt())
                    .closeDate(groupResponse.getResult().getCloseAt())
                    .pigBarnName(apply.getPigBarnName())
                    .applyStaffName(apply.getApplyStaffName())
                    .materialType(apply.getType())
                    .materialName(apply.getMaterialName())
//                    .unit(apply.getUnit())
//                    .unit(skuMap.get(apply.getMaterialId()).get(0).getUnit())
//                    .code(skuMap.get(apply.getMaterialId()).get(0).getCode())
//                    .vendorName(skuMap.get(apply.getMaterialId()).get(0).getVendorName())
//                    .specification(skuMap.get(apply.getMaterialId()).get(0).getSpecification())
                    .quantity(apply.getQuantity())
                    .unitPrice(apply.getUnitPrice())
                    .amount(apply.getQuantity().multiply(new BigDecimal(apply.getUnitPrice())).longValue())
                    .build();

            if (skuMap.containsKey(apply.getMaterialId())) {
                applyVo.setUnit(skuMap.get(apply.getMaterialId()).get(0).getUnit());
                applyVo.setCode(skuMap.get(apply.getMaterialId()).get(0).getCode());
                applyVo.setSpecification(skuMap.get(apply.getMaterialId()).get(0).getSpecification());
                //TODO warehousevendor
//                applyVo.setVendorName(skuMap.get(apply.getMaterialId()).get(0).getVendorName());
            }

            vos.add(applyVo);
        }

        return vos;
    }

}
