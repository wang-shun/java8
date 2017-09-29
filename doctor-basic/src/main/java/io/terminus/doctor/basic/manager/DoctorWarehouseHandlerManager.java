package io.terminus.doctor.basic.manager;

import com.google.common.collect.Maps;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.DoctorWarehouseStockHandleDto;
import io.terminus.doctor.basic.model.warehouseV2.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sunbo@terminus.io on 2017/8/11.
 */
@Component
public class DoctorWarehouseHandlerManager {


    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;
    @Autowired
    private DoctorWarehouseStockHandlerDao doctorWarehouseStockHandlerDao;
    @Autowired
    private DoctorWarehouseStockHandlerDetailDao doctorWarehouseStockHandlerDetailDao;

    @Autowired
    private DoctorWarehouseMonthlyStockDao doctorWarehouseMonthlyStockDao;

    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Autowired
    private DoctorWarehouseHandleDetailDao doctorWarehouseHandleDetailDao;
    @Autowired
    private DoctorMaterialCodeDao doctorMaterialCodeDao;
    @Autowired
    private DoctorMaterialVendorDao doctorMaterialVendorDao;


    @Transactional
    public void handle(List<StockHandleContext> contexts) {


        contexts.forEach(context -> {

//            context.getStockAndPurchases().forEach((stock, purchases) -> {
//
//                if (stock.getId() == null)
//                    doctorWarehouseStockDao.create(stock);
//                else doctorWarehouseStockDao.update(stock);
//                purchases.forEach(purchase -> {
//                    if (purchase.getId() == null)
//                        doctorWarehousePurchaseDao.create(purchase);
//                    else doctorWarehousePurchaseDao.update(purchase);
//                });
//            });

            context.getPurchaseHandleContext().forEach(p -> {
                if (p.getStock().getId() == null)
                    doctorWarehouseStockDao.create(p.getStock());
                else doctorWarehouseStockDao.update(p.getStock());
                for (DoctorWarehousePurchase purchase : p.getPurchaseQuantity().keySet()) {
                    if (purchase.getId() == null)
                        doctorWarehousePurchaseDao.create(purchase);
                    else doctorWarehousePurchaseDao.update(purchase);
                }
            });

            for (DoctorWarehouseMaterialHandle handle : context.getMaterialHandle()) {

                doctorWarehouseMaterialHandleDao.create(handle);

            }
            //两笔物料处理记录，只有调拨才有。
            if (context.getMaterialHandle().size() == 2) {
                //相互保存各自编号，方便通过一条调拨记录找到另一条
                context.getMaterialHandle().get(0).setOtherTrasnferHandleId(context.getMaterialHandle().get(1).getId());
                context.getMaterialHandle().get(1).setOtherTrasnferHandleId(context.getMaterialHandle().get(0).getId());
                doctorWarehouseMaterialHandleDao.update(context.getMaterialHandle().get(0));
                doctorWarehouseMaterialHandleDao.update(context.getMaterialHandle().get(1));
            }

            if (null != context.getApply()) {
                //出库才有
                context.getApply().setMaterialHandleId(context.getMaterialHandle().get(0).getId());
                doctorWarehouseMaterialApplyDao.create(context.getApply());
            }
        });

    }

//    @Transactional
    public void inStock(DoctorWarehouseStock stock, List<DoctorWarehousePurchase> purchases, DoctorWarehouseMaterialHandle handle, DoctorMaterialCode materialCode, DoctorMaterialVendor vendor) {
        if (null == stock.getId())
            doctorWarehouseStockDao.create(stock);
        else
            doctorWarehouseStockDao.update(stock);

        if (null != handle)
            doctorWarehouseMaterialHandleDao.create(handle);

        if (null != materialCode)
            doctorMaterialCodeDao.create(materialCode);
        if (null != vendor)
            doctorMaterialVendorDao.create(vendor);


        for (DoctorWarehousePurchase purchase : purchases) {
            if (purchase.getId() != null)
                doctorWarehousePurchaseDao.update(purchase);
            else
                doctorWarehousePurchaseDao.create(purchase);

            if (null != handle) {
                DoctorWarehouseHandleDetail outDetail = new DoctorWarehouseHandleDetail();
                outDetail.setMaterialHandleId(handle.getId());
                outDetail.setMaterialPurchaseId(purchase.getId());
                outDetail.setQuantity(purchase.getQuantity());
                doctorWarehouseHandleDetailDao.create(outDetail);
            }
        }
    }

//    @Transactional
    public void outStock(DoctorWarehouseStock stock, PurchaseHandleContext purchaseHandleContext, DoctorWarehouseMaterialHandle handle) {

        doctorWarehouseStockDao.update(stock);
        if (null != handle)
            doctorWarehouseMaterialHandleDao.create(handle);
        for (DoctorWarehousePurchase purchase : purchaseHandleContext.getPurchaseQuantity().keySet()) {
            doctorWarehousePurchaseDao.update(purchase);

            if (null != handle) {
                DoctorWarehouseHandleDetail outDetail = new DoctorWarehouseHandleDetail();
                outDetail.setMaterialHandleId(handle.getId());
                outDetail.setMaterialPurchaseId(purchase.getId());
                outDetail.setQuantity(purchaseHandleContext.getPurchaseQuantity().get(purchase));
                doctorWarehouseHandleDetailDao.create(outDetail);
            }
        }

    }

