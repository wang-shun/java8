package io.terminus.doctor.web.front.warehouseV2;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockStatisticsDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialApplyType;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehouseSkuStatus;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.*;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.warehouseV2.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Blob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 报表
 * Created by sunbo@terminus.io on 2017/8/9.
 */
@Slf4j
@RestController("warehouseReportController")
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
    @RpcConsumer
    private DoctorWarehouseVendorReadService doctorWarehouseVendorReadService;
    @RpcConsumer
    private DoctorBasicReadService doctorBasicReadService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;


    @InitBinder
    public void init(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

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

        Map<Long/*warehouseId*/, AmountAndQuantityDto> lastMonthBalance = new HashMap<>();  //上一个月的发生额
        date.add(Calendar.MONTH, -6);         //从6个月前开始遍历
        for (int i = 0; i < 6; i++) {                 //最近六个月
            date.add(Calendar.MONTH, 1);      //往前推
            int month = date.get(Calendar.MONTH) + 1; //默认月份0～11
            int year = date.get(Calendar.YEAR);

            //统计猪厂下每个仓库本月的入库和出库金额
            Response<Map<WarehouseMaterialHandleType, Map<Long, BigDecimal>>> inAndOutAmountsResponse = doctorWarehouseMaterialHandleReadService.
                    countWarehouseAmount(DoctorWarehouseMaterialHandle.builder()
                                    .farmId(farmId)
                                    .handleYear(year)
                                    .handleMonth(month)
                                    .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
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

            WarehouseReportVo balanceVo = new WarehouseReportVo();
            balanceVo.setMonthAndType(year + "-" + month + "结余");

            WarehouseReportVo inVo = new WarehouseReportVo();
            inVo.setMonthAndType(year + "-" + month + "入库");

            WarehouseReportVo outVo = new WarehouseReportVo();
            outVo.setMonthAndType(year + "-" + month + "出库");

            List<WarehouseReportVo.WarehouseReportMonthDetail> balanceDetails = new ArrayList<>(warehouseResponse.getResult().size());
            List<WarehouseReportVo.WarehouseReportMonthDetail> inDetails = new ArrayList<>(warehouseResponse.getResult().size());
            List<WarehouseReportVo.WarehouseReportMonthDetail> outDetails = new ArrayList<>(warehouseResponse.getResult().size());
            BigDecimal totalBalance = new BigDecimal(0), totalIn = new BigDecimal(0), totalOut = new BigDecimal(0);
            for (DoctorWareHouse wareHouse : warehouseResponse.getResult()) {
//                AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countWarehouseBalance(wareHouse.getId(), year, month));
//
                AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countWarehouseBalance(wareHouse.getId(), DateUtil.toYYYYMM(year + "-" + month)));

                balanceDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                        .name(wareHouse.getWareHouseName())
                        .amount((balance.getAmount()))
                        .build());
//
//                if (lastMonthBalance.containsKey(wareHouse.getId())) {
//                    AmountAndQuantityDto a = lastMonthBalance.get(wareHouse.getId());
//                    lastMonthBalance.put(wareHouse.getId(), new AmountAndQuantityDto(balance.getAmount() + a.getAmount(), balance.getQuantity().add(a.getQuantity())));
//                } else
//                    lastMonthBalance.put(wareHouse.getId(), new AmountAndQuantityDto(balance.getAmount(), balance.getQuantity()));


                BigDecimal inAmount;
                if (!inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.IN))
                    inAmount = new BigDecimal(0);
                else if (!inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.IN).containsKey(wareHouse.getId()))
                    inAmount = new BigDecimal(0);
                else
                    inAmount = inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.IN).get(wareHouse.getId());
                inAmount = inAmount.add(inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.INVENTORY_PROFIT) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.INVENTORY_PROFIT).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.INVENTORY_PROFIT).get(wareHouse.getId()) : new BigDecimal(0) : new BigDecimal(0));

                inAmount = inAmount.add(inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.TRANSFER_IN) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.TRANSFER_IN).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.TRANSFER_IN).get(wareHouse.getId()) : new BigDecimal(0) : new BigDecimal(0));

                inAmount = inAmount.add(inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.FORMULA_IN) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.FORMULA_IN).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.FORMULA_IN).get(wareHouse.getId()) : new BigDecimal(0) : new BigDecimal(0));

                inDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                        .name(wareHouse.getWareHouseName())
                        .amount(inAmount)
                        .build());

                BigDecimal outAmount;
                if (!inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.OUT))
                    outAmount = new BigDecimal(0);
                else if (!inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.OUT).containsKey(wareHouse.getId()))
                    outAmount = new BigDecimal(0);
                else
                    outAmount = inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.OUT).get(wareHouse.getId());

                outAmount = outAmount.add(inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.INVENTORY_DEFICIT) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.INVENTORY_DEFICIT).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.INVENTORY_DEFICIT).get(wareHouse.getId()) : new BigDecimal(0) : new BigDecimal(0));

                outAmount = outAmount.add(inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.TRANSFER_OUT) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.TRANSFER_OUT).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.TRANSFER_OUT).get(wareHouse.getId()) : new BigDecimal(0) : new BigDecimal(0));

                outAmount = outAmount.add(inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.FORMULA_OUT) ?
                        inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.FORMULA_OUT).containsKey(wareHouse.getId()) ?
                                inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.FORMULA_OUT).get(wareHouse.getId()) : new BigDecimal(0) : new BigDecimal(0));

                outDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                        .name(wareHouse.getWareHouseName())
                        .amount(outAmount)
                        .build());

                totalBalance = totalBalance.add(balance.getAmount());
                totalIn = totalIn.add(inAmount);
                totalOut = totalOut.add(outAmount);
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

            reports.add(outVo);
            reports.add(inVo);
            reports.add(balanceVo);
        }
        Collections.reverse(reports);
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

