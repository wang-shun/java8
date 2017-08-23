package io.terminus.doctor.web.front.new_warehouse;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandlerType;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseMonthlyReportVo;
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

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Map<Integer, Long[]>> report(@RequestParam Long farmId,
                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {

        //TODO 待修改
        DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
        purchaseCriteria.setFarmId(farmId);
        purchaseCriteria.setHandleYear(date.get(Calendar.YEAR));
        Response<List<DoctorWarehousePurchase>> purchaseResponse = doctorWarehousePurchaseReadService.list(purchaseCriteria);
        if (!purchaseResponse.isSuccess())
            throw new JsonResponseException(purchaseResponse.getError());


        Map<String, Map<Integer, Long[]>> warehouseReport = new HashMap<>();
        for (DoctorWarehousePurchase purchase : purchaseResponse.getResult()) {
            if (!warehouseReport.containsKey(purchase.getWarehouseName())) {
                Map<Integer, Long[]> month = new HashMap<>();
                long inMoney = purchase.getQuantity().multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
                long outMoney = purchase.getHandleQuantity().multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
                month.put(purchase.getHandleMonth(), new Long[]{inMoney, outMoney, inMoney - outMoney});
                warehouseReport.put(purchase.getWarehouseName(), month);
            } else {
                Map<Integer, Long[]> month = warehouseReport.get(purchase.getWarehouseName());
                if (!month.containsKey(purchase.getHandleMonth())) {

                    long inMoney = purchase.getQuantity().multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
                    long outMoney = purchase.getHandleQuantity().multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
                    month.put(purchase.getHandleMonth(), new Long[]{inMoney, outMoney, inMoney - outMoney});
                } else {
                    Long[] moneys = month.get(purchase.getHandleMonth());
                    long inMoney = purchase.getQuantity().multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
                    long outMoney = purchase.getHandleQuantity().multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
                    moneys[0] = moneys[0] + inMoney;
                    moneys[1] = moneys[1] + outMoney;
                    moneys[2] = moneys[2] + inMoney - outMoney;
                }
            }
        }
        return warehouseReport;

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

        //上月仓库出库记录
        DoctorWarehouseMaterialHandle lastMonthOutCriteria = new DoctorWarehouseMaterialHandle();
        lastMonthOutCriteria.setWarehouseId(warehouseId);
        lastMonthOutCriteria.setHandleMonth(date.get(Calendar.MONTH));
        lastMonthOutCriteria.setHandleYear(date.get(Calendar.YEAR));
        lastMonthOutCriteria.setType(WarehouseMaterialHandlerType.OUT.getValue());
        Response<List<DoctorWarehouseMaterialHandle>> lastMonthOutResponse = doctorWarehouseMaterialHandleReadService.list(lastMonthOutCriteria);
        if (!lastMonthOutResponse.isSuccess())
            throw new JsonResponseException(lastMonthOutResponse.getError());

        //统计月初余额和余量
        Map<String, BigDecimal> stockEaryOutQuantity = new HashMap<>();
        Map<String, Long> stockEaryOutMoney = new HashMap<>();
        for (DoctorWarehouseMaterialHandle outHandle : lastMonthOutResponse.getResult()) {
            String key = outHandle.getMaterialId() + "|" + outHandle.getVendorName();
            if (!stockEaryOutQuantity.containsKey(key)) {
                stockEaryOutQuantity.put(key, outHandle.getQuantity());
            } else {
                BigDecimal quantity = stockEaryOutQuantity.get(key);
                stockEaryOutQuantity.put(key, quantity.add(outHandle.getQuantity()));
            }
            BigDecimal money = new BigDecimal(outHandle.getUnitPrice()).multiply(outHandle.getQuantity());
            if (!stockEaryOutMoney.containsKey(key)) {
                stockEaryOutMoney.put(key, money.longValue());
            } else {
                long m = stockEaryOutMoney.get(key);
                stockEaryOutMoney.put(key, money.longValue() + m);
            }
        }

        DoctorWarehouseMaterialHandle lastMonthInCriteria = new DoctorWarehouseMaterialHandle();
        lastMonthInCriteria.setWarehouseId(warehouseId);
        lastMonthInCriteria.setHandleMonth(date.get(Calendar.MONTH));
        lastMonthInCriteria.setHandleYear(date.get(Calendar.YEAR));
        lastMonthInCriteria.setType(WarehouseMaterialHandlerType.IN.getValue());
        Response<List<DoctorWarehouseMaterialHandle>> lastMonthInResponse = doctorWarehouseMaterialHandleReadService.list(lastMonthInCriteria);
        if (!lastMonthInResponse.isSuccess())
            throw new JsonResponseException(lastMonthInResponse.getError());

        Map<String, BigDecimal> stockEaryInQuantity = new HashMap<>();
        Map<String, Long> stockEaryInMoney = new HashMap<>();
        for (DoctorWarehouseMaterialHandle inHandle : lastMonthInResponse.getResult()) {
            String key = inHandle.getMaterialId() + "|" + inHandle.getVendorName();
            if (!stockEaryInQuantity.containsKey(key)) {
                stockEaryInQuantity.put(key, inHandle.getQuantity());
            } else {
                BigDecimal quantity = stockEaryInQuantity.get(key);
                stockEaryInQuantity.put(key, quantity.add(inHandle.getQuantity()));
            }
            BigDecimal money = new BigDecimal(inHandle.getUnitPrice()).multiply(inHandle.getQuantity());
            if (!stockEaryInMoney.containsKey(key)) {
                stockEaryInMoney.put(key, money.longValue());
            } else {
                long m = stockEaryInMoney.get(key);
                stockEaryInMoney.put(key, money.longValue() + m);
            }
        }


        DoctorWarehouseMaterialHandle thisMonthInHandleCriteria = new DoctorWarehouseMaterialHandle();
        thisMonthInHandleCriteria.setWarehouseId(warehouseId);
        thisMonthInHandleCriteria.setHandleMonth(date.get(Calendar.MONTH) + 1);
        thisMonthInHandleCriteria.setHandleYear(date.get(Calendar.YEAR));
        thisMonthInHandleCriteria.setType(WarehouseMaterialHandlerType.IN.getValue());
        Response<List<DoctorWarehouseMaterialHandle>> thisMonthInHandleResponse = doctorWarehouseMaterialHandleReadService.list(thisMonthInHandleCriteria);
        if (!thisMonthInHandleResponse.isSuccess())
            throw new JsonResponseException(thisMonthInHandleResponse.getError());

        Map<String, BigDecimal> outQuantity = new HashMap<>();
        Map<String, Long> outMoney = new HashMap<>();
        Map<String, BigDecimal> inQuantity = new HashMap<>();
        Map<String, Long> inMoney = new HashMap<>();
        for (DoctorWarehouseMaterialHandle inHandle : thisMonthInHandleResponse.getResult()) {
            String key = inHandle.getMaterialId() + "|" + inHandle.getVendorName();

            if (!inQuantity.containsKey(key)) {
                inQuantity.put(key, inHandle.getQuantity());
            } else {
                BigDecimal quantity = inQuantity.get(key);
                inQuantity.put(key, quantity.add(inHandle.getQuantity()));
            }
            if (!inMoney.containsKey(key)) {
                inMoney.put(key, inHandle.getQuantity().multiply(new BigDecimal(inHandle.getUnitPrice())).longValue());
            } else {
                long money = inHandle.getQuantity().multiply(new BigDecimal(inHandle.getUnitPrice())).longValue();
                long m = inMoney.get(key);
                inMoney.put(key, money + m);
            }
        }

        DoctorWarehouseMaterialHandle thisMonthOutHandleCriteria = new DoctorWarehouseMaterialHandle();
        thisMonthOutHandleCriteria.setWarehouseId(warehouseId);
        thisMonthOutHandleCriteria.setHandleMonth(date.get(Calendar.MONTH) + 1);
        thisMonthOutHandleCriteria.setHandleYear(date.get(Calendar.YEAR));
        thisMonthOutHandleCriteria.setType(WarehouseMaterialHandlerType.OUT.getValue());
        Response<List<DoctorWarehouseMaterialHandle>> thisMonthOutHandleResponse = doctorWarehouseMaterialHandleReadService.list(thisMonthOutHandleCriteria);
        if (!thisMonthOutHandleResponse.isSuccess())
            throw new JsonResponseException(thisMonthOutHandleResponse.getError());
        for (DoctorWarehouseMaterialHandle outHandle : thisMonthOutHandleResponse.getResult()) {
            String key = outHandle.getMaterialId() + "|" + outHandle.getVendorName();
            if (!outQuantity.containsKey(key)) {
                outQuantity.put(key, outHandle.getQuantity());
            } else {
                BigDecimal q = outQuantity.get(key);
                outQuantity.put(key, q.add(outHandle.getQuantity()));
            }
            if (!outMoney.containsKey(key)) {
                outMoney.put(key, outHandle.getQuantity().multiply(new BigDecimal(outHandle.getUnitPrice())).longValue());
            } else {
                long money = outHandle.getQuantity().multiply(new BigDecimal(outHandle.getUnitPrice())).longValue();
                long m = outMoney.get(key);
                outMoney.put(key, money + m);
            }
        }

        DoctorWarehouseStock stockCriteria = new DoctorWarehouseStock();
        stockCriteria.setWarehouseId(warehouseId);
        Response<List<DoctorWarehouseStock>> stocksResponse = doctorWarehouseStockReadService.list(stockCriteria);
        if (!stocksResponse.isSuccess())
            throw new JsonResponseException(stocksResponse.getError());

        List<WarehouseMonthlyReportVo> report = new ArrayList<>();
        for (DoctorWarehouseStock stock : stocksResponse.getResult()) {
            String key = stock.getMaterialId() + "|" + stock.getVendorName();
            WarehouseMonthlyReportVo vo = new WarehouseMonthlyReportVo();
            vo.setMaterialName(stock.getMaterialName());
            if (DoctorWarehouseStockWriteService.DEFAULT_VENDOR_NAME.equals(stock.getVendorName()))
                vo.setVendorName("");
            else
                vo.setVendorName(stock.getVendorName());
            vo.setUnit(stock.getUnit());
            if (!stockEaryOutQuantity.containsKey(key))
                if (!stockEaryInQuantity.containsKey(key))
                    vo.setInitialQuantity(new BigDecimal(0));
                else
                    vo.setInitialQuantity(stockEaryInQuantity.get(key));
            else
                vo.setInitialQuantity(stockEaryInQuantity.get(key).subtract(stockEaryOutQuantity.get(key)));
            if (!stockEaryOutMoney.containsKey(key))
                if (!stockEaryInMoney.containsKey(key))
                    vo.setInitialAmount(0L);
                else
                    vo.setInitialAmount(stockEaryInMoney.get(key));
            else
                vo.setInitialAmount(stockEaryInMoney.get(key) - stockEaryOutMoney.get(key));
            if (!outMoney.containsKey(key))
                vo.setOutAmount(0L);
            else
                vo.setOutAmount(outMoney.get(key));
            if (!outQuantity.containsKey(key))
                vo.setOutQuantity(new BigDecimal(0));
            else
                vo.setOutQuantity(outQuantity.get(key));
            if (!inMoney.containsKey(key))
                vo.setInAmount(0L);
            else
                vo.setInAmount(inMoney.get(key));
            if (!inQuantity.containsKey(key))
                vo.setInQuantity(new BigDecimal(0));
            else
                vo.setInQuantity(inQuantity.get(key));
            vo.setBalanceAmount(vo.getInAmount() - vo.getOutAmount());
            vo.setBalanceQuantity(vo.getInQuantity().subtract(vo.getOutQuantity()));
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
    public List<DoctorWarehouseMaterialHandle> materialHandleReport(@RequestParam Long warehouseId,
                                                                    @RequestParam(required = false) String materialName,
                                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM") Calendar date) {

        DoctorWarehouseMaterialHandle criteria = new DoctorWarehouseMaterialHandle();
        criteria.setWarehouseId(warehouseId);
        criteria.setHandleYear(date.get(Calendar.YEAR));
        criteria.setHandleMonth(date.get(Calendar.MONTH) + 1);
        if (StringUtils.isNotBlank(materialName))
            criteria.setMaterialName(materialName);

        Response<List<DoctorWarehouseMaterialHandle>> materialHandleResponse = doctorWarehouseMaterialHandleReadService.list(criteria);
        if (!materialHandleResponse.isSuccess())
            throw new JsonResponseException(materialHandleResponse.getError());

        return materialHandleResponse.getResult();
    }

    /**
     * 领用
     *
     * @param pigBarnId
     * @param warehouseId
     * @param date
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "apply")
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

}
