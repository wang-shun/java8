package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.dto.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandlerType;
import io.terminus.doctor.basic.manager.DoctorWarehouseHandlerManager;
import io.terminus.doctor.basic.manager.DoctorWarehouseMaterialApplyManager;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouse.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-18 09:41:24
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseStockWriteServiceImpl implements DoctorWarehouseStockWriteService {


    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;

    @Autowired
    private DoctorWarehouseHandlerManager doctorWarehouseHandlerManager;

    @Autowired
    private DoctorFarmBasicReadService doctorFarmBasicReadService;

    @Autowired
    private DoctorWareHouseReadService doctorWareHouseReadService;

    @Autowired
    private DoctorBasicMaterialDao doctorBasicMaterialDao;

    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehouseMaterialApplyManager doctorWarehouseMaterialApplyManager;


    @Override
    public Response<Long> create(DoctorWarehouseStock doctorWarehouseStock) {
        try {
            doctorWarehouseStockDao.create(doctorWarehouseStock);
            return Response.ok(doctorWarehouseStock.getId());
        } catch (Exception e) {
            log.error("failed to create doctor warehouse stock, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseStock doctorWarehouseStock) {
        try {
            return Response.ok(doctorWarehouseStockDao.update(doctorWarehouseStock));
        } catch (Exception e) {
            log.error("failed to update doctor warehouse stock, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.update.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long id) {
        try {

            DoctorWarehouseStock stock = doctorWarehouseStockDao.findById(id);

            DoctorWarehouseStock criteria = new DoctorWarehouseStock();
            criteria.setWarehouseId(stock.getWarehouseId());
            criteria.setMaterialId(stock.getMaterialId());
            List<DoctorWarehouseStock> allVendorStocks = doctorWarehouseStockDao.list(criteria);

            for (DoctorWarehouseStock s : allVendorStocks) {
                if (s.getQuantity().compareTo(new BigDecimal(0)) > 0)
                    return Response.fail("stock.not.empty");
            }

            for (DoctorWarehouseStock s : allVendorStocks) {
                Boolean result = doctorWarehouseStockDao.delete(s.getId());
                if (!result)
                    return Response.fail("doctor.warehouse.stock.delete.fail");
            }

            return Response.ok(true);
        } catch (Exception e) {
            log.error("failed to delete doctor warehouse stock by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.delete.fail");
        }
    }

    @Override
    public Response<Boolean> in(WarehouseStockInDto stockIn) {
        StockContext context = validAndGetContext(stockIn.getFarmId(), stockIn.getWarehouseId(), stockIn.getDetails());
        List<DoctorWarehouseHandlerManager.StockHandleContext> handleContexts = new ArrayList<>(stockIn.getDetails().size());
        stockIn.getDetails().forEach(detail -> {
            DoctorWarehouseStock stock = getAvailableStock(context.getWareHouse(), detail, context.getSupportedMaterials().get(detail.getMaterialId()));

            //如果已有库存，累加数量
            if (stock.getId() != null) {
                stock.setQuantity(stock.getQuantity().add(detail.getQuantity()));
            }

            Calendar c = Calendar.getInstance();
            c.setTime(stockIn.getHandleDate());

            //记录物料采购入库的单价
            DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
            purchase.setFarmId(stockIn.getFarmId());
            purchase.setWarehouseId(stockIn.getWarehouseId());
            purchase.setWarehouseName(context.getWareHouse().getWareHouseName());
            purchase.setMaterialId(detail.getMaterialId());
            purchase.setVendorName(detail.getVendorName());
            purchase.setQuantity(detail.getQuantity());
            purchase.setHandleQuantity(new BigDecimal(0));
            purchase.setUnitPrice(detail.getUnitPrice());
            purchase.setHandleDate(stockIn.getHandleDate());
            purchase.setHandleYear(c.get(Calendar.YEAR));
            purchase.setHandleMonth(c.get(Calendar.MONTH) + 1);
            purchase.setHandleFinishFlag(1);

            DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
            materialHandle.setFarmId(stockIn.getFarmId());
            materialHandle.setWarehouseId(stockIn.getWarehouseId());
            materialHandle.setMaterialId(detail.getMaterialId());
            materialHandle.setMaterialName(context.getSupportedMaterials().get(detail.getMaterialId()));
            materialHandle.setWarehouseType(context.getWareHouse().getType());
            materialHandle.setUnitPrice(detail.getUnitPrice());
            materialHandle.setType(WarehouseMaterialHandlerType.IN.getValue());
            materialHandle.setQuantity(detail.getQuantity());
            materialHandle.setHandleDate(stockIn.getHandleDate());
            materialHandle.setHandleYear(c.get(Calendar.YEAR));
            materialHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);

            DoctorWarehouseHandlerManager.StockHandleContext handleContext = new DoctorWarehouseHandlerManager.StockHandleContext();
            handleContext.setStockAndPurchases(Collections.singletonMap(stock, Collections.singletonList(purchase)));
            handleContext.setMaterialHandle(materialHandle);
            handleContexts.add(handleContext);
        });

        doctorWarehouseHandlerManager.handle(handleContexts);
        return Response.ok(true);
    }

    @Override
    public Response<Boolean> inventory(WarehouseStockInventoryDto stockInventory) {

        StockContext context = validAndGetContext(stockInventory.getFarmId(), stockInventory.getWarehouseId(), stockInventory.getDetails());

//        Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> handleContext = new HashMap<>(stockInventory.getDetails().size());
        List<DoctorWarehouseHandlerManager.StockHandleContext> handleContexts = new ArrayList<>(stockInventory.getDetails().size());
        for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail : stockInventory.getDetails()) {

            //找到对应库存
            List<DoctorWarehouseStock> stocks = getStock(stockInventory.getWarehouseId(), detail.getMaterialId());
            if (null == stocks || stocks.isEmpty())
                return Response.fail("stock.not.found");

            BigDecimal totalStockQuantity = new BigDecimal(0);
            for (DoctorWarehouseStock stock : stocks) {
                totalStockQuantity = totalStockQuantity.add(stock.getQuantity());
            }
            int compareResult = totalStockQuantity.compareTo(detail.getQuantity());
            if (compareResult == 0) {
                log.info("盘点库存量与原库存量一致，warehouse[{}],material[{}]", stockInventory.getWarehouseId(), detail.getMaterialId());
                continue;
            }
            Calendar c = Calendar.getInstance();
            c.setTime(stockInventory.getHandleDate());

            DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
            purchaseCriteria.setWarehouseId(stockInventory.getWarehouseId());
            purchaseCriteria.setMaterialId(detail.getMaterialId());
            purchaseCriteria.setHandleFinishFlag(1);
            List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
            if (null == purchases || purchases.isEmpty())
                throw new ServiceException("purchase.not.found");
            //根据处理日期倒序，找出最近一次的入库记录
            purchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));
            DoctorWarehousePurchase lastPurchase = purchases.get(purchases.size() - 1);

            DoctorWarehouseHandlerManager.StockHandleContext handleContext = new DoctorWarehouseHandlerManager.StockHandleContext();
            Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockAndPurchases = new HashMap<>();

            DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();

            BigDecimal changedQuantity;
            if (compareResult > 0) {
                //盘亏
                changedQuantity = totalStockQuantity.subtract(detail.getQuantity());
                long averagePrice = handleOutAndCalcAveragePrice(changedQuantity, purchases, stockAndPurchases, stocks, false, null, null);
                materialHandle.setUnitPrice(averagePrice);
            } else {
                //盘盈
                changedQuantity = detail.getQuantity().subtract(totalStockQuantity);
                DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
                purchase.setFarmId(stockInventory.getFarmId());
                purchase.setHandleDate(stockInventory.getHandleDate());
                purchase.setWarehouseId(stockInventory.getWarehouseId());
                purchase.setWarehouseName(context.getWareHouse().getWareHouseName());
                purchase.setMaterialId(detail.getMaterialId());
                purchase.setVendorName(lastPurchase.getVendorName());
                purchase.setQuantity(changedQuantity);
                purchase.setHandleFinishFlag(1);
                purchase.setHandleQuantity(new BigDecimal(0));
                purchase.setUnitPrice(lastPurchase.getUnitPrice());
                purchase.setHandleMonth(c.get(Calendar.MONTH) + 1);
                purchase.setHandleYear(c.get(Calendar.YEAR));

                materialHandle.setUnitPrice(purchase.getUnitPrice());
                DoctorWarehouseStock stock = null;
                for (DoctorWarehouseStock s : stocks) {
                    if (s.getMaterialId().longValue() == purchase.getMaterialId().longValue() && Objects.equals(s.getVendorName(), purchase.getVendorName())) {
                        stock = s;
                        break;
                    }
                }
                if (null == stock)
                    return Response.fail("stock.not.found");
                stock.setQuantity(stock.getQuantity().add(changedQuantity));
                stockAndPurchases.put(stock, Collections.singletonList(purchase));
            }

            materialHandle.setFarmId(stockInventory.getFarmId());
            materialHandle.setWarehouseId(stockInventory.getWarehouseId());
            materialHandle.setMaterialId(detail.getMaterialId());
            materialHandle.setMaterialName(context.getSupportedMaterials().get(detail.getMaterialId()));
            materialHandle.setWarehouseType(context.getWareHouse().getType());
            materialHandle.setType(WarehouseMaterialHandlerType.INVENTORY.getValue());
            materialHandle.setQuantity(changedQuantity);
            materialHandle.setHandleDate(stockInventory.getHandleDate());
            materialHandle.setHandleYear(c.get(Calendar.YEAR));
            materialHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);

//            stocks.sort(Comparator.comparing(DoctorWarehouseStock::getUpdatedAt));
//            DoctorWarehouseStock stock = stocks.get(0);
//
//            int compareResult = detail.getQuantity().compareTo(stock.getQuantity());
//
//            if (compareResult == 0)
//                continue;
//
//            BigDecimal changeStockQuantity = stock.getQuantity().subtract(detail.getQuantity());
//            if (changeStockQuantity.compareTo(new BigDecimal(0)) < 0)
//                changeStockQuantity.negate();
//
//            //更新库存量
//            stock.setQuantity(detail.getQuantity());

//
//            if (compareResult > 0) {
//                //盘盈
//                Long unitPrice = lastPurchase.getUnitPrice();
//
//                DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
//                purchase.setHandleDate(stockInventory.getHandleDate());
//                purchase.setWarehouseId(stockInventory.getWarehouseId());
//                purchase.setWarehouseName(context.getWareHouse().getWareHouseName());
//                purchase.setMaterialId(detail.getMaterialId());
//                purchase.setVendorName(lastPurchase.getVendorName());
//                purchase.setQuantity(changeStockQuantity);
//                purchase.setHandleQuantity(new BigDecimal(0));
//                purchase.setUnitPrice(unitPrice);
//                purchase.setHandleMonth(c.get(Calendar.MONTH) + 1);
//                purchase.setHandleYear(c.get(Calendar.YEAR));
//
//                materialHandle.setUnitPrice(purchase.getUnitPrice());
//                stockAndPurchases.put(stock, Collections.singletonList(purchase));
//            } else if (compareResult < 0) {
//                //盘亏，减库存，减采购记录表中可用的量
//                List<DoctorWarehousePurchase> needHandlePurchase = new ArrayList<>();
//                long averagePrice = getNeedPurchaseAndCalcAveragePrice(needHandlePurchase, purchases, changeStockQuantity);
//                stockAndPurchases.put(stock, needHandlePurchase);
//                materialHandle.setUnitPrice(averagePrice);
//            }
            handleContext.setMaterialHandle(materialHandle);
            handleContext.setStockAndPurchases(stockAndPurchases);
            handleContexts.add(handleContext);
        }
        doctorWarehouseHandlerManager.handle(handleContexts);
        return Response.ok(true);
    }

    @Override
    public Response<Boolean> transfer(WarehouseStockTransferDto stockTransfer) {

        StockContext context = validAndGetContext(stockTransfer.getFarmId(), stockTransfer.getWarehouseId(), stockTransfer.getDetails());

        List<DoctorWarehouseHandlerManager.StockHandleContext> handleContexts = new ArrayList<>(stockTransfer.getDetails().size());
        for (WarehouseStockTransferDto.WarehouseStockTransferDetail detail : stockTransfer.getDetails()) {

            if (detail.getTransferInWarehouseId() == stockTransfer.getWarehouseId())
                return Response.fail("transfer.out.warehouse.equals.transfer.in.warehouse");

            Response<DoctorWareHouse> targetWareHouseResponse = doctorWareHouseReadService.findById(detail.getTransferInWarehouseId());
            if (!targetWareHouseResponse.isSuccess())
                return Response.fail(targetWareHouseResponse.getError());
            DoctorWareHouse targetWareHouse = targetWareHouseResponse.getResult();
            if (null == targetWareHouse)
                return Response.fail("warehouse.not.found");

            if (context.getWareHouse().getType().intValue() != targetWareHouse.getType().intValue())
                return Response.fail("transfer.warehouse.type.not.equals");

            //找到对应库存
            List<DoctorWarehouseStock> stocks = getStock(stockTransfer.getWarehouseId(), detail.getMaterialId());
            if (null == stocks || stocks.isEmpty())
                return Response.fail("stock.not.found");

            BigDecimal totalStockQuantity = new BigDecimal(0);
            for (DoctorWarehouseStock stock : stocks) {
                totalStockQuantity = totalStockQuantity.add(stock.getQuantity());
            }
            if (totalStockQuantity.compareTo(detail.getQuantity()) < 0)
                return Response.fail("stock.not.enough");


            DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
            purchaseCriteria.setWarehouseId(stockTransfer.getWarehouseId());
            purchaseCriteria.setMaterialId(detail.getMaterialId());
            purchaseCriteria.setHandleFinishFlag(1);
            List<DoctorWarehousePurchase> transferOutPurchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
            if (null == transferOutPurchases || transferOutPurchases.isEmpty())
                throw new ServiceException("purchase.not.found");
            transferOutPurchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));

