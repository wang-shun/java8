package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.DoctorWarehouseStockHandleDto;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.manager.*;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.*;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorFarmBasicReadService;
import io.terminus.doctor.common.exception.InvalidException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    private DoctorWareHouseDao doctorWareHouseDao;

    @Autowired
    private DoctorBasicMaterialDao doctorBasicMaterialDao;

    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehouseMaterialApplyManager doctorWarehouseMaterialApplyManager;
    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;
    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;


    @Autowired
    private DoctorWarehouseStockManager doctorWarehouseStockManager;
    @Autowired
    private DoctorWarehouseMaterialHandleManager doctorWarehouseMaterialHandleManager;
    @Autowired
    private DoctorWarehousePurchaseManager doctorWarehousePurchaseManager;
    @Autowired
    private DoctorWarehouseStockHandleManager doctorWarehouseStockHandleManager;
    @Autowired
    private DoctorWarehouseStockMonthlyManager doctorWarehouseStockMonthlyManager;

    @Autowired
    private DoctorBasicDao doctorBasicDao;

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

            if (stock.getQuantity().compareTo(new BigDecimal(0)) > 0)
                return Response.fail("stock.not.empty");

            return Response.ok(doctorWarehouseStockDao.delete(id));
        } catch (Exception e) {
            log.error("failed to delete doctor warehouse stock by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.delete.fail");
        }
    }

    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.stock.in.fail")
    public Response<Boolean> in(WarehouseStockInDto stockIn) {


//        boolean updateMode = stockIn.getStockHandleId() != null;
//        if (updateMode)
//            doctorWarehouseStockHandleManager.check(stockIn, stockIn.getDetails(), WarehouseMaterialHandleType.IN);

        StockContext context = getWarehouseAndSupportedBasicMaterial(stockIn.getFarmId(), stockIn.getWarehouseId());

        String serialNo = "R" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");

        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleManager.handle(stockIn, context.getWareHouse(), serialNo, WarehouseMaterialHandleType.IN);

        stockIn.getDetails().forEach(detail -> {

            DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(detail.getMaterialId());
            if (null == sku)
                throw new InvalidException("warehouse.sku.not.found", detail.getMaterialId());
            if (!sku.getType().equals(context.getWareHouse().getType()))
                throw new InvalidException("basic.material.not.allow.in.this.warehouse", sku.getItemId(), context.getWareHouse().getWareHouseName());
//            if (!context.getSupportedMaterials().containsKey(sku.getItemId()))
//                throw new InvalidException("basic.material.not.allow.in.this.warehouse", sku.getItemId(), context.getWareHouse().getWareHouseName());

            DoctorWarehouseStock stock = doctorWarehouseStockManager.in(stockIn, detail, context, sku);

            //记录物料采购入库的单价
            DoctorWarehousePurchase purchase = doctorWarehousePurchaseManager.in(stockIn, detail, stock, sku);

            doctorWarehouseMaterialHandleManager.in(DoctorWarehouseMaterialHandleManager.MaterialHandleContext.builder()
                    .stock(stock)
                    .stockDto(stockIn)
                    .stockDetail(detail)
                    .sku(sku)
                    .unitPrice(detail.getUnitPrice())
                    .vendorName(purchase.getVendorName())
                    .quantity(detail.getQuantity())
                    .purchases(Collections.singletonMap(purchase, detail.getQuantity()))
                    .stockHandle(stockHandle)
                    .build());

            doctorWarehouseStockMonthlyManager.count(stock.getWarehouseId(),
                    stock.getSkuId(),
                    stockIn.getHandleDate().get(Calendar.YEAR),
                    stockIn.getHandleDate().get(Calendar.MONTH) + 1,
                    detail.getQuantity(),
                    detail.getUnitPrice(),
                    true);
        });


//        }

//        if (updateMode) {
        //TODO 再把原来的入库全部铲掉
//        }

        return Response.ok(true);
    }


    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.stock.inventory.fail")
    public Response<Boolean> inventory(WarehouseStockInventoryDto stockInventory) {

//        StockContext context = validAndGetContext(stockInventory.getFarmId(), stockInventory.getWarehouseId(), stockInventory.getDetails());
        StockContext context = getWarehouseAndSupportedBasicMaterial(stockInventory.getFarmId(), stockInventory.getWarehouseId());

        boolean updateMode = stockInventory.getStockHandleId() != null;

        Map<Long/*skuID*/, List<DoctorWarehouseMaterialHandle>> skuHandle = doctorWarehouseMaterialHandleDao.findByStockHandle(stockInventory.getStockHandleId()).stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));

        String serialNo = updateMode ? "" : "P" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");

        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleManager.handle(stockInventory, context.getWareHouse(), serialNo, WarehouseMaterialHandleType.INVENTORY);

        for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail : stockInventory.getDetails()) {

            DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(detail.getMaterialId());
            if (null == sku)
                throw new InvalidException("warehouse.sku.not.found", detail.getMaterialId());
            if (!sku.getType().equals(context.getWareHouse().getType()))
                throw new InvalidException("basic.material.not.allow.in.this.warehouse", sku.getItemId(), context.getWareHouse().getWareHouseName());
//            if (!context.getSupportedMaterials().containsKey(sku.getItemId()))
//                throw new InvalidException("basic.material.not.allow.in.this.warehouse", sku.getItemId(), context.getWareHouse().getWareHouseName());

            //找到对应库存
            DoctorWarehouseStock stock = getStock(stockInventory.getWarehouseId(), detail.getMaterialId(), null);
            if (null == stock)
                throw new InvalidException("stock.not.found", context.getWareHouse().getWareHouseName(), sku.getName());

            int compareResult = stock.getQuantity().compareTo(detail.getQuantity());

            if (!updateMode && compareResult == 0) {
                log.info("盘点库存量与原库存量一致，warehouse[{}],material[{}]", stockInventory.getWarehouseId(), detail.getMaterialId());
                continue;
            }

            DoctorWarehouseMaterialHandle oldHandle = null;
            if (updateMode && skuHandle.containsKey(detail.getMaterialId())) {
                oldHandle = skuHandle.get(detail.getMaterialId()).get(0);
            }

            DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
            purchaseCriteria.setWarehouseId(stock.getWarehouseId());
            purchaseCriteria.setMaterialId(stock.getSkuId());

            DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
            materialHandle.setStockHandleId(stockHandle.getId());
            materialHandle.setFarmId(stockInventory.getFarmId());
            materialHandle.setWarehouseId(stockInventory.getWarehouseId());
            materialHandle.setWarehouseName(context.getWareHouse().getWareHouseName());
            materialHandle.setWarehouseType(context.getWareHouse().getType());
            materialHandle.setMaterialId(detail.getMaterialId());
            materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
            materialHandle.setMaterialName(sku.getName());

            materialHandle.setHandleDate(stockInventory.getHandleDate().getTime());
            materialHandle.setHandleYear(stockInventory.getHandleDate().get(Calendar.YEAR));
            materialHandle.setHandleMonth(stockInventory.getHandleDate().get(Calendar.MONTH) + 1);
            DoctorBasic unit = doctorBasicDao.findById(Long.parseLong(sku.getUnit()));
            if (null != unit)
                materialHandle.setUnit(unit.getName());
            materialHandle.setBeforeInventoryQuantity(stock.getQuantity());
            materialHandle.setOperatorId(stockInventory.getOperatorId());
            materialHandle.setOperatorName(stockInventory.getOperatorName());
            materialHandle.setRemark(detail.getRemark());

            BigDecimal changedQuantity;
            if (compareResult > 0) {  //盘亏

                if (updateMode
                        && null != oldHandle
                        && oldHandle.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())
                        && stock.getQuantity().compareTo(oldHandle.getQuantity()) < 0) {
                    throw new InvalidException("stock.not.enough", stock.getWarehouseName(), stock.getSkuName(), stock.getQuantity(), sku.getUnit());
                }

                purchaseCriteria.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
                List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
                if (null == purchases || purchases.isEmpty())
                    throw new InvalidException("purchase.not.found", stock.getWarehouseName(), stock.getSkuName());
                //根据处理日期倒序，找出最近一次的入库记录
                purchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));

                //盘亏
                changedQuantity = stock.getQuantity().subtract(detail.getQuantity());