//        Map<Long, AmountAndQuantityDto> balanceMap = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countEachMaterialBalance(warehouseId, date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1));
        //刨除本月的历史余额，也称为月初余额
        Map<Long, AmountAndQuantityDto> lastMonthBalanceMap = RespHelper.or500(doctorWarehouseStockMonthlyReadService.countEachMaterialBalance(warehouseId, lastMonth.get(Calendar.YEAR), lastMonth.get(Calendar.MONTH) + 1));

        List<WarehouseMonthlyReportVo> report = new ArrayList<>();
        for (DoctorWarehouseStock stock : stocksResponse.getResult()) {

//            AmountAndQuantityDto balance = RespHelper.or500(doctorWarehouseStockMonthlyReadService
//                    .countMaterialBalance(warehouseId, stock.getSkuId(), date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1));
//            AmountAndQuantityDto initialBalance = RespHelper.or500(doctorWarehouseStockMonthlyReadService
//                    .countMaterialBalance(warehouseId, stock.getSkuId(), lastMonth.get(Calendar.YEAR), lastMonth.get(Calendar.MONTH) + 1));

//            AmountAndQuantityDto balance = balanceMap.containsKey(stock.getSkuId()) ? balanceMap.get(stock.getSkuId()) : new AmountAndQuantityDto(0, new BigDecimal(0));
            AmountAndQuantityDto initialBalance = lastMonthBalanceMap.containsKey(stock.getSkuId()) ? lastMonthBalanceMap.get(stock.getSkuId()) : new AmountAndQuantityDto();

            Response<WarehouseStockStatisticsDto> statisticsResponse = doctorWarehouseReportReadService.countMaterialHandleByMaterialVendor(warehouseId, stock.getSkuId(), null, date,
                    WarehouseMaterialHandleType.IN,
                    WarehouseMaterialHandleType.OUT,
                    WarehouseMaterialHandleType.INVENTORY_PROFIT,
                    WarehouseMaterialHandleType.INVENTORY_DEFICIT,
                    WarehouseMaterialHandleType.TRANSFER_IN,
                    WarehouseMaterialHandleType.TRANSFER_OUT,
                    WarehouseMaterialHandleType.FORMULA_IN,
                    WarehouseMaterialHandleType.FORMULA_OUT
            );
            if (!statisticsResponse.isSuccess())
                throw new JsonResponseException(statisticsResponse.getError());


            WarehouseMonthlyReportVo vo = new WarehouseMonthlyReportVo();
            vo.setMaterialName(stock.getSkuName());

            if (skuMap.containsKey(stock.getSkuId())) {
                vo.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(skuMap.get(stock.getSkuId()).get(0).getVendorId())));

                DoctorBasic unit = RespHelper.or500(doctorBasicReadService.findBasicById(Long.parseLong(skuMap.get(stock.getSkuId()).get(0).getUnit())));
                if (null != unit)
                    vo.setUnit(unit.getName());
                vo.setSpecification(skuMap.get(stock.getSkuId()).get(0).getSpecification());
                vo.setCode(skuMap.get(stock.getSkuId()).get(0).getCode());
            }

            vo.setInAmount(statisticsResponse.getResult().getIn().getAmount()
                    .add(statisticsResponse.getResult().getInventoryProfit().getAmount())
                    .add(statisticsResponse.getResult().getTransferIn().getAmount())
                    .add(statisticsResponse.getResult().getFormulaIn().getAmount()));

            vo.setInQuantity(statisticsResponse.getResult().getIn().getQuantity()
                    .add(statisticsResponse.getResult().getInventoryProfit().getQuantity())
                    .add(statisticsResponse.getResult().getTransferIn().getQuantity())
                    .add(statisticsResponse.getResult().getFormulaIn().getQuantity()));

            vo.setOutAmount(statisticsResponse.getResult().getOut().getAmount()
                    .add(statisticsResponse.getResult().getInventoryDeficit().getAmount())
                    .add(statisticsResponse.getResult().getTransferOut().getAmount())
                    .add(statisticsResponse.getResult().getFormulaOut().getAmount()));
            vo.setOutQuantity(statisticsResponse.getResult().getOut().getQuantity()
                    .add(statisticsResponse.getResult().getInventoryDeficit().getQuantity())
                    .add(statisticsResponse.getResult().getTransferOut().getQuantity())
                    .add(statisticsResponse.getResult().getFormulaOut().getQuantity()));

            vo.setInitialAmount(initialBalance.getAmount());
            vo.setInitialQuantity(initialBalance.getQuantity());

//            vo.setBalanceAmount(initialBalance.getAmount() + balance.getAmount());
//            vo.setBalanceQuantity(initialBalance.getQuantity().add(balance.getQuantity()));
            vo.setBalanceAmount(initialBalance.getAmount().add(vo.getInAmount()).subtract(vo.getOutAmount()));
            vo.setBalanceQuantity(initialBalance.getQuantity().add(vo.getInQuantity()).subtract(vo.getOutQuantity()));

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
                                                                @RequestParam(required = false) Long orgId,
                                                                @RequestParam(required = false) Long farmId,
                                                                @RequestParam(required = false) String materialName,
                                                                @RequestParam(required = false) Integer type,
                                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {


        Map<String, Object> criteria = new HashMap<>();
        criteria.put("warehouseId", warehouseId);
        criteria.put("handleYear", date.get(Calendar.YEAR));
        criteria.put("handleMonth", date.get(Calendar.MONTH) + 1);
        criteria.put("deleteFlag", WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
        if (null != type) {
            if (WarehouseMaterialHandleType.IN.getValue() == type) {
                criteria.put("bigType", Lists.newArrayList(WarehouseMaterialHandleType.IN.getValue(), WarehouseMaterialHandleType.FORMULA_IN.getValue()));
            } else if (WarehouseMaterialHandleType.OUT.getValue() == type) {
                criteria.put("bigType", Lists.newArrayList(WarehouseMaterialHandleType.OUT.getValue(), WarehouseMaterialHandleType.FORMULA_OUT.getValue()));
            } else if (4 == type) {
                criteria.put("bigType", Lists.newArrayList(WarehouseMaterialHandleType.TRANSFER_IN.getValue(), WarehouseMaterialHandleType.TRANSFER_OUT.getValue()));
            } else if (3 == type) {
                criteria.put("bigType", Lists.newArrayList(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue(), WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue()));
            } else
                criteria.put("type", type);
        }

        if (StringUtils.isNotBlank(materialName)) {

            if (null == orgId && null == farmId)
                throw new JsonResponseException("warehouse.sku.org.id.or.farm.id.not.null");
            if (null == orgId) {
                DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
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
                return Collections.emptyList();
            criteria.put("skuIds", skuIds);
        }

        Response<List<DoctorWarehouseMaterialHandle>> materialHandleResponse = doctorWarehouseMaterialHandleReadService.advList(criteria);
        if (!materialHandleResponse.isSuccess())
            throw new JsonResponseException(materialHandleResponse.getError());

        Map<Long, List<DoctorWarehouseMaterialApply>> handleApply = RespHelper.or500(doctorWarehouseMaterialApplyReadService.month(warehouseId, date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, null)).stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialApply::getMaterialHandleId));


        Map<Long, List<DoctorWarehouseSku>> skuMap = RespHelper.or500(doctorWarehouseSkuReadService.findByIds(materialHandleResponse.getResult().stream().map(DoctorWarehouseMaterialHandle::getMaterialId).collect(Collectors.toList()))).stream().collect(Collectors.groupingBy(DoctorWarehouseSku::getId));

        List<WarehouseMaterialHandleVo> vos = new ArrayList<>(materialHandleResponse.getResult().size());
        for (DoctorWarehouseMaterialHandle handle : materialHandleResponse.getResult()) {

            String pigBarnName, pigGroupName = null;
            if (!handleApply.containsKey(handle.getId()))
                pigBarnName = pigGroupName = null;
            else {

                for (DoctorWarehouseMaterialApply a : handleApply.get(handle.getId())) {
                    if (a.getPigGroupId() != null)
                        pigGroupName = a.getPigGroupName();
                }
//                pigGroupName = handleApply.get(handle.getId()).get(0).getPigGroupName();
                pigBarnName = handleApply.get(handle.getId()).get(0).getPigBarnName();

            }

            WarehouseMaterialHandleVo handleVo = WarehouseMaterialHandleVo.builder()
                    .materialName(handle.getMaterialName())
                    .type(handle.getType())
                    .handleDate(handle.getHandleDate())
                    .quantity(handle.getQuantity())
                    .unitPrice(handle.getUnitPrice())
                    .pigBarnName(pigBarnName)
                    .pigGroupName(pigGroupName)
//                    .code(skuMap.get(handle.getMaterialId()).get(0).getCode())
//                    .vendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(skuMap.get(handle.getMaterialId()).get(0).getVendorId())))
//                    .specification(skuMap.get(handle.getMaterialId()).get(0).getSpecification())
//                    .unit(skuMap.get(handle.getMaterialId()).get(0).getUnit())
                    .warehouseName(handle.getWarehouseName())
                    .build();

            if (skuMap.containsKey(handle.getMaterialId())) {
                handleVo.setCode(skuMap.get(handle.getMaterialId()).get(0).getCode());
                handleVo.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(skuMap.get(handle.getMaterialId()).get(0).getVendorId())));
                handleVo.setSpecification(skuMap.get(handle.getMaterialId()).get(0).getSpecification());
                DoctorBasic unit = RespHelper.or500(doctorBasicReadService.findBasicById(Long.parseLong(skuMap.get(handle.getMaterialId()).get(0).getUnit())));
                handleVo.setUnit(null == unit ? "" : unit.getName());
//                handleVo.setUnit(skuMap.get(handle.getMaterialId()).get(0).getUnit());
            }

            if (WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == handle.getType()) {
                DoctorWarehouseMaterialHandle transferInHandle = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findById(handle.getRelMaterialHandleId()));
                handleVo.setTransferInWarehouseName(transferInHandle.getWarehouseName());
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

        if (null != pigBarnId)
            criteria.put("pigBarnId", pigBarnId);
        if (null != type) //物料类型
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

        criteria.put("applyType", WarehouseMaterialApplyType.BARN.getValue());
        Response<List<DoctorWarehouseMaterialApply>> applyResponse = doctorWarehouseMaterialApplyReadService.list(criteria);
        if (!applyResponse.isSuccess())
            throw new JsonResponseException(applyResponse.getError());

        Map<Long, List<DoctorWarehouseSku>> skuMap = RespHelper.or500(doctorWarehouseSkuReadService.findByIds(applyResponse.getResult().stream().map(DoctorWarehouseMaterialApply::getMaterialId).collect(Collectors.toList()))).stream().collect(Collectors.groupingBy(DoctorWarehouseSku::getId));

        List<WarehouseMaterialApplyVo> vos = new ArrayList<>(applyResponse.getResult().size());
        for (DoctorWarehouseMaterialApply apply : applyResponse.getResult()) {

            WarehouseMaterialApplyVo vo = new WarehouseMaterialApplyVo();
            BeanUtils.copyProperties(apply, vo);
            if (skuMap.containsKey(apply.getMaterialId())) {
                DoctorBasic unit = RespHelper.or500(doctorBasicReadService.findBasicById(Long.parseLong(skuMap.get(apply.getMaterialId()).get(0).getUnit())));
                vo.setUnit(null == unit ? "" : unit.getName());
                vo.setCode(skuMap.get(apply.getMaterialId()).get(0).getCode());
                vo.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(skuMap.get(apply.getMaterialId()).get(0).getVendorId())));
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
                                                        @RequestParam(required = false) Long pigBarnId,
                                                        @RequestParam(required = false) Integer materialType,
                                                        @RequestParam(required = false) String materialName,
                                                        @RequestParam(required = false) Date pigGroupCreateDateStart,
                                                        @RequestParam(required = false) Date pigGroupCreateDateEnd,
                                                        @RequestParam(required = false) Date pigGroupCloseDateStart,
                                                        @RequestParam(required = false) Date pigGroupCloseDateEnd) {


        if (null != pigGroupCreateDateStart && null != pigGroupCreateDateEnd
                && pigGroupCreateDateStart.after(pigGroupCreateDateEnd)) {
            throw new JsonResponseException("start.date.after.end.date");
        }
        if (null != pigGroupCloseDateStart && null != pigGroupCloseDateEnd
                && pigGroupCloseDateStart.after(pigGroupCloseDateEnd))
            throw new JsonResponseException("start.date.after.end.date");

        List<Long> pigGroupIds = null;
        if (null != pigGroupCreateDateStart || null != pigGroupCreateDateEnd || null != pigGroupCloseDateStart || null != pigGroupCloseDateEnd) {

            DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(warehouseId));
            if (null == wareHouse)
                throw new JsonResponseException("warehouse.not.found");

            Map<String, Object> params = new HashMap<>();
            params.put("startOpenAt", pigGroupCreateDateStart);  //建群开始时间
            params.put("endOpenAt", pigGroupCreateDateEnd);      //建群结束时间
            params.put("startCloseAt", pigGroupCloseDateStart);  //关群开始时间
            params.put("endCloseAt", pigGroupCloseDateEnd);      //关群结束时间
            params.put("farmId", wareHouse.getFarmId());
            pigGroupIds = RespHelper.or500(doctorGroupReadService.findGroup(params)).stream().map(DoctorGroup::getId).collect(Collectors.toList());
            if (pigGroupIds.isEmpty())
                return Collections.emptyList();
        }

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("warehouseId", warehouseId);
        criteria.put("type", materialType);
        criteria.put("pigBarnId", pigBarnId);
        if (null != pigGroupIds) {
            if (null != pigGroupId) {
                if (!pigGroupIds.contains(pigGroupId))
                    return Collections.emptyList();
                else
                    criteria.put("pigGroupId", pigGroupId);
            } else
                criteria.put("pigGroupIds", pigGroupIds);
        } else if (null != pigGroupId)
            criteria.put("pigGroupId", pigGroupId);
        else