    @Transactional
    public void inAndOutStock(List<DoctorWarehouseStockHandleDto> handleInDtos, List<DoctorWarehouseStockHandleDto> handleOutDtos, DoctorWarehouseStockHandler handle) {

//        List<DoctorWarehouseStockHandlerDetail> handleDetails = new ArrayList<>();
//        for (DoctorWarehouseStockHandleDto in : handleInDtos) {
//
//            if (null == in.getStock().getId()) {
//                doctorWarehouseStockDao.create(in.getStock());
//            } else {
//                in.getStock().setNumber(in.getStock().getNumber().add(in.getNumber()));
//                doctorWarehouseStockDao.update(in.getStock());
//            }
//
//            in.getHandleDetail().setStockId(in.getStock().getId());
//            handleDetails.add(in.getHandleDetail());
//
//            handleStockMonthlyReport(in.getStock(), in.getHandleDate(), in.getNumber(), in.getStock().getUnitPrice(), true);
//        }
//        boolean noHandleDetail = handleDetails.isEmpty();
//        for (DoctorWarehouseStockHandleDto out : handleOutDtos) {
//
//
//            out.getStock().setNumber(out.getStock().getNumber().subtract(out.getNumber()));
//            doctorWarehouseStockDao.update(out.getStock());
//
//            if (noHandleDetail) {
//                out.getHandleDetail().setStockId(out.getStock().getId());
//                handleDetails.add(out.getHandleDetail());
//            }
//
//            handleStockMonthlyReport(out.getStock(), out.getHandleDate(), out.getNumber(), out.getStock().getUnitPrice(), false);
//        }
//
//
//        doctorWarehouseStockHandlerDao.create(handle);
//        for (DoctorWarehouseStockHandlerDetail detail : handleDetails) {
//            detail.setHandlerId(handle.getId());
//            doctorWarehouseStockHandlerDetailDao.create(detail);
//        }

    }