//                    long averagePrice = handleOutAndCalcAveragePrice(changedQuantity, purchases, stockAndPurchases, stocks, false, null, null);
                DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = getNeedPurchase(purchases, changedQuantity);
                materialHandle.setUnitPrice(doctorWarehousePurchaseManager.calculateUnitPrice(stock));

                materialHandle.setType(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
                materialHandle.setQuantity(changedQuantity);
                stock.setQuantity(detail.getQuantity());
                doctorWarehouseHandlerManager.outStock(stock, purchaseHandleContext, materialHandle);
                doctorWarehouseStockMonthlyManager.count(stock.getWarehouseId(),
                        stock.getSkuId(),
                        stockInventory.getHandleDate().get(Calendar.YEAR),
                        stockInventory.getHandleDate().get(Calendar.MONTH) + 1,
                        changedQuantity,
                        materialHandle.getUnitPrice(),
                        false);
            } else {

                if (updateMode && null != oldHandle && oldHandle.getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue())
                        && detail.getQuantity().compareTo(oldHandle.getQuantity()) < 0)
                    throw new InvalidException("stock.not.enough", stock.getWarehouseName(), stock.getSkuName(), detail.getQuantity(), sku.getUnit());

                PageInfo page = new PageInfo(1, 1);
                Paging<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.paging(page.getOffset(), page.getLimit(), purchaseCriteria);
                if (null == purchases || purchases.isEmpty())
                    new InvalidException("purchase.not.found", stock.getWarehouseName(), stock.getSkuName());

                DoctorWarehousePurchase lastPurchase = purchases.getData().get(0);

                //盘盈
                changedQuantity = detail.getQuantity().subtract(stock.getQuantity());
                DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
                purchase.setFarmId(stock.getFarmId());
                purchase.setHandleDate(stockInventory.getHandleDate().getTime());
                purchase.setWarehouseId(stock.getWarehouseId());
                purchase.setWarehouseName(stock.getWarehouseName());
                purchase.setWarehouseType(stock.getWarehouseType());
                purchase.setMaterialId(stock.getSkuId());
                purchase.setVendorName(lastPurchase.getVendorName());
                purchase.setQuantity(changedQuantity);
                purchase.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
                purchase.setHandleQuantity(new BigDecimal(0));
                purchase.setUnitPrice(lastPurchase.getUnitPrice());
                purchase.setHandleMonth(stockInventory.getHandleDate().get(Calendar.MONTH) + 1);
                purchase.setHandleYear(stockInventory.getHandleDate().get(Calendar.YEAR));

                materialHandle.setQuantity(changedQuantity);
                materialHandle.setUnitPrice(purchase.getUnitPrice());
                materialHandle.setVendorName(purchase.getVendorName());
                materialHandle.setType(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
                stock.setQuantity(detail.getQuantity());

                doctorWarehouseHandlerManager.inStock(stock, Collections.singletonList(purchase), materialHandle, null, null);
                doctorWarehouseStockMonthlyManager.count(stock.getWarehouseId(),
                        stock.getSkuId(),
                        stockInventory.getHandleDate().get(Calendar.YEAR),
                        stockInventory.getHandleDate().get(Calendar.MONTH) + 1,
                        changedQuantity,
                        materialHandle.getUnitPrice(),
                        true);
            }

        }

        doctorWarehouseStockHandleManager.delete(stockInventory.getStockHandleId());

        return Response.ok(true);
    }

    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.stock.transfer.fail")
    public Response<Boolean> transfer(WarehouseStockTransferDto stockTransfer) {

//        StockContext context = validAndGetContext(stockTransfer.getFarmId(), stockTransfer.getWarehouseId(), stockTransfer.getDetails());
        StockContext context = getWarehouseAndSupportedBasicMaterial(stockTransfer.getFarmId(), stockTransfer.getWarehouseId());

        String serialNo = "D" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");

        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleManager.handle(stockTransfer, context.getWareHouse(), serialNo, WarehouseMaterialHandleType.TRANSFER);

        List<DoctorWarehouseHandlerManager.StockHandleContext> handleContexts = new ArrayList<>(stockTransfer.getDetails().size());
        for (WarehouseStockTransferDto.WarehouseStockTransferDetail detail : stockTransfer.getDetails()) {


            DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(detail.getMaterialId());
            if (null == sku)
                throw new InvalidException("warehouse.sku.not.found", detail.getMaterialId());

            if (detail.getTransferInWarehouseId() == stockTransfer.getWarehouseId())
                throw new InvalidException("transfer.out.warehouse.equals.transfer.in.warehouse", context.getWareHouse().getWareHouseName(), sku.getName());

            DoctorWareHouse targetWareHouse = doctorWareHouseDao.findById(detail.getTransferInWarehouseId());
            if (null == targetWareHouse)
                throw new InvalidException("warehouse.not.found", detail.getTransferInWarehouseId());

            if (context.getWareHouse().getType().intValue() != targetWareHouse.getType().intValue())
                throw new InvalidException("transfer.warehouse.type.not.equals", context.getWareHouse().getWareHouseName(), targetWareHouse.getWareHouseName());

//            if (!Objects.equals(context.getWareHouse().getManagerId(), targetWareHouse.getManagerId())) {
//                throw new InvalidException("transfer.warehouse.manager.id.not.equals", targetWareHouse.getManagerName(), context.getWareHouse().getManagerName());
//            }

            DoctorBasic unit = doctorBasicDao.findById(Long.parseLong(sku.getUnit()));

            //找到对应库存
            DoctorWarehouseStock stock = getStock(stockTransfer.getWarehouseId(), detail.getMaterialId(), null);
            if (null == stock)
                throw new InvalidException("stock.not.found", context.getWareHouse().getWareHouseName(), sku.getName());

            if (stock.getQuantity().compareTo(detail.getQuantity()) < 0)
                throw new InvalidException("stock.not.enough", stock.getWarehouseName(), stock.getSkuName(), stock.getQuantity(), unit.getName());


//            DoctorWarehouseSku targetSku = doctorWarehouseSkuDao.findByFarmIdAndCode(sku.getFarmId(), sku.getCode()).orElseGet(() -> {
//                DoctorWarehouseSku newTargetSku = new DoctorWarehouseSku();
//                newTargetSku.setOrgId(sku.getOrgId());
//                newTargetSku.setFarmId(targetWareHouse.getFarmId());
//                newTargetSku.setWarehouseId(targetWareHouse.getId());
//                newTargetSku.setItemId(sku.getItemId());
//                newTargetSku.setName(sku.getName());
//                newTargetSku.setCode(sku.getCode());
//                newTargetSku.setSrm(sku.getSrm());
//                newTargetSku.setVendorName(sku.getVendorName());
//                newTargetSku.setUnit(sku.getUnit());
//                newTargetSku.setSpecification(sku.getSpecification());
//
//                doctorWarehouseSkuDao.create(newTargetSku);
//                return newTargetSku;
//            });

            DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
            purchaseCriteria.setWarehouseId(stockTransfer.getWarehouseId());
            purchaseCriteria.setMaterialId(detail.getMaterialId());
            purchaseCriteria.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
            List<DoctorWarehousePurchase> transferOutPurchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
            if (null == transferOutPurchases || transferOutPurchases.isEmpty())
                throw new InvalidException("purchase.not.found", stock.getWarehouseName(), stock.getSkuName());
            transferOutPurchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));