//            criteria.put("groupOrBarn", "group");
            criteria.put("applyType", WarehouseMaterialApplyType.GROUP.getValue());

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
                    .amount(apply.getQuantity().multiply(apply.getUnitPrice()))
                    .applyDate(apply.getApplyDate())
                    .build();

            if (skuMap.containsKey(apply.getMaterialId())) {
                DoctorBasic unit = RespHelper.or500(doctorBasicReadService.findBasicById(Long.parseLong(skuMap.get(apply.getMaterialId()).get(0).getUnit())));
                applyVo.setUnit(null == unit ? "" : unit.getName());
                applyVo.setCode(skuMap.get(apply.getMaterialId()).get(0).getCode());
                applyVo.setSpecification(skuMap.get(apply.getMaterialId()).get(0).getSpecification());
                applyVo.setVendorName(RespHelper.or500(doctorWarehouseVendorReadService.findNameById(skuMap.get(apply.getMaterialId()).get(0).getVendorId())));
            }

            vos.add(applyVo);
        }

        return vos;
    }

//    public static void main(String[] args) {
////        System.out.println(isDecimal("aaa"));
////         System.out.println(isWholeNumber("0"));
////         System.out.println(isDecimal("0.00"));
////         System.out.println(Double.parseDouble("0.00") == 0d);
////         System.out.println(Double.parseDouble("0") == 0d);
////        System.out.println(Double.parseDouble("0.00000") == 0d);
////        System.out.println(Double.parseDouble("-90.78"));
////        BigDecimal   b   =   new   BigDecimal(-90.053d);
////        double   f1   =   b.setScale(2,   RoundingMode.HALF_UP).doubleValue();
////        System.out.println(f1);
////        System.out.println(isNumeric("-10.00"));
////        System.out.println(isNumeric("0.00"));
////        System.out.println(isNumeric(""));
////        System.out.println(isValidDate(""));
////        System.out.println(isNumeric(null));
////        System.out.println(isValidDate(null));
////        System.out.println(Double.parseDouble("0") == 0);
////        BigDecimal a = new BigDecimal(20048908023123123988d);
////        BigDecimal b = new BigDecimal(23223212143214231213213d);
////        System.out.println(a.divide(b, 4, RoundingMode.HALF_UP));
//        System.out.println(new BigDecimal(1000000000.00d));
//        System.out.println(new BigDecimal(1000000000.00d).setScale(4, BigDecimal.ROUND_HALF_UP));
//    }

    @RpcConsumer
    private DoctorWarehouseSettlementService doctorWarehouseSettlementService;

    /**
     * 物料变动报表
     */
    // var params = {farmId:farmId,settlementDate:settlementDate,orgId:orgId,warehouseId:warehouseId,materialName:materialName,type:type};
    @RequestMapping(method = RequestMethod.GET, value = "/wlbdReport")
    public List<Map<String,Object>> wlbdReport(
            Long orgId,
            Long farmId,
            String settlementDate,
//            Integer pigBarnType,
//            Long pigBarnId,
//            Long pigGroupId,
//            Integer handlerType,
            Integer type,
            Long warehouseId,
            String materialName
    ) throws ParseException {

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
        Date dd = sdf.parse(settlementDate);

        if(ObjectUtils.isEmpty(farmId))
        {
            throw new JsonResponseException("stock.farmId.null");
        }

        if(ObjectUtils.isEmpty(settlementDate))
        {
            throw new JsonResponseException("stock.settlementDate.null");
        }

        if(ObjectUtils.isEmpty(warehouseId))
        {
            throw new JsonResponseException("stock.warehouseId.null");
        }

        boolean byjsflag = false; //默认没结算
        try {
            if (doctorWarehouseSettlementService.isSettled(orgId, dd)) {
                byjsflag = true; //表示已经结算了
            }
        } catch (Exception e){

        }

        // 最终结果集数据集合
        List<Map<String, Object>> resultNewMap = Lists.newArrayList();

        List<String> materials = Lists.newArrayList();
        if(StringUtils.isBlank(materialName))
        {
            List<Map<String,Object>> resultMap = doctorWarehouseReportReadService.getMeterails(
                    farmId,settlementDate, type,warehouseId,materialName
            );

            if(resultMap.size() > 0)
            {
                for(Map<String,Object> map:resultMap){
                    materials.add(String.valueOf(map.get("material_name")));
                }
            }
            else {
                return resultNewMap;
            }
        } else {
            materials.add(materialName);
        }

        if(materials.size() > 0) {

            for(String str : materials) {

                // 查出上月结存数据
                Map<String, Object> lastMap = doctorWarehouseReportReadService.lastWlbdReport(farmId, settlementDate, type, warehouseId, str);
                if (lastMap == null) {
                    // 构造上月结存新map数据
                    lastMap = Maps.newHashMap();
                    lastMap.put("id","");
                    lastMap.put("material_name","上月结存");
                    lastMap.put("material_type","");
                    lastMap.put("ware_house_name","");
                    lastMap.put("handle_date","");
                    lastMap.put("settlement_date","");
                    lastMap.put("handler_type","");
                    lastMap.put("rksl","");
                    lastMap.put("rkdj","");
                    lastMap.put("rkje","");
                    lastMap.put("cksl","");
                    if(byjsflag) {
                        lastMap.put("ckdj", "");
                        lastMap.put("ckje", "");
                    }else{ //结算前
                        lastMap.put("ckdj", "--");
                        lastMap.put("ckje", "--");
                    }
                    lastMap.put("jcsl","");
                    if(byjsflag) {
                        lastMap.put("jcdj", "");
                        lastMap.put("jcje", "");
                    }else{ //结算前
                        lastMap.put("jcdj", "--");
                        lastMap.put("jcje", "--");
                    }
                    lastMap.put("pig_barn_name","");
                    lastMap.put("pig_type","");
                    lastMap.put("pig_group_name","");
                    lastMap.put("apply_staff_name","");
                    lastMap.put("farm_name","");
                    lastMap.put("unit","");
                    lastMap.put("provider_name","");
                    lastMap.put("specification","");
                } else {
                    Object lckdj = lastMap.get("ckdj");
                    Object lckje = lastMap.get("ckje");
                    if(byjsflag) {
                        if (isNull(lckdj)) {
                            lastMap.put("ckdj", "");
                        } else {
                            lastMap.put("ckdj",
                                    new BigDecimal(Double.parseDouble(lckdj.toString())).setScale(4, BigDecimal.ROUND_HALF_UP));
                        }
                        if (isNull(lckje)) {
                            lastMap.put("ckje", "");
                        } else {
                            lastMap.put("ckje",
                                    new BigDecimal(Double.parseDouble(lckje.toString())).setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                    } else { //结算前
                        lastMap.put("ckdj", "--");
                        lastMap.put("ckje", "--");
                    }

                    Object ljcsl = lastMap.get("jcsl");
                    Object ljcdj = lastMap.get("jcdj");
                    Object ljcje = lastMap.get("jcje");
                    if(isNull(ljcsl)){
                        BigDecimal quantity = RespHelper.or500(doctorWarehouseMaterialHandleReadService.findWJSQuantity(BigInteger.valueOf(warehouseId),null,null,type,str,dd));
                        lastMap.put("jcsl",quantity);
                    } else {
                        lastMap.put("jcsl",
                                new BigDecimal(Double.parseDouble(ljcsl.toString())).setScale(3, BigDecimal.ROUND_HALF_UP));
                    }
                    if(byjsflag) {
                        if (isNull(ljcdj)) {
                            lastMap.put("jcdj", "");
                        } else {
                            lastMap.put("jcdj",
                                    new BigDecimal(Double.parseDouble(ljcdj.toString())).setScale(4, BigDecimal.ROUND_HALF_UP));
                        }
                        if (isNull(ljcje)) {
                            lastMap.put("jcje", "");
                        } else {
                            lastMap.put("jcje",
                                    new BigDecimal(Double.parseDouble(ljcje.toString())).setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                    } else {
                        lastMap.put("jcdj", "--");
                        lastMap.put("jcje", "--");
                    }
                }
                resultNewMap.add(lastMap);

                List<Map<String, Object>> resultMap = doctorWarehouseReportReadService.wlbdReport(
                        farmId, settlementDate, type, warehouseId, str
                );

                // 计算这条物料的结存数据与汇总数据
                if (resultMap.size() > 0) {

                    //本月汇总变量定义
                    List<Map<String, Object>> resultEndMap = Lists.newArrayList();
                    // 入库
                    BigDecimal thisMonthTotalRksl = new BigDecimal(0d);
                    BigDecimal thisMonthTotalRkje = new BigDecimal(0d);
                    // 出库
                    BigDecimal thisMonthTotalCksl = new BigDecimal(0d);
                    BigDecimal thisMonthTotalCkje = new BigDecimal(0d);
                    // 结存
                    BigDecimal thisMonthTotalJcsl = new BigDecimal(0d);
                    BigDecimal thisMonthTotalJcje = new BigDecimal(0d);

                    // 上月结存数据、金额
                    BigDecimal lastMonthTotalJcsl = isNull(lastMap.get("jcsl")) || "--".equals(lastMap.get("jcsl").toString()) ? new BigDecimal(0d) :
                            new BigDecimal(Double.parseDouble(lastMap.get("jcsl").toString()));
                    BigDecimal lastMonthTotalJcje = isNull(lastMap.get("jcje")) || "--".equals(lastMap.get("jcje").toString()) ? new BigDecimal(0d) :
                            new BigDecimal(Double.parseDouble(lastMap.get("jcje").toString()));

                    // 临时变量
                    BigDecimal tempsinglejcsl = lastMonthTotalJcsl;
                    BigDecimal tempsinglejcje = lastMonthTotalJcje;

                    for(int i = 0;i < resultMap.size();i++){

                        Map<String, Object> thismap = resultMap.get(i);

                        //获取下一条数据
                        Map<String,Object> nextmap = null;
                        if(i < resultMap.size() - 1) {
                            nextmap = resultMap.get(i + 1);
                        }

                        if(null != thismap && null != nextmap) {

                            String thisId = isBlank(thismap.get("id")) ? "" : thismap.get("id").toString().trim();
                            String thisMaterName = isBlank(thismap.get("material_name")) ? "" : thismap.get("material_name").toString().trim();
                            String thisMaterType = isBlank(thismap.get("material_type")) ? "" : thismap.get("material_type").toString().trim();
                            String thisWareHouseName = isBlank(thismap.get("ware_house_name")) ? "" : thismap.get("ware_house_name").toString().trim();
                            String thisHandleDate = isBlank(thismap.get("handle_date")) ? "" : thismap.get("handle_date").toString().trim();
                            String thisSettlementDate = isBlank(thismap.get("settlement_date")) ? "" : thismap.get("settlement_date").toString().trim();
                            String thisHandlerType = isBlank(thismap.get("handler_type")) ? "" : thismap.get("handler_type").toString().trim();
                            String thisPigBarnName = isBlank(thismap.get("pig_barn_name")) ? "" : thismap.get("pig_barn_name").toString().trim();
                            String thisPigType = isBlank(thismap.get("pig_type")) ? "" : thismap.get("pig_type").toString().trim();
                            String thisPigGroupName = isBlank(thismap.get("pig_group_name")) ? "" : thismap.get("pig_group_name").toString().trim();

                            String nextId = isBlank(nextmap.get("id")) ? "" : nextmap.get("id").toString().trim();
                            String nextMaterName = isBlank(nextmap.get("material_name")) ? "" : nextmap.get("material_name").toString().trim();
                            String nextMaterType = isBlank(nextmap.get("material_type")) ? "" : nextmap.get("material_type").toString().trim();
                            String nextWareHouseName = isBlank(nextmap.get("ware_house_name")) ? "" : nextmap.get("ware_house_name").toString().trim();
                            String nextHandleDate = isBlank(nextmap.get("handle_date")) ? "" : nextmap.get("handle_date").toString().trim();
                            String nextSettlementDate = isBlank(nextmap.get("settlement_date")) ? "" : nextmap.get("settlement_date").toString().trim();
                            String nextHandlerType = isBlank(nextmap.get("handler_type")) ? "" : nextmap.get("handler_type").toString().trim();
                            String nextPigBarnName = isBlank(nextmap.get("pig_barn_name")) ? "" : nextmap.get("pig_barn_name").toString().trim();
                            String nextPigType = isBlank(nextmap.get("pig_type")) ? "" : nextmap.get("pig_type").toString().trim();

                            if(thisId.equals(nextId)
                                    && thisMaterName.equals(nextMaterName)
                                    && thisMaterType.equals(nextMaterType)
                                    && thisWareHouseName.equals(nextWareHouseName)
                                    && thisHandleDate.equals(nextHandleDate)
                                    && thisSettlementDate.equals(nextSettlementDate)
                                    && thisHandlerType.equals(nextHandlerType)
                                    && thisPigBarnName.equals(nextPigBarnName)
                                    && thisPigType.equals(nextPigType)
                                    && "".equals(thisPigGroupName))
                            {
                                continue;
                            }

                        }

                        Map<String, Object> tempmap = thismap;

                        // 计算每一条明细的结存数据
                        Object rksl = thismap.get("rksl");
                        Object rkje = thismap.get("rkje");
                        Object cksl = thismap.get("cksl");
                        Object ckje = thismap.get("ckje");

                        BigDecimal drksl = isNull(rksl) ? new BigDecimal(0d) :  new BigDecimal(rksl.toString());
                        BigDecimal drkje = isNull(rkje) ? new BigDecimal(0d) :  new BigDecimal(rkje.toString());
                        BigDecimal dcksl = isNull(cksl) ? new BigDecimal(0d) :  new BigDecimal(cksl.toString());
                        BigDecimal dckje = isNull(ckje) ? new BigDecimal(0d) :  new BigDecimal(ckje.toString());
                        tempmap.put("rksl",isNull(rksl) ? "" :  new BigDecimal(rksl.toString()).setScale(3, BigDecimal.ROUND_HALF_UP));
                        tempmap.put("cksl",isNull(cksl) ? "" :  new BigDecimal(cksl.toString()).setScale(3, BigDecimal.ROUND_HALF_UP));
                        tempmap.put("rkje",isNull(rkje) ? "" :  new BigDecimal(rkje.toString()).setScale(2, BigDecimal.ROUND_HALF_UP));

                        if(byjsflag) {
                            tempmap.put("ckje", isNull(ckje) ? "" : new BigDecimal(ckje.toString()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        } else {
                            tempmap.put("ckje","--");
                        }

                        thisMonthTotalRksl.add(drksl); //入库数量累加
                        thisMonthTotalRkje.add(drkje); //入库金额累加

                        if(drksl.compareTo(BigDecimal.ZERO) == 0 || drkje.compareTo(BigDecimal.ZERO) == 0){
                            tempmap.put("rkdj","");
                        } else {
                            tempmap.put("rkdj", drkje.divide(drksl, 4, BigDecimal.ROUND_HALF_UP));
                        }

                        thisMonthTotalCksl.add(dcksl); //出库数量累加
                        thisMonthTotalCkje.add(dckje); //出库金额累加

                        if(dcksl.compareTo(BigDecimal.ZERO) == 0 || dckje.compareTo(BigDecimal.ZERO) == 0){
                            if(byjsflag) {
                                tempmap.put("ckdj", "");
                            }else{
                                tempmap.put("ckdj", "--");
                            }
                        } else {
                            if(byjsflag) {
                                tempmap.put("ckdj", dckje.divide(dcksl, 4, BigDecimal.ROUND_HALF_UP));
                            }else {
                                tempmap.put("ckdj", "--");
                            }
                        }

                        BigDecimal singleJcsl = new BigDecimal(0d);
                        BigDecimal singleJcje = new BigDecimal(0d);

                        if(drksl.compareTo(BigDecimal.ZERO) == 1) { //表示大于0
                            singleJcsl = tempsinglejcsl.add(drksl); //加法
                        }
                        else if(dcksl.compareTo(BigDecimal.ZERO) !=0&&!dcksl.equals("")) {//表示不等于0
                            singleJcsl = tempsinglejcsl.subtract(dcksl); //减法
                        }
                        else {
                            singleJcsl = tempsinglejcsl;
                        }

                        if(drkje.compareTo(BigDecimal.ZERO) == 1) {
                            singleJcje = tempsinglejcje.add(drkje);
                        }
                        else if(dckje.compareTo(BigDecimal.ZERO) != 0&&!dcksl.equals("")) {
                            singleJcje = tempsinglejcje.subtract(dckje);
                        }
                        else {
                            singleJcje = tempsinglejcje;
                        }

                        tempmap.put("jcsl",singleJcsl.compareTo(BigDecimal.ZERO) == 0 ? "" :
                                singleJcsl.setScale(3, BigDecimal.ROUND_HALF_UP)); //单笔记录的结存数量
                        //物料变动报表结存的数量为0的之后，单价和金额也展示0
                        if(singleJcsl.compareTo(BigDecimal.ZERO) == 0||singleJcje.compareTo(BigDecimal.ZERO) == 0){
                            tempmap.put("jcje","");
                            tempmap.put("jcdj", "");
                        }else {
                            if (byjsflag) {
                                tempmap.put("jcje", singleJcje.compareTo(BigDecimal.ZERO) == 0 ? "" :
                                        singleJcje.setScale(2, BigDecimal.ROUND_HALF_UP)); //单笔记录的结存金额
                            } else {
                                tempmap.put("jcje", "--");
                            }
                            if (byjsflag) {
                                tempmap.put("jcdj", singleJcje.divide(singleJcsl, 4, BigDecimal.ROUND_HALF_UP));
                            } else {
                                tempmap.put("jcdj", "--");
                            }
                        }
                        resultNewMap.add(tempmap);
                        tempsinglejcsl = singleJcsl;
                        tempsinglejcje = singleJcje;
                    }

                    // 结存数量、金额等于最后一笔
                    thisMonthTotalJcsl = tempsinglejcsl;
                    thisMonthTotalJcje = tempsinglejcje;

                    Map thisMap = Maps.newHashMap();
                    thisMap.put("id","");
                    thisMap.put("material_name","本月结存");
                    thisMap.put("material_type","");
                    thisMap.put("ware_house_name","");
                    thisMap.put("handle_date","");
                    thisMap.put("settlement_date","");
                    thisMap.put("handler_type","");
                    thisMap.put("rksl",thisMonthTotalRksl.compareTo(BigDecimal.ZERO) == 0
                                        ? "" : thisMonthTotalRksl.setScale(3, BigDecimal.ROUND_HALF_UP));
                    thisMap.put("rkdj",thisMonthTotalRksl.compareTo(BigDecimal.ZERO) == 0 || thisMonthTotalRkje.compareTo(BigDecimal.ZERO) == 0
                                        ? "" : thisMonthTotalRkje.divide(thisMonthTotalRksl, 4, BigDecimal.ROUND_HALF_UP));
                    thisMap.put("rkje",thisMonthTotalRkje.compareTo(BigDecimal.ZERO) == 0
                                    ? "" : thisMonthTotalRkje.setScale(2, BigDecimal.ROUND_HALF_UP));
                    thisMap.put("cksl",
                            thisMonthTotalCksl.compareTo(BigDecimal.ZERO) == 0
                                    ? "" : thisMonthTotalCksl.setScale(3, BigDecimal.ROUND_HALF_UP));
                    if(byjsflag) {
                        thisMap.put("ckdj", thisMonthTotalCksl.compareTo(BigDecimal.ZERO) == 0
                                || thisMonthTotalCkje.compareTo(BigDecimal.ZERO) == 0 ? "" :
                                thisMonthTotalCkje.divide(thisMonthTotalCksl, 4, BigDecimal.ROUND_HALF_UP));
                        thisMap.put("ckje", thisMonthTotalCkje.compareTo(BigDecimal.ZERO) == 0 ? "" :
                                thisMonthTotalCkje.setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    else {
                        thisMap.put("ckdj","--");
                        thisMap.put("ckje","--");
                    }

                    thisMap.put("jcsl", thisMonthTotalJcsl.compareTo(BigDecimal.ZERO) == 0 ? "" :
                            thisMonthTotalJcsl.setScale(3, BigDecimal.ROUND_HALF_UP));
                    if(byjsflag) {
                        thisMap.put("jcdj", thisMonthTotalJcsl.compareTo(BigDecimal.ZERO) == 0 || thisMonthTotalJcje.compareTo(BigDecimal.ZERO) == 0 ? "" :
                                thisMonthTotalJcje.divide(thisMonthTotalJcsl, 4, BigDecimal.ROUND_HALF_UP));
                        thisMap.put("jcje", thisMonthTotalJcje.compareTo(BigDecimal.ZERO) == 0 ? "" :
                                thisMonthTotalJcje.setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    else{
                        thisMap.put("jcdj","--");
                        thisMap.put("jcje","--");
                    }

                    thisMap.put("pig_barn_name","");
                    thisMap.put("pig_type","");
                    thisMap.put("pig_group_name","");
                    thisMap.put("apply_staff_name","");
                    thisMap.put("farm_name","");
                    thisMap.put("unit","");
                    thisMap.put("provider_name","");
                    thisMap.put("specification","");
                    resultNewMap.add(thisMap);
                }
            }
        }
        return resultNewMap;
    }

    private static boolean isBlank(Object str){
        try {
            if (null == str ||  "".equals(str.toString().trim())) {
                return true;
            }
        }
        catch (Exception e){
            return false;
        }
        return false;
    }

    private static boolean isNull(Object str){
        try {
            if (str == null
                    || str.toString().trim().equals("")
                    || Double.parseDouble(str.toString().trim()) == 0d
                    ) return true;
        }
        catch (Exception e){
            return false;
        }
        return false;
    }

    public static boolean isNumeric(String str){
        if(str == null || str.trim().equals("")) return  false;
        try {
            double b = Double.parseDouble(str);
            return true;
        }
        catch (Exception e){
            return  false;
        }
    }

    /**
     * 数字处理 start
     * @param regex
     * @param orginal
     * @return
     */
    private static boolean isMatch(String regex, String orginal){
        if (orginal == null || orginal.trim().equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher isNum = pattern.matcher(orginal);
        return isNum.matches();
    }

    public static boolean isPositiveInteger(String orginal) {
        return isMatch("^\\+{0,1}[1-9]\\d*", orginal);
    }

    public static boolean isNegativeInteger(String orginal) {
        return isMatch("^-[1-9]\\d*", orginal);
    }

    public static boolean isWholeNumber(String orginal) {
        return isMatch("[+-]{0,1}0", orginal) || isPositiveInteger(orginal) || isNegativeInteger(orginal);
    }

    public static boolean isDecimal(String orginal){
        return isMatch("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+", orginal);
    }

    private static String translateNum(String numvalue,int seq){
        if(null == numvalue || "".equals(numvalue.trim()) || Double.parseDouble(numvalue) == 0d) return "";

        if(isPositiveInteger(numvalue) && Double.parseDouble(numvalue) != 0d){
            if(seq == 2) {
                numvalue = numvalue + ".00";
            } else {
                numvalue = numvalue + ".0000";
            }
        } else if(isDecimal(numvalue) && Double.parseDouble(numvalue) != 0d){
            BigDecimal b = new BigDecimal(Double.parseDouble(numvalue));
            double f1 = b.setScale(seq, RoundingMode.HALF_UP).doubleValue();
            numvalue = f1 + "";
            if(numvalue.contains(".")){
                String prex = numvalue.substring(0,numvalue.indexOf(".") + 1);
                String sk = numvalue.substring(numvalue.indexOf(".") + 1);
                if(sk.length() < seq){
                    int diff = seq - sk.length();
                    String end = "";
                    for(int i = 0;i < diff;i++){
                        end += "0";
                    }
                    numvalue = prex + sk + end;
                }
            }
            else {
                if(seq == 2) {
                    numvalue = numvalue + ".00";
                } else {
                    numvalue = numvalue + ".0000";
                }
            }
        }
        return numvalue;
    }
    /**
     * end
     */

    @Autowired
    private Exporter exporter;

    //导出
    @RequestMapping(method = RequestMethod.GET, value = "/wlbdReport/export")
    public void exportWlbdReport(
            Long orgId,
            Long farmId,
            String settlementDate,
            Integer pigBarnType,
            Long pigBarnId,
            Long pigGroupId,
            Integer handlerType,
            Integer type,
            Long warehouseId,
            String materialName,
            HttpServletRequest request, HttpServletResponse response) {

        //开始导出
        try {

            if(ObjectUtils.isEmpty(farmId))
            {
                throw new JsonResponseException("stock.farmId.null");
            }

            if(ObjectUtils.isEmpty(settlementDate))
            {
                throw new JsonResponseException("stock.settlementDate.null");
            }

            if(ObjectUtils.isEmpty(warehouseId))
            {
                throw new JsonResponseException("stock.warehouseId.null");
            }

            DoctorFarm farm = doctorFarmReadService.findFarmById(farmId).getResult();
            if (null == farm) {
                throw new JsonResponseException("farm.not.found");
            }

            String farmName = farm.getName();
            String fileName = "物料变动报表" + new Date().getTime();

            boolean byjsflag = false; //默认没结算
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
            try {
                Date ddate = simpleDateFormat.parse(settlementDate);
                if (doctorWarehouseSettlementService.isSettled(orgId, ddate)) {
                    byjsflag = true; //表示已经结算了
                }
            } catch (Exception e){

            }

            //导出名称
            exporter.setHttpServletResponse(request, response, fileName);

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {

                //设置表头标题
                Sheet sheet = workbook.createSheet();

                String fontName = "微软雅黑";

                XSSFFont titleFont = workbook.createFont();
                titleFont.setFontName(fontName);  // 设置字体
                titleFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD); // 字体加粗

                XSSFFont normalFont = workbook.createFont();
                normalFont.setFontName(fontName);

                XSSFDataFormat format = workbook.createDataFormat();

                //表头样式
                XSSFCellStyle titleCellStyle = workbook.createCellStyle();
                //样式居中
                titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                titleCellStyle.setDataFormat(format.getFormat("@"));
                titleCellStyle.setFont(titleFont);

                //数据样式
                XSSFCellStyle normalCellStyle = workbook.createCellStyle();
                //样式居中
                normalCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                normalCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                normalCellStyle.setDataFormat(format.getFormat("@"));
                normalCellStyle.setFont(normalFont);

                XSSFCellStyle leftCellStyle = workbook.createCellStyle();
                //样式居左
                leftCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                leftCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                leftCellStyle.setDataFormat(format.getFormat("@"));
                leftCellStyle.setFont(normalFont);

                //设置表头列
                Row titleRow =  sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("物料名称");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(1);
                titleCell.setCellValue("物料类型");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(2);
                titleCell.setCellValue("仓库名称");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(3);
                titleCell.setCellValue("事件日期");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(4);
                titleCell.setCellValue("会计年月");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(5);
                titleCell.setCellValue("事件类型");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(6);
                titleCell.setCellValue("入库");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(7);
                titleCell = titleRow.createCell(8);
                titleCell = titleRow.createCell(9);
                titleCell.setCellValue("出库");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(10);
                titleCell = titleRow.createCell(11);
                titleCell = titleRow.createCell(12);
                titleCell.setCellValue("结存");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(13);
                titleCell = titleRow.createCell(14);
                int vb = 15 ;
                titleCell = titleRow.createCell(vb);
                titleCell.setCellValue("猪舍名称");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(vb + 1);
                titleCell.setCellValue("猪舍类型");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(vb + 2);
                titleCell.setCellValue("猪群名称");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(vb + 3);
                titleCell.setCellValue("饲养员");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(vb + 4);
                titleCell.setCellValue("猪场名称");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(vb + 5);
                titleCell.setCellValue("单位");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(vb + 6);
                titleCell.setCellValue("厂家");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(vb + 7);
                titleCell.setCellValue("规格");
                titleCell.setCellStyle(titleCellStyle);

                titleRow =  sheet.createRow(1);
                titleCell = titleRow.createCell(0);
                titleCell = titleRow.createCell(1);
                titleCell = titleRow.createCell(2);
                titleCell = titleRow.createCell(3);
                titleCell = titleRow.createCell(4);
                titleCell = titleRow.createCell(5);
                titleCell = titleRow.createCell(6);
                titleCell.setCellValue("数量");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(7);
                titleCell.setCellValue("单价");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(8);
                titleCell.setCellValue("金额");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(9);
                titleCell.setCellValue("数量");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(10);
                titleCell.setCellValue("单价");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(11);
                titleCell.setCellValue("金额");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(12);
                titleCell.setCellValue("数量");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(13);
                titleCell.setCellValue("单价");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(14);
                titleCell.setCellValue("金额");
                titleCell.setCellStyle(titleCellStyle);
                int vh = 15;
                titleCell = titleRow.createCell(vh);
                titleCell = titleRow.createCell(vh + 1);
                titleCell = titleRow.createCell(vh + 2);
                titleCell = titleRow.createCell(vh + 3);
                titleCell = titleRow.createCell(vh + 4);
                titleCell = titleRow.createCell(vh + 5);
                titleCell = titleRow.createCell(vh + 6);
                titleCell = titleRow.createCell(vh + 7);

                int fis = 0;
                int sec = 1;
                ///第一行第一列到第五列与第二行第一列到第五列进行合并
                for(int i=0;i <= 5;i++){
                    CellRangeAddress cra = new CellRangeAddress(fis, sec, i, i);
                    sheet.addMergedRegion(cra);
                }

                CellRangeAddress cra = new CellRangeAddress(fis, fis, 6, 8);
                sheet.addMergedRegion(cra);
                cra = new CellRangeAddress(fis, fis, 9, 11);
                sheet.addMergedRegion(cra);

                cra = new CellRangeAddress(fis, fis, 12, 14);
                sheet.addMergedRegion(cra);

                int vg = 15;
                int ebvy = vg + 7;
                for(int i = vg;i <= ebvy;i++){
                    cra = new CellRangeAddress(fis, sec, i, i);
                    sheet.addMergedRegion(cra);
                }

                List<Map<String,Object>> exportVos = wlbdReport(orgId,farmId,settlementDate, type,warehouseId,materialName);

                //往excel表中写入数据
                if(null != exportVos && !CollectionUtils.isEmpty(exportVos)) {

                    Row dataRow = null;
                    Cell dataCell = null;
                    int startRowIndex = 2;

                    for (Map<String, Object> map : exportVos) {
                        String tName = String.valueOf(map.get("material_name"));
                        if ("上月结存".equals(tName)) {
                            //只显示结存数据,从第二行开始创建起
                            dataRow = sheet.createRow(startRowIndex);
                            for (int i = 0; i <= 22; i++) {
                                if (i == 0) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(tName);
                                    dataCell.setCellStyle(leftCellStyle);
                                }

                                if ((i > 0 && i < 12) || (i > (vg - 1)  && i <= ebvy)) {
                                    dataCell = dataRow.createCell(i);
                                }

                                if (i == 12) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("jcsl"))
                                                    ? "" : map.get("jcsl").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 13) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("jcdj"))
                                                    ? "" : map.get("jcdj").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 14) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("jcje"))
                                                    ? "" : map.get("jcje").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                            }

                            //合并单元格
                            cra = new CellRangeAddress(startRowIndex, startRowIndex, 0, 11);
                            sheet.addMergedRegion(cra);
                            cra = new CellRangeAddress(startRowIndex, startRowIndex, vg, ebvy);
                            sheet.addMergedRegion(cra);
                        } else if ("本月结存".equals(tName)) {
                            dataRow = sheet.createRow(startRowIndex);
                            for (int i = 0; i <= ebvy; i++) {

                                if (i == 0) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(tName);
                                    dataCell.setCellStyle(leftCellStyle);
                                }

                                if ((i > 0 && i < 6) || (i > (vg - 1) && i <= ebvy)) {
                                    dataCell = dataRow.createCell(i);
                                }

                                if (i == 6) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("rksl"))
                                                    ? "" : map.get("rksl").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 7) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("rkdj"))
                                                    ? "" : map.get("rkdj").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 8) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("rkje"))
                                                    ? "" : map.get("rkje").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 9) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("cksl"))
                                                    ? "" : map.get("cksl").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 10) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("ckdj"))
                                                    ? "" : map.get("ckdj").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 11) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("ckje"))
                                                    ? "" : map.get("ckje").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 12) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("jcsl"))
                                                    ? "" : map.get("jcsl").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 13) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("jcdj"))
                                                    ? "" : map.get("jcdj").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                                if (i == 14) {
                                    dataCell = dataRow.createCell(i);
                                    dataCell.setCellValue(
                                            isNull(map.get("jcje"))
                                                    ? "" : map.get("jcje").toString());
                                    dataCell.setCellStyle(normalCellStyle);
                                    dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                }

                            }

                            //合并单元格
                            cra = new CellRangeAddress(startRowIndex, startRowIndex, 0, 5);
                            sheet.addMergedRegion(cra);
                            cra = new CellRangeAddress(startRowIndex, startRowIndex, vg, ebvy);
                            sheet.addMergedRegion(cra);
                        } else {
                            // 写入表数据
                            dataRow = sheet.createRow(startRowIndex);

                            dataCell = dataRow.createCell(0);
                            String mname = String.valueOf(map.get("material_name"));
                            mname = null == mname || "".equals(mname.trim()) || "null".equals(mname.trim().toLowerCase()) ? "" : mname.trim();
                            dataCell.setCellValue(mname);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(1);
                            String mtype = String.valueOf(map.get("material_type"));
                            mtype = null == mtype || "".equals(mtype.trim()) || "null".equals(mtype.trim().toLowerCase()) ? "" : mtype.trim();
                            switch (mtype) {
                                case "1":
                                    dataCell.setCellValue("饲料");
                                    break;
                                case "2":
                                    dataCell.setCellValue("原料");
                                    break;
                                case "3":
                                    dataCell.setCellValue("疫苗");
                                    break;
                                case "4":
                                    dataCell.setCellValue("药品");
                                    break;
                                case "5":
                                    dataCell.setCellValue("消耗品");
                                    break;
                                default:
                                    dataCell.setCellValue("");
                                    break;
                            }
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(2);
                            String housename = String.valueOf(map.get("ware_house_name"));
                            housename = null == housename || "".equals(housename.trim()) || "null".equals(housename.trim().toLowerCase()) ? "" : housename.trim();
                            dataCell.setCellValue(housename);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(3);
                            String housedate = String.valueOf(map.get("handle_date"));
                            housedate = null == housedate || "".equals(housedate.trim()) || "null".equals(housedate.trim().toLowerCase()) ? "" : housedate.trim();
                            dataCell.setCellValue(housedate);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(4);
                            String settlementdate = String.valueOf(map.get("settlement_date"));
                            settlementdate = null == settlementdate || "".equals(settlementdate.trim()) || "null".equals(settlementdate.trim().toLowerCase()) ? "" : settlementdate.trim();
                            dataCell.setCellValue(settlementdate);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(5);
                            String handler_type = String.valueOf(map.get("handler_type"));
                            handler_type = null == handler_type || "".equals(handler_type.trim()) || "null".equals(handler_type.trim().toLowerCase()) ? "" : handler_type.trim();
                            switch (handler_type) {
                                case "1":
                                    dataCell.setCellValue("采购入库");
                                    break;
                                case "2":
                                    dataCell.setCellValue("领料出库");
                                    break;
                                case "7":
                                    dataCell.setCellValue("盘盈");
                                    break;
                                case "8":
                                    dataCell.setCellValue("盘亏");
                                    break;
                                case "9":
                                    dataCell.setCellValue("调入");
                                    break;
                                case "10":
                                    dataCell.setCellValue("调出");
                                    break;
                                case "11":
                                    dataCell.setCellValue("配方生产入库");
                                    break;
                                case "12":
                                    dataCell.setCellValue("配方生产出库");
                                    break;
                                case "13":
                                    dataCell.setCellValue("退料入库");
                                    break;
                                default:
                                    dataCell.setCellValue("");
                                    break;
                            }
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(6);
                            dataCell.setCellValue(
                                    isNull(map.get("rksl"))
                                            ? "" : map.get("rksl").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(7);
                            dataCell.setCellValue(
                                    isNull(map.get("rkdj"))
                                            ? "" : map.get("rkdj").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(8);
                            dataCell.setCellValue(
                                    isNull(map.get("rkje"))
                                            ? "" : map.get("rkje").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(9);
                            dataCell.setCellValue(
                                    isNull(map.get("cksl"))
                                            ? "" : map.get("cksl").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(10);
                            dataCell.setCellValue(
                                    isNull(map.get("ckdj"))
                                            ? "" : map.get("ckdj").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(11);
                            dataCell.setCellValue(
                                    isNull(map.get("ckje"))
                                            ? "" : map.get("ckje").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(12);
                            dataCell.setCellValue(
                                    isNull(map.get("jcsl"))
                                            ? "" : map.get("jcsl").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(13);
                            dataCell.setCellValue(
                                    isNull(map.get("jcdj"))
                                            ? "" : map.get("jcdj").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(14);
                            dataCell.setCellValue(
                                    isNull(map.get("jcje"))
                                            ? "" : map.get("jcje").toString());
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(vg);
                            String pig_barn_name = String.valueOf(map.get("pig_barn_name"));
                            pig_barn_name = null == pig_barn_name || "".equals(pig_barn_name.trim()) || "null".equals(pig_barn_name.trim().toLowerCase()) ? "" : pig_barn_name.trim();
                            dataCell.setCellValue(pig_barn_name);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(vg + 1);
                            String pig_type = String.valueOf(map.get("pig_type"));
                            pig_type = null == pig_type || "".equals(pig_type.trim()) || "null".equals(pig_type.trim().toLowerCase()) ? "" : pig_type.trim();
                            switch (pig_type) {
                                case "2":
                                    dataCell.setCellValue("保育猪");
                                    break;
                                case "3":
                                    dataCell.setCellValue("育肥猪");
                                    break;
                                case "4":
                                    dataCell.setCellValue("后备猪");
                                    break;
                                case "5":
                                    dataCell.setCellValue("配种母猪");
                                    break;
                                case "6":
                                    dataCell.setCellValue("妊娠母猪");
                                    break;
                                case "7":
                                    dataCell.setCellValue("分娩母猪");
                                    break;
                                case "9":
                                    dataCell.setCellValue("种公猪");
                                    break;
                                default:
                                    dataCell.setCellValue("");
                                    break;
                            }
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(vg + 2);
                            String pig_group_name = String.valueOf(map.get("pig_group_name"));
                            pig_group_name = null == pig_group_name || "".equals(pig_group_name.trim()) || "null".equals(pig_group_name.trim().toLowerCase()) ? "" : pig_group_name.trim();
                            dataCell.setCellValue(pig_group_name);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(vg + 3);
                            String apply_staff_name = String.valueOf(map.get("apply_staff_name"));
                            apply_staff_name = null == apply_staff_name || "".equals(apply_staff_name.trim()) || "null".equals(apply_staff_name.trim().toLowerCase()) ? "" : apply_staff_name.trim();
                            dataCell.setCellValue(apply_staff_name);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(vg + 4);
                            String farm_name = String.valueOf(map.get("farm_name"));
                            farm_name = null == farm_name || "".equals(farm_name.trim()) || "null".equals(farm_name.trim().toLowerCase()) ? "" : farm_name.trim();
                            dataCell.setCellValue(farm_name);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(vg + 5);
                            String unit = String.valueOf(map.get("unit"));
                            unit = null == unit || "".equals(unit.trim()) || "null".equals(unit.trim().toLowerCase()) ? "" : unit.trim();
                            dataCell.setCellValue(unit);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(vg + 6);
                            String provider_name = String.valueOf(map.get("provider_name"));
                            provider_name = null == provider_name || "".equals(provider_name.trim()) || "null".equals(provider_name.trim().toLowerCase()) ? "" : provider_name.trim();
                            dataCell.setCellValue(provider_name);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);

                            dataCell = dataRow.createCell(vg + 7);
                            String specification = String.valueOf(map.get("specification"));
                            specification = null == specification || "".equals(specification.trim()) || "null".equals(specification.trim().toLowerCase()) ? "" : specification.trim();
                            dataCell.setCellValue(specification);
                            dataCell.setCellStyle(normalCellStyle);
                            dataCell.setCellType(XSSFCell.CELL_TYPE_STRING);
                        }
                        startRowIndex++;
                    }
                }
                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