//            List<DoctorWarehousePurchase> transferOutPurchase = new ArrayList<>();
//            List<DoctorWarehousePurchase> transferInPurchase = new ArrayList<>();
//
            Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockAndPurchases = new HashMap<>();


            Calendar c = Calendar.getInstance();
            c.setTime(stockTransfer.getHandleDate());

            long averagePrice = handleOutAndCalcAveragePrice(detail.getQuantity(), transferOutPurchases, stockAndPurchases, stocks, true, targetWareHouse, c);

//            for (DoctorWarehouseStock transferOutStock : stockAndPurchases.keySet()) {
//
//                List<DoctorWarehousePurchase> purchases = stockAndPurchases.get(transferOutStock);
//                List<DoctorWarehousePurchase> transferInPurchases = new ArrayList<>(purchases.size());
//                BigDecimal quantity = new BigDecimal(0);
//                for (DoctorWarehousePurchase purchase : purchases) {
//                    quantity = quantity.add(purchase.getQuantity())
//                }
//
//                //转入的库存
//                DoctorWarehouseStock transferInStock = getStock(detail.getTransferInWarehouseId(), detail.getMaterialId(), transferOutStock.getVendorName());
//                if (null == transferInStock) {
//                    transferInStock = new DoctorWarehouseStock();
//                    transferInStock.setFarmId(stockTransfer.getFarmId());
//                    transferInStock.setWarehouseId(detail.getTransferInWarehouseId());
//                    transferInStock.setWarehouseName(targetWareHouse.getWareHouseName());
//                    transferInStock.setWarehouseType(targetWareHouse.getType());
//                    transferInStock.setMaterialId(detail.getMaterialId());
//                    transferInStock.setMaterialName(context.getSupportedMaterials().get(detail.getMaterialId()));
//                    transferInStock.setVendorName(transferOutStock.getVendorName());
//                    transferInStock.setUnit(transferOutStock.getUnit());
//                    transferInStock.setQuantity(detail.getQuantity());
//                } else
//                    transferInStock.setQuantity(transferInStock.getQuantity().add(detail.getQuantity()));
//            }