//                long averagePrice = handleOutAndCalcAveragePrice(detail.getQuantity(), transferOutPurchases, stockAndPurchases, stocks, true, targetWareHouse, c);
            long averagePrice = doctorWarehousePurchaseManager.calculateUnitPrice(stock);
            DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = getNeedPurchase(transferOutPurchases, detail.getQuantity());


            //调出
            DoctorWarehouseMaterialHandle outHandle = buildMaterialHandle(stock, stockTransfer, detail.getQuantity(), averagePrice, WarehouseMaterialHandleType.TRANSFER_OUT.getValue());
            outHandle.setStockHandleId(stockHandle.getId());
            outHandle.setHandleYear(stockTransfer.getHandleDate().get(Calendar.YEAR));
            outHandle.setHandleMonth(stockTransfer.getHandleDate().get(Calendar.MONTH) + 1);
            outHandle.setRemark(detail.getRemark());
            if (null != unit)
                outHandle.setUnit(unit.getName());
            outHandle.setBeforeInventoryQuantity(stock.getQuantity());
            stock.setQuantity(stock.getQuantity().subtract(detail.getQuantity()));
            doctorWarehouseHandlerManager.outStock(stock, purchaseHandleContext, outHandle);
            doctorWarehouseStockMonthlyManager.count(stock.getWarehouseId(),
                    stock.getSkuId(),
                    outHandle.getHandleYear(),
                    outHandle.getHandleMonth(),
                    detail.getQuantity(),
                    averagePrice,
                    false);

            DoctorWarehouseStock transferInStock = getStock(targetWareHouse.getId(), sku.getId(), null);
            if (null == transferInStock) {
                transferInStock = new DoctorWarehouseStock();
                transferInStock.setFarmId(targetWareHouse.getFarmId());
                transferInStock.setWarehouseId(targetWareHouse.getId());
                transferInStock.setWarehouseName(targetWareHouse.getWareHouseName());
                transferInStock.setWarehouseType(targetWareHouse.getType());

                transferInStock.setSkuId(sku.getId());
                transferInStock.setSkuName(sku.getName());
//                transferInStock.setUnit(stock.getUnit());
                transferInStock.setQuantity(detail.getQuantity());
            } else
                transferInStock.setQuantity(transferInStock.getQuantity().add(detail.getQuantity()));
            //构造调入MaterialHandle记录
            DoctorWarehouseMaterialHandle inHandle = buildMaterialHandle(transferInStock, stockTransfer, detail.getQuantity(), averagePrice, WarehouseMaterialHandleType.TRANSFER_IN.getValue());
            inHandle.setHandleYear(stockTransfer.getHandleDate().get(Calendar.YEAR));
            inHandle.setHandleMonth(stockTransfer.getHandleDate().get(Calendar.MONTH) + 1);
            if (null != unit)
                inHandle.setUnit(unit.getName());
            inHandle.setRemark(detail.getRemark());
            inHandle.setBeforeInventoryQuantity(stock.getQuantity());
            inHandle.setOtherTransferHandleId(outHandle.getId());
            List<DoctorWarehousePurchase> transferInPurchase = new ArrayList<>();
            for (DoctorWarehousePurchase purchase : purchaseHandleContext.getPurchaseQuantity().keySet()) {
                transferInPurchase.add(copyPurchase(purchase, stockTransfer.getHandleDate(), targetWareHouse, purchaseHandleContext.getPurchaseQuantity().get(purchase)));
            }
            doctorWarehouseHandlerManager.inStock(transferInStock, transferInPurchase, inHandle, null, null);
            outHandle.setOtherTransferHandleId(inHandle.getId());
            doctorWarehouseMaterialHandleDao.update(outHandle);
            doctorWarehouseStockMonthlyManager.count(transferInStock.getWarehouseId(),
                    transferInStock.getSkuId(),
                    outHandle.getHandleYear(),
                    outHandle.getHandleMonth(),
                    detail.getQuantity(),
                    averagePrice,
                    true);
