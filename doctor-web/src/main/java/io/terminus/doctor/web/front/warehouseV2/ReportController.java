package io.terminus.doctor.web.front.warehouseV2;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
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

//        List<DoctorWarehouseMaterialApply> applies = RespHelper.or500(doctorWarehouseMaterialApplyReadService.month(warehouseId, date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, null));
//
//        Map<Long/*MaterialHandleId*/, DoctorWarehouseMaterialApply> handleApply = new HashMap<>();
//        for (DoctorWarehouseMaterialApply apply : applies) {
//            handleApply.put(apply.getMaterialHandleId(), apply);
//        }
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

    /**
     * 物料变动报表
     */
    @RequestMapping(method = RequestMethod.GET, value = "/wlbdReport")
    public List<Map<String,Object>> wlbdReport(
            Long farmId,
            String settlementDate,
            Integer pigBarnType,
            Long pigBarnId,
            Long pigGroupId,
            Integer handlerType,
            Integer type,
            Long warehouseId,
            String materialName
    ) {

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

        return doctorWarehouseReportReadService.wlbdReport(
                farmId,settlementDate,pigBarnType,
                pigBarnId,pigGroupId,handlerType,
                type,warehouseId,materialName
        ).getResult();
    }

    @Autowired
    private Exporter exporter;

    //导出
    @RequestMapping(method = RequestMethod.GET, value = "/wlbdReport/export")
    public void exportWlbdReport(
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

        //开始导出
        try {

            DoctorFarm farm = doctorFarmReadService.findFarmById(farmId).getResult();
            if (null == farm)
                throw new JsonResponseException("farm.not.found");

            String farmName = farm.getName();
            String fileName = "物料变动报表" + new Date().getTime();

            List<Map<String,Object>> exportVos = RespHelper.or500(
                    doctorWarehouseReportReadService.wlbdReport(
                            farmId,settlementDate,pigBarnType,
                            pigBarnId,pigGroupId,handlerType,
                            type,warehouseId,materialName
                    ));

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

                //表头样式
                XSSFCellStyle titleCellStyle = workbook.createCellStyle();
                //样式居中
                titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                titleCellStyle.setFont(titleFont);

                //数据样式
                XSSFCellStyle normalCellStyle = workbook.createCellStyle();
                //样式居中
                normalCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                normalCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                normalCellStyle.setFont(normalFont);

                XSSFCellStyle leftCellStyle = workbook.createCellStyle();
                //样式居左
                leftCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                leftCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
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
                titleCell = titleRow.createCell(15);
                titleCell.setCellValue("猪舍名称");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(16);
                titleCell.setCellValue("猪舍类型");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(17);
                titleCell.setCellValue("猪群名称");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(18);
                titleCell.setCellValue("饲养员");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(19);
                titleCell.setCellValue("猪场名称");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(20);
                titleCell.setCellValue("单位");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(21);
                titleCell.setCellValue("厂家");
                titleCell.setCellStyle(titleCellStyle);
                titleCell = titleRow.createCell(22);
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
                titleCell = titleRow.createCell(15);
                titleCell = titleRow.createCell(16);
                titleCell = titleRow.createCell(17);
                titleCell = titleRow.createCell(18);
                titleCell = titleRow.createCell(19);
                titleCell = titleRow.createCell(20);
                titleCell = titleRow.createCell(21);
                titleCell = titleRow.createCell(22);

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

                for(int i=15;i <= 22;i++){
                    cra = new CellRangeAddress(fis, sec, i, i);
                    sheet.addMergedRegion(cra);
                }

                if(!CollectionUtils.isEmpty(exportVos)) {
                    Map<String,Object> lastMonthData = exportVos.get(0);
                    String tName = String.valueOf(lastMonthData.get("material_name"));
                    Row dataRow = null;
                    Cell dataCell = null;
                    int startRowIndex = 2;
                    if("上月结存".equals(tName))
                    {
                        //只显示结存数据,从第二行开始创建起
                        dataRow = sheet.createRow(startRowIndex);
                        for(int i = 0;i <= 22; i++) {

                            if(i == 0)
                            {
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(tName);
                                dataCell.setCellStyle(leftCellStyle);
                            }

                            if((i > 0 && i < 12) || (i > 14 && i <= 22))
                            {
                                dataCell = dataRow.createCell(i);
                            }

                            if(i == 12){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(lastMonthData.get("jcsl"))
                                                ? "" : lastMonthData.get("jcsl").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 13){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(lastMonthData.get("jcdj"))
                                                ? "" : lastMonthData.get("jcdj").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 14){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(lastMonthData.get("jcje"))
                                                ? "" : lastMonthData.get("jcje").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                        }

                        //合并单元格
                        cra = new CellRangeAddress(startRowIndex, startRowIndex, 0, 11);
                        sheet.addMergedRegion(cra);
                        cra = new CellRangeAddress(startRowIndex, startRowIndex, 15, 22);
                        sheet.addMergedRegion(cra);
                        startRowIndex++;
                    }

                    //表数据
                    for (Map<String, Object> map : exportVos) {
                        String vId = String.valueOf(map.get("id"));
                        if(StringUtils.isNotBlank(vId)){ // id不为空
                            dataRow = sheet.createRow(startRowIndex);

                            dataCell = dataRow.createCell(0);
                            String mname = String.valueOf(map.get("material_name"));
                            mname = null == mname || "".equals(mname.trim()) || "null".equals(mname.trim().toLowerCase()) ? "" : mname.trim();
                            dataCell.setCellValue(mname);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(1);
                            String mtype = String.valueOf(map.get("material_type"));
                            mtype = null == mtype || "".equals(mtype.trim()) || "null".equals(mtype.trim().toLowerCase()) ? "" : mtype.trim();
                            switch (mtype)
                            {
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

                            dataCell = dataRow.createCell(2);
                            String housename = String.valueOf(map.get("ware_house_name"));
                            housename = null == housename || "".equals(housename.trim()) || "null".equals(housename.trim().toLowerCase())  ? "" : housename.trim();
                            dataCell.setCellValue(housename);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(3);
                            String housedate = String.valueOf(map.get("handle_date"));
                            housedate = null == housedate || "".equals(housedate.trim()) || "null".equals(housedate.trim().toLowerCase())  ? "" : housedate.trim();
                            dataCell.setCellValue(housedate);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(4);
                            String settlementdate = String.valueOf(map.get("settlement_date"));
                            settlementdate = null == settlementdate || "".equals(settlementdate.trim()) || "null".equals(settlementdate.trim().toLowerCase())   ? "" : settlementdate.trim();
                            dataCell.setCellValue(settlementdate);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(5);
                            String handler_type = String.valueOf(map.get("handler_type"));
                            handler_type = null == handler_type || "".equals(handler_type.trim()) || "null".equals(handler_type.trim().toLowerCase())  ? "" : handler_type.trim();
                            switch (handler_type)
                            {
                                case "1":
                                    dataCell.setCellValue("采购入库");
                                    break;
                                case "2":
                                    dataCell.setCellValue("退料入库");
                                    break;
                                case "3":
                                    dataCell.setCellValue("配方入库");
                                    break;
                                case "4":
                                    dataCell.setCellValue("盘盈入库");
                                    break;
                                case "5":
                                    dataCell.setCellValue("调拨入库");
                                    break;
                                case "6":
                                    dataCell.setCellValue("领料出库");
                                    break;
                                case "7":
                                    dataCell.setCellValue("盘亏出库");
                                    break;
                                case "8":
                                    dataCell.setCellValue("配方出库");
                                    break;
                                case "9":
                                    dataCell.setCellValue("调拨出库");
                                    break;
                                default:
                                    dataCell.setCellValue("");
                                    break;
                            }
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(6);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("rksl"))
                                            ? "" : map.get("rksl").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(7);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("rkdj"))
                                            ? "" : map.get("rkdj").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(8);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("rkje"))
                                            ? "" : map.get("rkje").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(9);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("cksl"))
                                            ? "" : map.get("cksl").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(10);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("ckdj"))
                                            ? "" : map.get("ckdj").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(11);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("ckje"))
                                            ? "" : map.get("ckje").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(12);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("jcsl"))
                                            ? "" : map.get("jcsl").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(13);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("jcdj"))
                                            ? "" : map.get("jcdj").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(14);
                            dataCell.setCellValue(
                                    ObjectUtils.isEmpty(map.get("jcje"))
                                            ? "" : map.get("jcje").toString());
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(15);
                            String pig_barn_name = String.valueOf(map.get("pig_barn_name"));
                            pig_barn_name = null == pig_barn_name || "".equals(pig_barn_name.trim()) || "null".equals(pig_barn_name.trim().toLowerCase())  ? "" : pig_barn_name.trim();
                            dataCell.setCellValue(pig_barn_name);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(16);
                            String pig_type = String.valueOf(map.get("pig_type"));
                            pig_type = null == pig_type || "".equals(pig_type.trim()) || "null".equals(pig_type.trim().toLowerCase())  ? "" : pig_type.trim();
                            switch (pig_type)
                            {
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

                            dataCell = dataRow.createCell(17);
                            String pig_group_name = String.valueOf(map.get("pig_group_name"));
                            pig_group_name = null == pig_group_name || "".equals(pig_group_name.trim())  || "null".equals(pig_group_name.trim().toLowerCase()) ? "" : pig_group_name.trim();
                            dataCell.setCellValue(pig_group_name);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(18);
                            String apply_staff_name = String.valueOf(map.get("apply_staff_name"));
                            apply_staff_name = null == apply_staff_name || "".equals(apply_staff_name.trim()) || "null".equals(apply_staff_name.trim().toLowerCase())  ? "" : apply_staff_name.trim();
                            dataCell.setCellValue(apply_staff_name);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(19);
                            String farm_name = String.valueOf(map.get("farm_name"));
                            farm_name = null == farm_name || "".equals(farm_name.trim()) || "null".equals(farm_name.trim().toLowerCase()) ? "" : farm_name.trim();
                            dataCell.setCellValue(farm_name);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(20);
                            String unit = String.valueOf(map.get("unit"));
                            unit = null == unit || "".equals(unit.trim()) || "null".equals(unit.trim().toLowerCase()) ? "" : unit.trim();
                            dataCell.setCellValue(unit);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(21);
                            String provider_name = String.valueOf(map.get("provider_name"));
                            provider_name = null == provider_name || "".equals(provider_name.trim()) || "null".equals(provider_name.trim().toLowerCase())  ? "" : provider_name.trim();
                            dataCell.setCellValue(provider_name);
                            dataCell.setCellStyle(normalCellStyle);

                            dataCell = dataRow.createCell(22);
                            String specification = String.valueOf(map.get("specification"));
                            specification = null == specification || "".equals(specification.trim()) || "null".equals(specification.trim().toLowerCase())  ? "" : specification.trim();
                            dataCell.setCellValue(specification);
                            dataCell.setCellStyle(normalCellStyle);

                            startRowIndex++;
                        }

                    }

                    //最后一行数据
                    Map<String,Object> thisMonthHZData = exportVos.get(exportVos.size() - 1);
                    tName = String.valueOf(thisMonthHZData.get("material_name"));
                    if("本月结存".equals(tName)) {

                        dataRow = sheet.createRow(startRowIndex);
                        for(int i = 0;i <= 22; i++) {

                            if(i == 0)
                            {
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(tName);
                                dataCell.setCellStyle(leftCellStyle);
                            }

                            if((i > 0 && i < 6) || (i > 14 && i <= 22))
                            {
                                dataCell = dataRow.createCell(i);
                            }

                            if(i == 6){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("rksl"))
                                                ? "" : thisMonthHZData.get("rksl").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 7){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("rkdj"))
                                                ? "" : thisMonthHZData.get("rkdj").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 8){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("rkje"))
                                                ? "" : thisMonthHZData.get("rkje").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 9){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("cksl"))
                                                ? "" : thisMonthHZData.get("cksl").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 10){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("ckdj"))
                                                ? "" : thisMonthHZData.get("ckdj").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 11){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("ckje"))
                                                ? "" : thisMonthHZData.get("ckje").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 12){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("jcsl"))
                                                ? "" : thisMonthHZData.get("jcsl").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 13){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("jcdj"))
                                                ? "" : thisMonthHZData.get("jcdj").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                            if(i == 14){
                                dataCell = dataRow.createCell(i);
                                dataCell.setCellValue(
                                        ObjectUtils.isEmpty(thisMonthHZData.get("jcje"))
                                                ? "" : thisMonthHZData.get("jcje").toString());
                                dataCell.setCellStyle(normalCellStyle);
                            }

                        }

                        //合并单元格
                        cra = new CellRangeAddress(startRowIndex, startRowIndex, 0, 5);
                        sheet.addMergedRegion(cra);
                        cra = new CellRangeAddress(startRowIndex, startRowIndex, 15, 22);
                        sheet.addMergedRegion(cra);

                    }

                }

                workbook.write(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