//            transferOutStock.setQuantity(transferOutStock.getQuantity().subtract(detail.getQuantity()));
//            stockAndPurchases.put(transferOutStock, transferOutPurchase);

//            handleContext.put(transferInStock, transferInPurchase);
//            stockAndPurchases.put(transferInStock, transferInPurchase);
            DoctorWarehouseHandlerManager.StockHandleContext handleContext = new DoctorWarehouseHandlerManager.StockHandleContext();
            handleContext.setStockAndPurchases(stockAndPurchases);

            DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
            materialHandle.setFarmId(stockTransfer.getFarmId());
            materialHandle.setWarehouseId(stockTransfer.getWarehouseId());
            materialHandle.setMaterialId(detail.getMaterialId());
            materialHandle.setMaterialName(context.getSupportedMaterials().get(detail.getMaterialId()));
            materialHandle.setWarehouseType(context.getWareHouse().getType());
            materialHandle.setUnitPrice(averagePrice);
            materialHandle.setType(WarehouseMaterialHandlerType.TRANSFER.getValue());
            materialHandle.setQuantity(detail.getQuantity());
            materialHandle.setHandleDate(stockTransfer.getHandleDate());
            materialHandle.setHandleYear(c.get(Calendar.YEAR));
            materialHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);
            handleContext.setMaterialHandle(materialHandle);
            handleContexts.add(handleContext);
        }
        doctorWarehouseHandlerManager.handle(handleContexts);
        return Response.ok(true);
    }

    @Override
    public Response<Boolean> out(WarehouseStockOutDto stockOut) {

        StockContext context = validAndGetContext(stockOut.getFarmId(), stockOut.getWarehouseId(), stockOut.getDetails());
        List<DoctorWarehouseHandlerManager.StockHandleContext> handleContexts = new ArrayList<>();
        List<DoctorWarehouseMaterialApply> materialApplies = new ArrayList<>(stockOut.getDetails().size());
        for (WarehouseStockOutDto.WarehouseStockOutDetail detail : stockOut.getDetails()) {

            //无论什么供应商的都可以出库
            List<DoctorWarehouseStock> stocks = getStock(stockOut.getWarehouseId(), detail.getMaterialId());
            if (stocks.isEmpty())
                return Response.fail("stock.not.found");
            BigDecimal totalStockQuantity = new BigDecimal(0);
            for (DoctorWarehouseStock stock : stocks) {
                totalStockQuantity = totalStockQuantity.add(stock.getQuantity());
            }

            if (totalStockQuantity.compareTo(detail.getQuantity()) < 0)
                return Response.fail("stock.not.enough");
//
//            //先入先出
//            stocks.sort(Comparator.comparing(DoctorWarehouseStock::getCreatedAt).reversed());
//            Map<DoctorWarehouseStock, BigDecimal> outStocks = new HashMap<>();
//            for (DoctorWarehouseStock stock : stocks) {
//
//                outStocks.put(stock, totalStockQuantity);
//                totalStockQuantity.subtract(stock.getQuantity());
//            }

            DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
            purchaseCriteria.setWarehouseId(stockOut.getWarehouseId());
            purchaseCriteria.setMaterialId(detail.getMaterialId());
            purchaseCriteria.setHandleFinishFlag(1);//未出库完的
            List<DoctorWarehousePurchase> materialPurchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
            if (null == materialPurchases || materialPurchases.isEmpty())
                return Response.fail("purchase.not.found");
            materialPurchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));