//                handleContext.addMaterialHandle(inHandle);

//                handleContexts.add(handleContext);
        }
//            doctorWarehouseHandlerManager.handle(handleContexts);


        return Response.ok(true);
    }

    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.stock.out.fail")
    public Response<Boolean> out(WarehouseStockOutDto stockOut) {

//        StockContext context = validAndGetContext(stockOut.getFarmId(), stockOut.getWarehouseId(), stockOut.getDetails());
        StockContext context = getWarehouseAndSupportedBasicMaterial(stockOut.getFarmId(), stockOut.getWarehouseId());

        String serialNo = "C" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");

        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleManager.handle(stockOut, context.getWareHouse(), serialNo, WarehouseMaterialHandleType.OUT);

        for (WarehouseStockOutDto.WarehouseStockOutDetail detail : stockOut.getDetails()) {

            DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(detail.getMaterialId());
            if (null == sku)
                throw new InvalidException("warehouse.sku.not.found", detail.getMaterialId());
            if (!sku.getType().equals(context.getWareHouse().getType()))
                throw new InvalidException("basic.material.not.allow.in.this.warehouse", sku.getItemId(), context.getWareHouse().getWareHouseName());
//            if (!context.getSupportedMaterials().containsKey(sku.getItemId()))
//                throw new InvalidException("basic.material.not.allow.in.this.warehouse", sku.getItemId(), context.getWareHouse().getWareHouseName());

            DoctorBasic unit = doctorBasicDao.findById(Long.parseLong(sku.getUnit()));

            DoctorWarehouseStock stock = doctorWarehouseStockManager.out(stockOut, detail, context, sku, unit);

            DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = doctorWarehousePurchaseManager.out(stock, detail.getQuantity());
            long unitPrice = doctorWarehousePurchaseManager.calculateUnitPrice(stock);

            DoctorWarehouseMaterialHandle materialHandle = doctorWarehouseMaterialHandleManager.out(DoctorWarehouseMaterialHandleManager.MaterialHandleContext.builder()
                    .stock(stock)
                    .stockDto(stockOut)
                    .stockDetail(detail)
                    .sku(sku)
                    .quantity(detail.getQuantity())
                    .unitPrice(unitPrice)
                    .purchases(purchaseHandleContext.getPurchaseQuantity())
                    .stockHandle(stockHandle)
                    .build());
            doctorWarehouseMaterialApplyManager.apply(materialHandle, detail);

            doctorWarehouseStockMonthlyManager.count(stock.getWarehouseId(),
                    stock.getSkuId(),
                    stockOut.getHandleDate().get(Calendar.YEAR),
                    stockOut.getHandleDate().get(Calendar.MONTH) + 1,
                    detail.getQuantity(),
                    unitPrice,
                    false);
        }


        return Response.ok(true);
    }

    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.produce.formula.fail")
    public Response<Boolean> formula(WarehouseFormulaDto formulaDto) {
        long totalAmount = 0;
        BigDecimal totalQuantity = new BigDecimal(0);

//            List<DoctorWarehouseHandlerManager.StockHandleContext> handleContexts = new ArrayList<>();
        for (WarehouseFormulaDto.WarehouseFormulaDetail detail : formulaDto.getDetails()) {

            List<DoctorWarehouseStock> stocks = getStockByFarm(formulaDto.getFarmId(), detail.getMaterialId());
            if (stocks.isEmpty())
                return Response.fail("stock.not.found");
            BigDecimal totalStockQuantity = new BigDecimal(0);
            for (DoctorWarehouseStock stock : stocks) {
                totalStockQuantity = totalStockQuantity.add(stock.getQuantity());
            }
            if (totalStockQuantity.compareTo(detail.getQuantity()) < 0)
                return Response.fail("stock.not.enough");


            DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
            purchaseCriteria.setFarmId(formulaDto.getFarmId());
            purchaseCriteria.setMaterialId(detail.getMaterialId());
            purchaseCriteria.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());//未出库完的
            List<DoctorWarehousePurchase> materialPurchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
            if (null == materialPurchases || materialPurchases.isEmpty())
                return Response.fail("purchase.not.found");
            materialPurchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));

            DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = getNeedPurchase(materialPurchases, detail.getQuantity());

            Map<Long, List<DoctorWarehousePurchase>> eachWarehousePurchase = purchaseHandleContext.getPurchaseQuantity().keySet().stream().collect(Collectors.groupingBy(DoctorWarehousePurchase::getWarehouseId));

            Map<Long, List<DoctorWarehouseStock>> eachWarehouseStock = stocks.stream().collect(Collectors.groupingBy(DoctorWarehouseStock::getWarehouseId));
            for (Long warehouseId : eachWarehousePurchase.keySet()) {

                List<DoctorWarehousePurchase> thisWarehousePurchase = eachWarehousePurchase.get(warehouseId);
                Map<DoctorWarehousePurchase, BigDecimal> thisWarehousePurchaseMap = new HashMap<>();
                BigDecimal thisWarehouseQuantity = new BigDecimal(0);
                BigDecimal thisWarehouseAmount = new BigDecimal(0);
                for (DoctorWarehousePurchase purchase : thisWarehousePurchase) {
                    thisWarehouseQuantity = thisWarehouseQuantity.add(purchaseHandleContext.getPurchaseQuantity().get(purchase));
                    thisWarehouseAmount = thisWarehouseAmount.add(purchaseHandleContext.getPurchaseQuantity().get(purchase).multiply(new BigDecimal(purchase.getUnitPrice())));
                    thisWarehousePurchaseMap.put(purchase, purchaseHandleContext.getPurchaseQuantity().get(purchase));
                }

                List<DoctorWarehouseStock> stock = eachWarehouseStock.get(warehouseId);
                if (stock.isEmpty())
                    throw new ServiceException("stock.not.found");
                stock.get(0).setQuantity(stock.get(0).getQuantity().subtract(thisWarehouseQuantity));
                DoctorWarehouseHandlerManager.PurchaseHandleContext thisWarehousePurchaseContext = new DoctorWarehouseHandlerManager.PurchaseHandleContext();
                thisWarehousePurchaseContext.setStock(stock.get(0));
                thisWarehousePurchaseContext.setPurchaseQuantity(thisWarehousePurchaseMap);
//                    long thisWarehouseAveragePrice = thisWarehouseAmount.divide(thisWarehouseQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();
                long thisWarehouseAveragePrice = doctorWarehousePurchaseManager.calculateUnitPrice(stock.get(0));
                doctorWarehouseHandlerManager.outStock(stock.get(0), thisWarehousePurchaseContext, DoctorWarehouseMaterialHandle.builder()
                        .farmId(stock.get(0).getFarmId())
                        .warehouseId(stock.get(0).getWarehouseId())
                        .warehouseType(stock.get(0).getWarehouseType())
                        .warehouseName(stock.get(0).getWarehouseName())
                        .materialId(stock.get(0).getSkuId())
                        .materialName(stock.get(0).getSkuName())
//                        .unit(stock.get(0).getUnit())
                        .unitPrice(thisWarehouseAveragePrice)
                        .type(WarehouseMaterialHandleType.FORMULA_OUT.getValue())
                        .quantity(thisWarehouseQuantity)
                        .operatorId(formulaDto.getOperatorId())
                        .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
                        .operatorName(formulaDto.getOperatorName())
                        .handleDate(formulaDto.getHandleDate().getTime())
                        .handleYear(formulaDto.getHandleDate().get(Calendar.YEAR))
                        .handleMonth(formulaDto.getHandleDate().get(Calendar.MONTH) + 1)
                        .remark(detail.getRemark())
                        .build());
                doctorWarehouseStockMonthlyManager.count(warehouseId,
                        stock.get(0).getSkuId(),
                        formulaDto.getHandleDate().get(Calendar.YEAR),
                        formulaDto.getHandleDate().get(Calendar.MONTH) + 1,
                        thisWarehouseQuantity,
                        thisWarehouseAveragePrice,
                        false);
                log.debug("仓库{}出库{}，物料{}", warehouseId, thisWarehouseQuantity, stock.get(0).getSkuId());
            }

            totalAmount += detail.getQuantity().multiply(new BigDecimal(purchaseHandleContext.getAveragePrice())).longValue();
            totalQuantity = totalQuantity.add(detail.getQuantity());
        }


        //生产出的饲料的仓库编号
        DoctorWareHouse wareHouse = doctorWareHouseDao.findById(formulaDto.getWarehouseId());
        if (null == wareHouse)
            throw new ServiceException("warehouse.not.found");

        DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(formulaDto.getFeedMaterialId());
        if (null == sku)
            throw new InvalidException("warehouse.sku.not.found", formulaDto.getFeedMaterialId());

        DoctorWarehouseStock stock = getStock(formulaDto.getWarehouseId(), formulaDto.getFeedMaterialId(), null);

        if (null == stock) {
            stock = new DoctorWarehouseStock();
            stock.setFarmId(formulaDto.getFarmId());
            stock.setWarehouseId(formulaDto.getWarehouseId());
            stock.setWarehouseName(wareHouse.getWareHouseName());
            stock.setWarehouseType(wareHouse.getType());
            stock.setSkuId(formulaDto.getFeedMaterialId());
            stock.setSkuName(formulaDto.getFeedMaterial().getName());
            stock.setQuantity(formulaDto.getFeedMaterialQuantity());
        } else
            stock.setQuantity(stock.getQuantity().add(formulaDto.getFeedMaterialQuantity()));

        long unitPrice = new BigDecimal(totalAmount).divide(totalQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();

        doctorWarehouseHandlerManager.inStock(stock, Collections.singletonList(DoctorWarehousePurchase.builder()
                        .farmId(stock.getFarmId())
                        .warehouseId(stock.getWarehouseId())
                        .warehouseType(stock.getWarehouseType())
                        .warehouseName(stock.getWarehouseName())
                        .materialId(stock.getSkuId())
                        .vendorName(DoctorWarehouseStockWriteService.DEFAULT_VENDOR_NAME)
                        .handleDate(formulaDto.getHandleDate().getTime())
                        .handleMonth(formulaDto.getHandleDate().get(Calendar.MONTH) + 1)
                        .handleYear(formulaDto.getHandleDate().get(Calendar.YEAR))
                        .quantity(formulaDto.getFeedMaterialQuantity())
                        .unitPrice(unitPrice)
                        .handleQuantity(new BigDecimal(0))
                        .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                        .build()),
                DoctorWarehouseMaterialHandle.builder()
                        .farmId(stock.getFarmId())
                        .warehouseId(stock.getWarehouseId())
                        .warehouseName(stock.getWarehouseName())
                        .warehouseType(stock.getWarehouseType())
                        .materialId(stock.getSkuId())
                        .materialName(stock.getSkuName())
                        .unitPrice(unitPrice)
                        .handleDate(formulaDto.getHandleDate().getTime())
                        .handleYear(formulaDto.getHandleDate().get(Calendar.YEAR))
                        .handleMonth(formulaDto.getHandleDate().get(Calendar.MONTH) + 1)
                        .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
                        .unit(sku.getUnit())
                        .type(WarehouseMaterialHandleType.FORMULA_IN.getValue())
                        .quantity(formulaDto.getFeedMaterialQuantity())
                        .operatorId(formulaDto.getOperatorId())
                        .operatorName(formulaDto.getOperatorName())
                        .build(), null, null);
        doctorWarehouseStockMonthlyManager.count(stock.getWarehouseId(),
                stock.getSkuId(),
                formulaDto.getHandleDate().get(Calendar.YEAR),
                formulaDto.getHandleDate().get(Calendar.MONTH) + 1,
                formulaDto.getFeedMaterialQuantity(),
                unitPrice,
                true);
        return Response.ok(true);
    }

