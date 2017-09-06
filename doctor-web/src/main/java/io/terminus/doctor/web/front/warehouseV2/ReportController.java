package io.terminus.doctor.web.front.warehouseV2;

import com.fasterxml.jackson.annotation.JsonView;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockStatisticsDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.basic.service.warehouseV2.*;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseMaterialHandleVo;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseMonthlyReportVo;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehousePigGroupApplyVo;
import io.terminus.doctor.web.front.warehouseV2.vo.WarehouseReportVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sunbo@terminus.io on 2017/8/9.
 */
@Slf4j
@RestController
@RequestMapping("api/doctor/warehouse/report")
public class ReportController {


    @RpcConsumer
    private DoctorWarehouseMonthlyStockReadService doctorWarehouseMonthlyStockReadService;

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
                            .build(), WarehouseMaterialHandleType.OUT, WarehouseMaterialHandleType.IN);

            if (!inAndOutAmountsResponse.isSuccess())
                throw new JsonResponseException(inAndOutAmountsResponse.getError());

            //统计猪厂下每个仓库的余额
            Response<Map<Long, Long>> balanceAmountsResponse = doctorWarehousePurchaseReadService.countWarehouseBalanceAmount(farmId);
            if (!balanceAmountsResponse.isSuccess())
                throw new JsonResponseException(balanceAmountsResponse.getError());

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
                balanceDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                        .name(wareHouse.getWareHouseName())
                        .amount(balanceAmountsResponse.getResult().containsKey(wareHouse.getId()) ? balanceAmountsResponse.getResult().get(wareHouse.getId()) : 0)
                        .build());

                long inAmount;
                if (!inAndOutAmountsResponse.getResult().containsKey(WarehouseMaterialHandleType.IN))
                    inAmount = 0;
                else if (!inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.IN).containsKey(wareHouse.getId()))
                    inAmount = 0;
                else
                    inAmount = inAndOutAmountsResponse.getResult().get(WarehouseMaterialHandleType.IN).get(wareHouse.getId());
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
                outDetails.add(WarehouseReportVo.WarehouseReportMonthDetail.builder()
                        .name(wareHouse.getWareHouseName())
                        .amount(outAmount)
                        .build());

                totalBalance += balanceAmountsResponse.getResult().containsKey(wareHouse.getId()) ? balanceAmountsResponse.getResult().get(wareHouse.getId()) : 0;
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

        DoctorWarehouseStock stockCriteria = new DoctorWarehouseStock();
        stockCriteria.setWarehouseId(warehouseId);
        Response<List<DoctorWarehouseStock>> stocksResponse = doctorWarehouseStockReadService.list(stockCriteria);
        if (!stocksResponse.isSuccess())
            throw new JsonResponseException(stocksResponse.getError());

        List<WarehouseMonthlyReportVo> report = new ArrayList<>();
        for (DoctorWarehouseStock stock : stocksResponse.getResult()) {

            Response<AmountAndQuantityDto> balanceResponse = doctorWarehouseReportReadService.countMaterialBalance(warehouseId, stock.getMaterialId(), stock.getVendorName());
            if (!balanceResponse.isSuccess())
                throw new JsonResponseException(balanceResponse.getError());
            Response<WarehouseStockStatisticsDto> statisticsResponse = doctorWarehouseReportReadService.countMaterialHandleByMaterialVendor(warehouseId, stock.getMaterialId(), stock.getVendorName(), date,
                    WarehouseMaterialHandleType.IN,
                    WarehouseMaterialHandleType.OUT
            );
            if (!statisticsResponse.isSuccess())
                throw new JsonResponseException(statisticsResponse.getError());

            WarehouseMonthlyReportVo vo = new WarehouseMonthlyReportVo();
            vo.setMaterialName(stock.getMaterialName());
            if (DoctorWarehouseStockWriteService.DEFAULT_VENDOR_NAME.equals(stock.getVendorName()))
                vo.setVendorName("");
            else
                vo.setVendorName(stock.getVendorName());
            vo.setUnit(stock.getUnit());

            vo.setBalanceAmount(balanceResponse.getResult().getAmount());
            vo.setBalanceQuantity(balanceResponse.getResult().getQuantity());

            vo.setInAmount(statisticsResponse.getResult().getIn().getAmount());
            vo.setInQuantity(statisticsResponse.getResult().getIn().getQuantity());
            vo.setOutAmount(statisticsResponse.getResult().getOut().getAmount());
            vo.setOutQuantity(statisticsResponse.getResult().getOut().getQuantity());

            //TODO 这种计算方式还是会出现负数
            vo.setInitialAmount(vo.getBalanceAmount() + vo.getOutAmount() - vo.getInAmount());
            vo.setInitialQuantity(vo.getBalanceQuantity().add(vo.getOutQuantity()).subtract(vo.getInQuantity()));

            vo.setInQuantity(vo.getBalanceQuantity().add(vo.getOutQuantity()).multiply(vo.getInQuantity()));

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
                                                                @RequestParam(required = false) String materialName,
                                                                @RequestParam(required = false) Integer type,
                                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {

        DoctorWarehouseMaterialHandle criteria = new DoctorWarehouseMaterialHandle();
        criteria.setWarehouseId(warehouseId);
        criteria.setHandleYear(date.get(Calendar.YEAR));
        criteria.setHandleMonth(date.get(Calendar.MONTH) + 1);
        if (null != type)
            criteria.setType(type);
        if (StringUtils.isNotBlank(materialName))
            criteria.setMaterialName(materialName);

        Response<List<DoctorWarehouseMaterialHandle>> materialHandleResponse = doctorWarehouseMaterialHandleReadService.list(criteria);
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

        List<WarehouseMaterialHandleVo> vos = new ArrayList<>(materialHandleResponse.getResult().size());
        for (DoctorWarehouseMaterialHandle handle : materialHandleResponse.getResult()) {

            String pigBarnName, pigGroupName;
            if (!handleApply.containsKey(handle.getId()))
                pigBarnName = pigGroupName = null;
            else {
                pigBarnName = handleApply.get(handle.getId()).getPigBarnName();
                pigGroupName = handleApply.get(handle.getId()).getPigGroupName();
            }

            vos.add(WarehouseMaterialHandleVo.builder()
                    .materialName(handle.getMaterialName())
                    .type(handle.getType())
                    .handleDate(handle.getHandleDate())
                    .quantity(handle.getQuantity())
                    .unitPrice(handle.getUnitPrice())
                    .pigBarnName(pigBarnName)
                    .pigGroupName(pigGroupName)
                    .warehouseName(handle.getWarehouseName())
                    .build());
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
    public List<DoctorWarehouseMaterialApply> apply(@RequestParam(required = false) Long pigBarnId,
                                                    @RequestParam Long warehouseId,
                                                    @RequestParam(required = false) Integer type,
                                                    @RequestParam(required = false) String materialName,
                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {
        DoctorWarehouseMaterialApply criteria = new DoctorWarehouseMaterialApply();
        criteria.setApplyYear(date.get(Calendar.YEAR));
        criteria.setApplyMonth(date.get(Calendar.MONTH) + 1);
        criteria.setWarehouseId(warehouseId);

        if (StringUtils.isNotBlank(materialName))
            criteria.setMaterialName(materialName);
        if (null != pigBarnId)
            criteria.setPigBarnId(pigBarnId);
        if (null != type)
            criteria.setType(type);

        Response<List<DoctorWarehouseMaterialApply>> applyResponse = doctorWarehouseMaterialApplyReadService.list(criteria);
        if (!applyResponse.isSuccess())
            throw new JsonResponseException(applyResponse.getError());
        return applyResponse.getResult();
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
                                                        @RequestParam Long pigGroupId,
                                                        @RequestParam(required = false) Integer materialType,
                                                        @RequestParam(required = false) String materialName) {
        Response<List<DoctorWarehouseMaterialApply>> applyResponse = doctorWarehouseMaterialApplyReadService.list(DoctorWarehouseMaterialApply.builder()
                .warehouseId(warehouseId)
                .type(materialType)
                .materialName(materialName)
                .pigGroupId(pigGroupId)
                .build());

        if (!applyResponse.isSuccess())
            throw new JsonResponseException(applyResponse.getError());
        if (null == applyResponse.getResult() || applyResponse.getResult().isEmpty()) {
            log.info("未找到物料领用记录,warehouseId[{}],pigGroupId[{}],materialType[{}],materialName[{}]", warehouseId, pigGroupId, materialType, materialName);
            return Collections.emptyList();
        }

        List<WarehousePigGroupApplyVo> vos = new ArrayList<>();
        for (DoctorWarehouseMaterialApply apply : applyResponse.getResult()) {
            if (null == apply.getPigGroupId())
                continue;

            Response<DoctorGroup> groupResponse = doctorGroupReadService.findGroupById(apply.getPigGroupId());
            if (!groupResponse.isSuccess())
                throw new JsonResponseException(groupResponse.getError());
            if (null == groupResponse.getResult())
                throw new JsonResponseException("pig.group.not.found");

            vos.add(WarehousePigGroupApplyVo.builder()
                    .pigGroupId(apply.getPigGroupId())
                    .openDate(groupResponse.getResult().getOpenAt())
                    .closeDate(groupResponse.getResult().getCloseAt())
                    .pigBarnName(apply.getPigBarnName())
                    .applyStaffName(apply.getApplyStaffName())
                    .materialType(apply.getType())
                    .materialName(apply.getMaterialName())
                    .unit(apply.getUnit())
                    .quantity(apply.getQuantity())
                    .unitPrice(apply.getUnitPrice())
                    .amount(apply.getQuantity().multiply(new BigDecimal(apply.getUnitPrice())).longValue())
                    .build());
        }

        return vos;
    }

}