//            List<DoctorWarehousePurchase> outPurchase = new ArrayList<>();
//            long averagePrice = getNeedPurchaseAndCalcAveragePrice(outPurchase, materialPurchases, detail.getQuantity());
            Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockWithPurchases = new HashMap<>();
            long averagePrice = handleOutAndCalcAveragePrice(detail.getQuantity(), materialPurchases, stockWithPurchases, stocks, false, null, null);

            DoctorWarehouseHandlerManager.StockHandleContext handleContext = new DoctorWarehouseHandlerManager.StockHandleContext();
            handleContext.setStockAndPurchases(stockWithPurchases);

            DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
            materialHandle.setFarmId(stockOut.getFarmId());
            materialHandle.setWarehouseId(stockOut.getWarehouseId());
            materialHandle.setMaterialId(detail.getMaterialId());
            materialHandle.setMaterialName(context.getSupportedMaterials().get(detail.getMaterialId()));
            materialHandle.setWarehouseType(context.getWareHouse().getType());
            materialHandle.setUnitPrice(averagePrice);
            materialHandle.setType(WarehouseMaterialHandlerType.OUT.getValue());
            materialHandle.setQuantity(detail.getQuantity());
            materialHandle.setHandleDate(stockOut.getHandleDate());
            Calendar c = Calendar.getInstance();
            c.setTime(stockOut.getHandleDate());
            materialHandle.setHandleYear(c.get(Calendar.YEAR));
            materialHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);
            handleContext.setMaterialHandle(materialHandle);

            DoctorWarehouseMaterialApply materialApply = new DoctorWarehouseMaterialApply();
            materialApply.setMaterialId(detail.getMaterialId());
            materialApply.setMaterialName(context.getSupportedMaterials().get(detail.getMaterialId()));
            materialApply.setApplyDate(stockOut.getHandleDate());
            materialApply.setApplyYear(c.get(Calendar.YEAR));
            materialApply.setApplyMonth(c.get(Calendar.MONTH) + 1);
            materialApply.setType(context.getWareHouse().getType());
            materialApply.setUnit(stocks.get(0).getUnit());//单位都是一致的
            materialApply.setQuantity(detail.getQuantity());
            materialApply.setUnitPrice(averagePrice);
            materialApply.setPigBarnId(detail.getApplyPigBarnId());
            materialApply.setPigBarnName(detail.getApplyPigBarnName());
            materialApply.setPigGroupId(detail.getApplyPigGroupId());
            materialApply.setPigGroupName(detail.getApplyPigGroupName());
            materialApply.setApplyStaffName(detail.getApplyStaffName());
            materialApplies.add(materialApply);

            handleContexts.add(handleContext);
        }
        doctorWarehouseMaterialApplyManager.creates(materialApplies);
        doctorWarehouseHandlerManager.handle(handleContexts);
        return Response.ok(true);
    }