//    @Override
//    public Response<Boolean> outAndIn(List<DoctorWarehouseStockHandleDto> inHandles, List<DoctorWarehouseStockHandleDto> outHandles, DoctorWarehouseStockHandler handle) {
//        try {
//            doctorWarehouseHandlerManager.inAndOutStock(inHandles, outHandles, handle);
//        } catch (Exception e) {
//            log.error("failed to out of stock,cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("doctor.warehouseV2.stock.out.fail");
//        }
//
//        return Response.ok(true);
//    }


    @Deprecated
    private StockContext validAndGetContext(Long farmID, Long warehouseID, List<? extends AbstractWarehouseStockDetail> details) {

        //查询基础数据，农场可添加物料
        Response<DoctorFarmBasic> farmBasicResponse = doctorFarmBasicReadService.findFarmBasicByFarmId(farmID);
        if (!farmBasicResponse.isSuccess())
            throw new ServiceException(farmBasicResponse.getError());
        DoctorFarmBasic farmBasic = farmBasicResponse.getResult();
        if (null == farmBasic)
            throw new InvalidException("farm.basic.not.found", farmID);

        List<Long> currentFarmSupportedMaterials = farmBasic.getMaterialIdList();

        DoctorWareHouse wareHouse = doctorWareHouseDao.findById(warehouseID);
        if (null == wareHouse)
            throw new InvalidException("warehouse.not.found", warehouseID);


        //先过滤一遍。
        details.forEach(detail -> {
            if (!currentFarmSupportedMaterials.contains(detail.getMaterialId()))
                throw new InvalidException("material.not.allow.in.this.warehouse", detail.getMaterialId(), wareHouse.getWareHouseName());
        });

        if (details.isEmpty())
            throw new ServiceException("stock.material.id.null");

        List materialIds = details.stream().map(AbstractWarehouseStockDetail::getMaterialId).collect(Collectors.toList());
        List<DoctorBasicMaterial> supportedMaterials = doctorBasicMaterialDao.findByIdsAndType(wareHouse.getType().longValue(), materialIds);
        if (null == supportedMaterials || supportedMaterials.isEmpty())
            throw new InvalidException("material.not.found", StringUtils.join(materialIds, ','));
//        if (supportedMaterials.isEmpty())
//            throw new ServiceException("material.not.allow.in.this.warehouse");

        Map<Long, String> supportedMaterialIds = new HashedMap(supportedMaterials.size());
        supportedMaterials.forEach(material -> {
            supportedMaterialIds.put(material.getId(), material.getName());
        });
        //再过滤一遍，加上type类型条件
        details.forEach(detail -> {
            if (!supportedMaterialIds.containsKey(detail.getMaterialId()))
                throw new InvalidException("material.not.allow.in.this.warehouse", detail.getMaterialId(), wareHouse.getWareHouseName());
        });

        StockContext context = new StockContext();
        context.setSupportedMaterials(supportedMaterialIds);
        context.setWareHouse(wareHouse);
        return context;
    }

    public StockContext getWarehouseAndSupportedBasicMaterial(Long farmId, Long warehouseId) {
        //查询基础数据，农场可添加物料
//        Response<DoctorFarmBasic> farmBasicResponse = doctorFarmBasicReadService.findFarmBasicByFarmId(farmId);
//        if (!farmBasicResponse.isSuccess())
//            throw new ServiceException(farmBasicResponse.getError());
//        DoctorFarmBasic farmBasic = farmBasicResponse.getResult();
//        if (null == farmBasic)
//            throw new InvalidException("farm.basic.not.found", farmId);

//        List<Long> currentFarmSupportedMaterials = farmBasic.getMaterialIdList();

        DoctorWareHouse wareHouse = doctorWareHouseDao.findById(warehouseId);
        if (null == wareHouse)
            throw new InvalidException("warehouse.not.found", warehouseId);

//        List<DoctorBasicMaterial> supportedMaterials = doctorBasicMaterialDao.findByIdsAndType(wareHouse.getType().longValue(), currentFarmSupportedMaterials);
//        if (null == supportedMaterials || supportedMaterials.isEmpty())
//            throw new InvalidException("basic.material.not.found", StringUtils.join(currentFarmSupportedMaterials, ','));
//        Map<Long, String> supportedMaterialIds = new HashedMap(supportedMaterials.size());
//        supportedMaterials.forEach(material -> {
//            supportedMaterialIds.put(material.getId(), material.getName());
//        });

        StockContext context = new StockContext();
//        context.setSupportedMaterials(supportedMaterialIds);
        context.setWareHouse(wareHouse);
        return context;
    }