    private void handleStockMonthlyReport(DoctorWarehouseStock stock, Date handleDate, BigDecimal number, Long unitPrice, boolean in) {
//        Map<String, Object> criteria = new HashedMap();
//        criteria.put("warehouseId", stock.getWarehouseId());
//        criteria.put("materialId", stock.getMaterialId());
//        if (null != stock.getVendorId())
//            criteria.put("vendorId", stock.getVendorId());
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(handleDate);
//
//        criteria.put("year", calendar.get(Calendar.YEAR));
//        criteria.put("month", calendar.get(Calendar.MONTH) + 1);
//        List<DoctorWarehouseMonthlyStock> monthlyStockResult = doctorWarehouseMonthlyStockDao.list(criteria);
//
//
//        DoctorWarehouseMonthlyStock lastMonthStock = findLastMonth(criteria);
//
//        Long money = number.multiply(new BigDecimal(stock.getUnitPrice())).longValue();
//
//        if (null == monthlyStockResult || monthlyStockResult.isEmpty()) {
//
//            DoctorWarehouseMonthlyStock monthlyStock = new DoctorWarehouseMonthlyStock();
//            monthlyStock.setWarehouseId(stock.getWarehouseId());
//            monthlyStock.setWarehouseName(stock.getWarehouseName());
//            monthlyStock.setWarehouseType(stock.getWarehouseType());
//            monthlyStock.setMaterialId(stock.getMaterialId());
//            monthlyStock.setMaterialName(stock.getMaterialName());
//            monthlyStock.setVendorId(stock.getVendorId());
//            if (null == lastMonthStock) {
//                monthlyStock.setEarlyMoney(0L);
//                monthlyStock.setEarlyNumber(new BigDecimal(0));
//            } else {
//                monthlyStock.setEarlyNumber(lastMonthStock.getBalanceNumber());
//                monthlyStock.setEarlyMoney(lastMonthStock.getBalanceMoney());
//            }
//            monthlyStock.setYear(calendar.get(Calendar.YEAR));
//            monthlyStock.setMonth(calendar.get(Calendar.MONTH) + 1);
//            if (in) {
//                monthlyStock.setInNumber(number);
//                monthlyStock.setInMoney(money);
//                monthlyStock.setOutMoney(0L);
//                monthlyStock.setOutNumber(new BigDecimal(0));
//            } else {
//                monthlyStock.setOutNumber(number);
//                monthlyStock.setOutMoney(money);
//                monthlyStock.setInNumber(new BigDecimal(0));
//                monthlyStock.setInMoney(0L);
//            }
//            monthlyStock.setBalanceMoney(money);
//            monthlyStock.setBalanceNumber(number);
//            doctorWarehouseMonthlyStockDao.create(monthlyStock);
//        } else {
//
//            DoctorWarehouseMonthlyStock monthlyStock = monthlyStockResult.get(0);
//
//            if (in) {
//                monthlyStock.setInMoney(monthlyStock.getInMoney() + money);
//                monthlyStock.setInNumber(monthlyStock.getInNumber().add(number));
//                monthlyStock.setBalanceNumber(monthlyStock.getBalanceNumber().add(number));
//                monthlyStock.setBalanceMoney(monthlyStock.getBalanceMoney() + money);
//            } else {
//                monthlyStock.setOutNumber(monthlyStock.getOutNumber().add(number));
//                monthlyStock.setOutMoney(monthlyStock.getOutMoney() + money);
//                monthlyStock.setBalanceMoney(monthlyStock.getBalanceMoney() - money);
//                monthlyStock.setBalanceNumber(monthlyStock.getBalanceNumber().subtract(number));
//            }
//            doctorWarehouseMonthlyStockDao.update(monthlyStock);
//        }
    }


    private DoctorWarehouseMonthlyStock findLastMonth(Map<String, Object> criteria) {

        Map<String, Object> lastMonthCriteria = Maps.newHashMap(criteria);
        if (criteria.get("month").equals("1")) {
            lastMonthCriteria.put("month", 12);
            lastMonthCriteria.put("year", Integer.parseInt(criteria.get("year").toString()) - 1);
        } else {
            lastMonthCriteria.put("month", Integer.parseInt(criteria.get("month").toString()) - 1);
        }

        List<DoctorWarehouseMonthlyStock> lastMonthStockResult = doctorWarehouseMonthlyStockDao.list(lastMonthCriteria);
        if (null == lastMonthStockResult || lastMonthStockResult.isEmpty())
            return null;
        else return lastMonthStockResult.get(0);
    }


    @Data
    public static class StockHandleContext {

//        private DoctorWarehouseStock stock;


        private Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockAndPurchases;

        private List<PurchaseHandleContext> purchaseHandleContext;

        private List<DoctorWarehouseMaterialHandle> materialHandle;

        private DoctorWarehouseMaterialApply apply;

//        private List<DoctorWarehousePurchase> purchases;


        public void addMaterialHandle(DoctorWarehouseMaterialHandle handle) {
            if (null == this.materialHandle)
                this.materialHandle = new ArrayList<>();

            this.materialHandle.add(handle);
        }

    }

    @Data
    public static class PurchaseHandleContext {

        //均价
        private long averagePrice;

        private DoctorWarehouseStock stock;

        private Map<DoctorWarehousePurchase, BigDecimal> purchaseQuantity = new HashMap<>();
    }


}