//    private void getHnaldeOut(Long warehouseID, Long materialID, String vendorName) {
//        DoctorWarehouseMaterialHandle inHandleCriteria = new DoctorWarehouseMaterialHandle();
//        inHandleCriteria.setWarehouseId(warehouseID);
//        inHandleCriteria.setMarterialId(materialID);
//        inHandleCriteria.setType(WarehouseMaterialHandlerType.IN.getValue());
//        inHandleCriteria.setVendorName(vendorName);
//        List<DoctorWarehouseMaterialHandle> inhandles = doctorWarehouseMaterialHandleDao.list(inHandleCriteria);
//        if (null == inhandles || inhandles.isEmpty())
//            throw new ServiceException("stock.in.not.found");
//        //先入库的先出库
//        inhandles.sort(Comparator.comparing(DoctorWarehouseMaterialHandle::getHandleDate));
//
//        DoctorWarehouseMaterialHandle outHandleCriteria = new DoctorWarehouseMaterialHandle();
//        outHandleCriteria.setWarehouseId(warehouseID);
//        outHandleCriteria.setMarterialId(materialID);
//        outHandleCriteria.setType(WarehouseMaterialHandlerType.OUT.getValue());
//        outHandleCriteria.setVendorName(vendorName);
//        List<DoctorWarehouseMaterialHandle> outHandles = doctorWarehouseMaterialHandleDao.list(outHandleCriteria);
//        Map<String, BigDecimal> alreadyOut = new HashMap<>();
//        if (null != outHandles) {
//            for (DoctorWarehouseMaterialHandle outHandle : outHandles) {
//                String key = outHandle.getWarehouseId() + "|" + outHandle.getMarterialId() + "|" + outHandle.getVendorName();
//                if (alreadyOut.containsKey(key)) {
//
//                }
//            }
//        }
//    }


    //    @Override