//    private DoctorWarehouseStock getAvailableStock(DoctorWareHouse wareHouse, WarehouseStockInDto.WarehouseStockInDetailDto detail, String materialName) {
//
//        DoctorWarehouseStock stock = getStock(wareHouse.getId(), detail.getMaterialId(), detail.getVendorName());
//
//        if (null == stock) {
//            stock = new DoctorWarehouseStock();
//            stock.setFarmId(wareHouse.getFarmId());
//            stock.setWarehouseId(wareHouse.getId());
//            stock.setWarehouseName(wareHouse.getWareHouseName());
//            stock.setWarehouseType(wareHouse.getType());
//            stock.setSkuId(detail.getMaterialId());
//            stock.setSkuName(materialName);
////            if (StringUtils.isBlank(detail.getVendorName()))
////                stock.setVendorName(DEFAULT_VENDOR_NAME);
////            else
////                stock.setVendorName(detail.getVendorName());
////            stock.setUnit(detail.getUnit());
//            stock.setQuantity(detail.getQuantity());
//            return stock;
//        } else
//            return stock;
//    }

    private DoctorWarehouseStock getStock(Long warehouseID, Long materialID, String vendorName) {
        DoctorWarehouseStock criteria = new DoctorWarehouseStock();
        criteria.setWarehouseId(warehouseID);
        criteria.setSkuId(materialID);
//        if (StringUtils.isNotBlank(vendorName))
//            criteria.setVendorName(vendorName);
//        else criteria.setVendorName(DEFAULT_VENDOR_NAME);

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(criteria);
        if (null == stocks || stocks.isEmpty())
            return null;
        else return stocks.get(0);
    }

    private List<DoctorWarehouseStock> getStock(Long warehouseID, Long materialID) {
        DoctorWarehouseStock criteria = new DoctorWarehouseStock();
        criteria.setWarehouseId(warehouseID);
        criteria.setSkuId(materialID);

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(criteria);
        if (null == stocks)
            return Collections.emptyList();

        return stocks;
    }

    private List<DoctorWarehouseStock> getStockByFarm(Long FarmId, Long materialID) {
        DoctorWarehouseStock criteria = new DoctorWarehouseStock();
        criteria.setFarmId(FarmId);
        criteria.setSkuId(materialID);

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(criteria);
        if (null == stocks)
            return Collections.emptyList();

        return stocks;
    }

    private DoctorWarehousePurchase copyPurchase(DoctorWarehousePurchase source, Calendar handleDate, DoctorWareHouse targetWarehouse, BigDecimal quantity) {
        DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
        purchase.setHandleDate(handleDate.getTime());
        purchase.setHandleYear(handleDate.get(Calendar.YEAR));
        purchase.setHandleMonth(handleDate.get(Calendar.MONTH) + 1);

        purchase.setWarehouseId(targetWarehouse.getId());
        purchase.setWarehouseName(targetWarehouse.getWareHouseName());
        purchase.setWarehouseType(targetWarehouse.getType());
        purchase.setMaterialId(source.getMaterialId());
        purchase.setVendorName(source.getVendorName());
        purchase.setQuantity(quantity);
        purchase.setHandleQuantity(new BigDecimal(0));
        purchase.setUnitPrice(source.getUnitPrice());
        purchase.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
        purchase.setFarmId(targetWarehouse.getFarmId());
        return purchase;
    }

    private DoctorWarehouseMaterialHandle buildMaterialHandle(DoctorWarehouseStock stock, AbstractWarehouseStockDto
            stockDto, BigDecimal quantity, long price, int type) {
        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setFarmId(stock.getFarmId());
        materialHandle.setWarehouseId(stock.getWarehouseId());
        materialHandle.setWarehouseName(stock.getWarehouseName());
        materialHandle.setWarehouseType(stock.getWarehouseType());
        materialHandle.setMaterialId(stock.getSkuId());
        materialHandle.setMaterialName(stock.getSkuName());
        materialHandle.setUnitPrice(price);
        materialHandle.setType(type);
        materialHandle.setQuantity(quantity);
        materialHandle.setHandleDate(stockDto.getHandleDate().getTime());
        materialHandle.setOperatorId(stockDto.getOperatorId());
        materialHandle.setOperatorName(stockDto.getOperatorName());
        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
//        materialHandle.setUnit(stock.getUnit());
        return materialHandle;
    }

//    private DoctorWarehouseMaterialApply buildMaterialApply(DoctorWarehouseMaterialHandle handle) {
//        DoctorWarehouseMaterialApply materialApply = new DoctorWarehouseMaterialApply();
//        materialApply.setWarehouseId(handle.getWarehouseId());
//        materialApply.setFarmId(handle.getFarmId());
//        materialApply.setWarehouseName(handle.getWarehouseName());
//        materialApply.setWarehouseType(handle.getWarehouseType());
//        materialApply.setMaterialId(handle.getMaterialId());
//        materialApply.setMaterialName(handle.getMaterialName());
//
//        materialApply.setType(handle.getWarehouseType());
//        materialApply.setUnit(handle.getUnit());
//        materialApply.setQuantity(handle.getQuantity());
//        materialApply.setUnitPrice(handle.getUnitPrice());
//        materialApply.setApplyDate(handle.getHandleDate());
//        materialApply.setApplyYear(handle.getHandleYear());
//        materialApply.setApplyMonth(handle.getHandleMonth());
//        return materialApply;
//    }

//    private long handleOutAndCalcAveragePrice(BigDecimal totalNeedOutQuantity,
//                                              List<DoctorWarehousePurchase> purchases,
//                                              Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockAndPurchases,
//                                              List<DoctorWarehouseStock> stocks,
//                                              boolean isTransfer,
//                                              DoctorWareHouse targetWarehouse,
//                                              Calendar handleDate) {
//
//        Map<String, DoctorWarehouseStock> materialStocks = new HashMap<>();
//        for (DoctorWarehouseStock stock : stocks) {
//            String key = stock.getSkuName() + "|" + stock.getVendorName();
//            materialStocks.put(key, stock);
//        }
//
//        Map<DoctorWarehouseStock, BigDecimal> stockChangedQuantity = null;
//        if (isTransfer) {
//            stockChangedQuantity = new HashMap<>();
//        }
//
//        BigDecimal needPurchaseQuantity = new BigDecimal(totalNeedOutQuantity.toString());
//
//        BigDecimal totalHandleQuantity = new BigDecimal(0);
//        long totalHandleMoney = 0L;
//        for (DoctorWarehousePurchase purchase : purchases) {
//            if (needPurchaseQuantity.compareTo(new BigDecimal(0)) <= 0)
//                break;
//
//            BigDecimal availablePurchaseQuantity = purchase.getQuantity().subtract(purchase.getHandleQuantity());
//            BigDecimal actualCutDownQuantity = availablePurchaseQuantity;
//            if (needPurchaseQuantity.compareTo(availablePurchaseQuantity) <= 0) {
//                actualCutDownQuantity = needPurchaseQuantity;
//            }
//
//            purchase.setHandleQuantity(purchase.getHandleQuantity().add(actualCutDownQuantity));
//            if (purchase.getHandleQuantity().compareTo(purchase.getQuantity()) >= 0)
//                purchase.setHandleFinishFlag(0);
//
//            DoctorWarehouseStock stock = materialStocks.get(purchase.getMaterialId() + "|" + purchase.getVendorName());
//            //扣减库存
//            stock.setQuantity(stock.getQuantity().subtract(actualCutDownQuantity));
//            log.info("库存[{}]扣减{}{}", stock.getId(), actualCutDownQuantity, stock.getUnit());
//            if (!stockAndPurchases.containsKey(stock)) {
//                List<DoctorWarehousePurchase> stockPurchases = new ArrayList<>();
//                stockPurchases.add(purchase);
//                stockAndPurchases.put(stock, stockPurchases);
//            } else
//                stockAndPurchases.get(stock).add(purchase);
//
//            if (isTransfer) {
//                //如果是调拨，还需要根据转出构建对应的转入stock和purchase
//                if (!stockChangedQuantity.containsKey(stock)) {
//                    stockChangedQuantity.put(stock, actualCutDownQuantity);
//                } else {
//                    BigDecimal newQuantity = stockChangedQuantity.get(stock).add(actualCutDownQuantity);
//                    stockChangedQuantity.put(stock, newQuantity);
//                }
//            }
//
//            totalHandleMoney += actualCutDownQuantity.multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
//            totalHandleQuantity = totalHandleQuantity.add(actualCutDownQuantity);
//
//            needPurchaseQuantity = needPurchaseQuantity.subtract(actualCutDownQuantity);
//        }
//
//
//        if (isTransfer) {
//            for (DoctorWarehouseStock stock : stockAndPurchases.keySet()) {
//                //根据出库构造入库
//                DoctorWarehouseStock transferInStock = getStock(targetWarehouse.getId(), stock.getMaterialId(), stock.getVendorName());
//                if (null == transferInStock) {
//                    transferInStock = new DoctorWarehouseStock();
//                    transferInStock.setFarmId(stock.getFarmId());
//                    transferInStock.setWarehouseId(targetWarehouse.getId());
//                    transferInStock.setWarehouseName(targetWarehouse.getWareHouseName());
//                    transferInStock.setWarehouseType(targetWarehouse.getType());
//                    transferInStock.setMaterialId(stock.getMaterialId());
//                    transferInStock.setMaterialName(stock.getMaterialName());
//                    transferInStock.setVendorName(stock.getVendorName());
//                    transferInStock.setUnit(stock.getUnit());
//                    transferInStock.setQuantity(stockChangedQuantity.get(stock));
//                } else
//                    transferInStock.setQuantity(transferInStock.getQuantity().add(stockChangedQuantity.get(stock)));
//
//                List<DoctorWarehousePurchase> transferOutPurchase = stockAndPurchases.get(stock);
//                List<DoctorWarehousePurchase> transferInPurchase = new ArrayList<>(transferOutPurchase.size());
//                for (DoctorWarehousePurchase purchase : transferOutPurchase) {
//                    transferInPurchase.add(copyPurchase(purchase, handleDate, targetWarehouse, stockChangedQuantity.get(stock)));
//                }
//
//                stockAndPurchases.put(transferInStock, transferInPurchase);
//            }
//        }
//
//        //去除小数部分，四舍五入
//        return new BigDecimal(totalHandleMoney).divide(totalHandleQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();
//    }

    private DoctorWarehouseHandlerManager.PurchaseHandleContext getNeedPurchase(List<DoctorWarehousePurchase> purchases, BigDecimal totalQuantity) {

        DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = new DoctorWarehouseHandlerManager.PurchaseHandleContext();
        if (null == purchases || purchases.isEmpty())
            return purchaseHandleContext;

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

            purchaseHandleContext.getPurchaseQuantity().put(purchase, actualCutDownQuantity);

            totalHandleMoney += actualCutDownQuantity.multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
            totalHandleQuantity = totalHandleQuantity.add(actualCutDownQuantity);

            needPurchaseQuantity = needPurchaseQuantity.subtract(actualCutDownQuantity);
        }
        //去除小数部分，四舍五入
        purchaseHandleContext.setAveragePrice(new BigDecimal(totalHandleMoney).divide(totalHandleQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue());
        return purchaseHandleContext;
    }


    @Data
    public class StockContext {
        private DoctorWareHouse wareHouse;
        private Map<Long/*stockId*/, DoctorWarehouseStock> stockMap;
        private Map<Long, String> supportedMaterials;
    }

    @Data
    public class SkuGroup {
        private BigDecimal totalQuantity;
        private List<DoctorWarehouseStock> stocks;
        private List<AbstractWarehouseStockDetail> details;
    }
}