//    public Response<Boolean> out(List<DoctorWarehouseStockHandleDto> dtos, DoctorWarehouseStockHandler handle) {
//
//        try {
//            doctorWarehouseHandlerManager.outStock(dtos, handle);
//        } catch (Exception e) {
//            log.error("failed to out of stock,cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("doctor.warehouse.stock.out.fail");
//        }
//
//        return Response.ok(true);
//    }

    @Override
    public Response<Boolean> outAndIn(List<DoctorWarehouseStockHandleDto> inHandles, List<DoctorWarehouseStockHandleDto> outHandles, DoctorWarehouseStockHandler handle) {
        try {
            doctorWarehouseHandlerManager.inAndOutStock(inHandles, outHandles, handle);
        } catch (Exception e) {
            log.error("failed to out of stock,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.out.fail");
        }

        return Response.ok(true);
    }


    private StockContext validAndGetContext(Long farmID, Long warehoueID, List<? extends AbstractWarehouseStockDetail> details) {

        //查询基础数据，农场可添加物料
        Response<DoctorFarmBasic> farmBasicResponse = doctorFarmBasicReadService.findFarmBasicByFarmId(farmID);
        if (!farmBasicResponse.isSuccess())
            throw new ServiceException(farmBasicResponse.getError());
        DoctorFarmBasic farmBasic = farmBasicResponse.getResult();
        if (null == farmBasic)
            throw new ServiceException("farm.basic.not.found");

        List<Long> currentFarmSupportedMaterials = farmBasic.getMaterialIdList();

        Response<DoctorWareHouse> wareHouseResponse = doctorWareHouseReadService.findById(warehoueID);
        if (!wareHouseResponse.isSuccess())
            throw new JsonResponseException(wareHouseResponse.getError());
        DoctorWareHouse wareHouse = wareHouseResponse.getResult();
        if (null == wareHouse)
            throw new JsonResponseException("warehouse.not.found");


        Map<Long, String> supportedMaterials = new HashedMap(details.size());
        details.forEach(detail -> {
            if (!currentFarmSupportedMaterials.contains(detail.getMaterialId()))
                throw new ServiceException("material.not.allow.in.this.warehouse");

            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findById(detail.getMaterialId());
            if (null == basicMaterial)
                throw new ServiceException("material.not.found");

            supportedMaterials.put(detail.getMaterialId(), basicMaterial.getName());
        });

        StockContext context = new StockContext();
        context.setSupportedMaterials(supportedMaterials);
        context.setWareHouse(wareHouse);
        return context;
    }

    private DoctorWarehouseStock getAvailableStock(DoctorWareHouse wareHouse, WarehouseStockInDto.WarehouseStockInDetailDto detail, String materialName) {

        DoctorWarehouseStock stock = getStock(wareHouse.getId(), detail.getMaterialId(), detail.getVendorName());

        if (null == stock) {
            stock = new DoctorWarehouseStock();
            stock.setFarmId(wareHouse.getFarmId());
            stock.setWarehouseId(wareHouse.getId());
            stock.setWarehouseName(wareHouse.getWareHouseName());
            stock.setWarehouseType(wareHouse.getType());
            stock.setMaterialId(detail.getMaterialId());
            stock.setMaterialName(materialName);
            if (StringUtils.isBlank(detail.getVendorName()))
                stock.setVendorName(DEFAULT_VENDOR_NAME);
            else
                stock.setVendorName(detail.getVendorName());
            stock.setUnit(detail.getUnit());
            stock.setQuantity(detail.getQuantity());
            return stock;
        } else
            return stock;
    }

    private DoctorWarehouseStock getStock(Long warehouseID, Long materialID, String vendorName) {
        DoctorWarehouseStock criteria = new DoctorWarehouseStock();
        criteria.setWarehouseId(warehouseID);
        criteria.setMaterialId(materialID);
        if (StringUtils.isNotBlank(vendorName))
            criteria.setVendorName(vendorName);
        else criteria.setVendorName(DEFAULT_VENDOR_NAME);

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(criteria);
        if (null == stocks || stocks.isEmpty())
            return null;
        else return stocks.get(0);
    }

    private List<DoctorWarehouseStock> getStock(Long warehouseID, Long materialID) {
        DoctorWarehouseStock criteria = new DoctorWarehouseStock();
        criteria.setWarehouseId(warehouseID);
        criteria.setMaterialId(materialID);

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(criteria);
        if (null == stocks)
            return Collections.emptyList();

        return stocks;
    }

    private DoctorWarehousePurchase copyPurchase(DoctorWarehousePurchase source, Calendar handleDate, Long warehouseID, String warehouseName, BigDecimal quantity) {
        DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
        purchase.setHandleDate(handleDate.getTime());
        purchase.setHandleYear(handleDate.get(Calendar.YEAR));
        purchase.setHandleMonth(handleDate.get(Calendar.MONTH) + 1);

        purchase.setWarehouseId(warehouseID);
        purchase.setWarehouseName(warehouseName);
        purchase.setMaterialId(source.getMaterialId());
        purchase.setVendorName(source.getVendorName());
        purchase.setQuantity(quantity);
        purchase.setHandleQuantity(new BigDecimal(0));
        purchase.setUnitPrice(source.getUnitPrice());
        purchase.setHandleFinishFlag(1);
        purchase.setFarmId(source.getFarmId());
        return purchase;
    }

    private long handleOutAndCalcAveragePrice(BigDecimal totalNeedOutQuantity,
                                              List<DoctorWarehousePurchase> purchases,
                                              Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockWithPurchase,
                                              List<DoctorWarehouseStock> stocks,
                                              boolean isTransfer,
                                              DoctorWareHouse targetWarehouse,
                                              Calendar handleDate) {

        Map<String, DoctorWarehouseStock> materialStocks = new HashMap<>();
        for (DoctorWarehouseStock stock : stocks) {
            String key = stock.getMaterialId() + "|" + stock.getVendorName();
            materialStocks.put(key, stock);
        }

        Map<DoctorWarehouseStock, BigDecimal> stockChangedQuantity = null;
        if (isTransfer) {
            stockChangedQuantity = new HashMap<>();
        }

        BigDecimal needPurchaseQuantity = new BigDecimal(totalNeedOutQuantity.toString());

        BigDecimal totalHandleQuantity = new BigDecimal(0);
        long totalHandleMoney = 0L;
        for (DoctorWarehousePurchase purchase : purchases) {
            if (needPurchaseQuantity.compareTo(new BigDecimal(0)) <= 0)
                break;

            BigDecimal availablePurchaseQuantity = purchase.getQuantity().subtract(purchase.getHandleQuantity());
            BigDecimal actualCutDownQuantity = availablePurchaseQuantity;
            if (needPurchaseQuantity.compareTo(availablePurchaseQuantity) <= 0) {
                actualCutDownQuantity = needPurchaseQuantity;
            }

            purchase.setHandleQuantity(purchase.getHandleQuantity().add(actualCutDownQuantity));
            if (purchase.getHandleQuantity().compareTo(purchase.getQuantity()) >= 0)
                purchase.setHandleFinishFlag(0);

            DoctorWarehouseStock stock = materialStocks.get(purchase.getMaterialId() + "|" + purchase.getVendorName());
            //扣减库存
            stock.setQuantity(stock.getQuantity().subtract(actualCutDownQuantity));
            log.info("库存[{}]扣减{}{}", stock.getId(), actualCutDownQuantity, stock.getUnit());
            if (!stockWithPurchase.containsKey(stock)) {
                List<DoctorWarehousePurchase> stockPurchases = new ArrayList<>();
                stockPurchases.add(purchase);
                stockWithPurchase.put(stock, stockPurchases);
            } else
                stockWithPurchase.get(stock).add(purchase);

            if (isTransfer) {
                //如果是调拨，还需要根据转出构建对应的转入stock和purchase
                if (!stockChangedQuantity.containsKey(stock)) {
                    stockChangedQuantity.put(stock, actualCutDownQuantity);
                } else {
                    BigDecimal newQuantity = stockChangedQuantity.get(stock).add(actualCutDownQuantity);
                    stockChangedQuantity.put(stock, newQuantity);
                }
            }

            totalHandleMoney += actualCutDownQuantity.multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
            totalHandleQuantity = totalHandleQuantity.add(actualCutDownQuantity);

            needPurchaseQuantity = needPurchaseQuantity.subtract(actualCutDownQuantity);
        }


        if (isTransfer) {
            for (DoctorWarehouseStock stock : stockWithPurchase.keySet()) {
                //根据出库构造入库
                DoctorWarehouseStock transferInStock = getStock(targetWarehouse.getId(), stock.getMaterialId(), stock.getVendorName());
                if (null == transferInStock) {
                    transferInStock = new DoctorWarehouseStock();
                    transferInStock.setFarmId(stock.getFarmId());
                    transferInStock.setWarehouseId(targetWarehouse.getId());
                    transferInStock.setWarehouseName(targetWarehouse.getWareHouseName());
                    transferInStock.setWarehouseType(targetWarehouse.getType());
                    transferInStock.setMaterialId(stock.getMaterialId());
                    transferInStock.setMaterialName(stock.getMaterialName());
                    transferInStock.setVendorName(stock.getVendorName());
                    transferInStock.setUnit(stock.getUnit());
                    transferInStock.setQuantity(stockChangedQuantity.get(stock));
                } else
                    transferInStock.setQuantity(transferInStock.getQuantity().add(stockChangedQuantity.get(stock)));

                List<DoctorWarehousePurchase> transferOutPurchase = stockWithPurchase.get(stock);
                List<DoctorWarehousePurchase> transferInPurchase = new ArrayList<>(transferOutPurchase.size());
                for (DoctorWarehousePurchase purchase : transferOutPurchase) {
                    transferInPurchase.add(copyPurchase(purchase, handleDate, targetWarehouse.getId(), targetWarehouse.getWareHouseName(), stockChangedQuantity.get(stock)));
                }

                stockWithPurchase.put(transferInStock, transferInPurchase);
            }
        }

        //去除小数部分，四舍五入
        return new BigDecimal(totalHandleMoney).divide(totalHandleQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();
    }

    private long getNeedPurchaseAndCalcAveragePrice(List<DoctorWarehousePurchase> needHandlePurchase, List<DoctorWarehousePurchase> purchases, BigDecimal totalQuantity) {

        BigDecimal needPurchaseQuantity = totalQuantity;

        BigDecimal totalHandleQuantity = new BigDecimal(0);
        long totalHandleMoney = 0L;
        for (DoctorWarehousePurchase purchase : purchases) {
            if (needPurchaseQuantity.compareTo(new BigDecimal(0)) <= 0)
                break;

            BigDecimal availablePurchaseQuantity = purchase.getQuantity().subtract(purchase.getHandleQuantity());
            BigDecimal actualCutDownQuantity = availablePurchaseQuantity;
            if (needPurchaseQuantity.compareTo(availablePurchaseQuantity) <= 0) {
                actualCutDownQuantity = needPurchaseQuantity;
            }

            purchase.setHandleQuantity(purchase.getHandleQuantity().add(actualCutDownQuantity));
            if (purchase.getHandleQuantity().compareTo(purchase.getQuantity()) >= 0)
                purchase.setHandleFinishFlag(0);
            needHandlePurchase.add(purchase);
            totalHandleMoney += actualCutDownQuantity.multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
            totalHandleQuantity = totalHandleQuantity.add(actualCutDownQuantity);

            needPurchaseQuantity = needPurchaseQuantity.subtract(actualCutDownQuantity);
        }
        //去除小数部分，四舍五入
        return new BigDecimal(totalHandleMoney).divide(totalHandleQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();
    }


    @Data
    public class StockContext {
        private DoctorWareHouse wareHouse;
        private Map<Long, String> supportedMaterials;
    }
}