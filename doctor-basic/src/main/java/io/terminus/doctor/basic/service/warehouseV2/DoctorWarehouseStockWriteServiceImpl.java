package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.*;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.manager.DoctorWarehouseHandlerManager;
import io.terminus.doctor.basic.manager.DoctorWarehouseMaterialApplyManager;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.FeedFormula;
import io.terminus.doctor.basic.model.warehouseV2.*;
import io.terminus.doctor.basic.service.DoctorFarmBasicReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehousePurchaseWriteService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteService;
import io.terminus.doctor.common.utils.DateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.omg.IOP.TAG_ORB_TYPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.plugin2.util.NativeLibLoader;

import javax.xml.soap.Detail;
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
    private DoctorMaterialCodeDao doctorMaterialCodeDao;
    @Autowired
    private DoctorMaterialVendorDao doctorMaterialVendorDao;


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

//            DoctorWarehouseStock criteria = new DoctorWarehouseStock();
//            criteria.setWarehouseId(stock.getWarehouseId());
//            criteria.setMaterialId(stock.getMaterialId());
//            List<DoctorWarehouseStock> allVendorStocks = doctorWarehouseStockDao.list(criteria);
//
//            for (DoctorWarehouseStock s : allVendorStocks) {
//                if (s.getQuantity().compareTo(new BigDecimal(0)) > 0)
//                    return Response.fail("stock.not.empty");
//            }
//
//            for (DoctorWarehouseStock s : allVendorStocks) {
//                Boolean result = doctorWarehouseStockDao.delete(s.getId());
//                if (!result)
//                    return Response.fail("doctor.warehouseV2.stock.delete.fail");
//            }
            if (stock.getQuantity().compareTo(new BigDecimal(0)) > 0)
                return Response.fail("stock.not.empty");

            return Response.ok(doctorWarehouseStockDao.delete(id));
        } catch (Exception e) {
            log.error("failed to delete doctor warehouse stock by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.delete.fail");
        }
    }

    @Override
    public Response<Boolean> in(WarehouseStockInDto stockIn) {
        try {
            StockContext context = validAndGetContext(stockIn.getFarmId(), stockIn.getWarehouseId(), stockIn.getDetails());


            Calendar.getInstance().get(Calendar.MILLISECOND);

            stockIn.getDetails().forEach(detail -> {
                DoctorWarehouseStock stock = getAvailableStock(context.getWareHouse(), detail, context.getSupportedMaterials().get(detail.getMaterialId()));

                //如果已有库存，累加数量
                if (stock.getId() != null) {
                    stock.setQuantity(stock.getQuantity().add(detail.getQuantity()));
                }


                DoctorMaterialCode materialCode = null;
                if ((StringUtils.isNotBlank(detail.getSpecification()) || StringUtils.isNotBlank(detail.getMaterialCode())) &&
                        doctorMaterialCodeDao.list(DoctorMaterialCode.builder()
                                .warehouseId(stock.getWarehouseId())
                                .materialId(stock.getMaterialId())
                                .vendorName(detail.getVendorName())
                                .build()).isEmpty()) {
                    materialCode = new DoctorMaterialCode();
                    materialCode.setWarehouseId(stock.getWarehouseId());
                    materialCode.setMaterialId(stock.getMaterialId());
                    materialCode.setVendorName(detail.getVendorName());
                    materialCode.setSpecification(detail.getSpecification());
                    materialCode.setCode(detail.getMaterialCode());
                }
                DoctorMaterialVendor materialVendor = null;
                if (StringUtils.isNotBlank(detail.getVendorName()) &&
                        doctorMaterialVendorDao.list(DoctorMaterialVendor.builder()
                                .warehouseId(stock.getWarehouseId())
                                .materialId(stock.getMaterialId())
                                .build()).isEmpty()) {
                    materialVendor = new DoctorMaterialVendor();
                    materialVendor.setWarehouseId(stock.getWarehouseId());
                    materialVendor.setMaterialId(stock.getMaterialId());
                    materialVendor.setVendorName(detail.getVendorName());
                }


                Calendar c = Calendar.getInstance();
                c.setTime(stockIn.getHandleDate());

                //记录物料采购入库的单价
                DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
                purchase.setFarmId(stockIn.getFarmId());
                purchase.setWarehouseId(stockIn.getWarehouseId());
                purchase.setWarehouseName(context.getWareHouse().getWareHouseName());
                purchase.setWarehouseType(context.getWareHouse().getType());
                purchase.setMaterialId(detail.getMaterialId());
                if (StringUtils.isBlank(detail.getVendorName()))
                    purchase.setVendorName(DEFAULT_VENDOR_NAME);
                else
                    purchase.setVendorName(detail.getVendorName());
                purchase.setQuantity(detail.getQuantity());
                purchase.setHandleQuantity(new BigDecimal(0));
                purchase.setUnitPrice(detail.getUnitPrice());
                purchase.setHandleDate(stockIn.getHandleDate());
                purchase.setHandleYear(c.get(Calendar.YEAR));
                purchase.setHandleMonth(c.get(Calendar.MONTH) + 1);
                purchase.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());

                DoctorWarehouseMaterialHandle materialHandle =
                        buildMaterialHandle(stock, stockIn, detail.getQuantity(), detail.getUnitPrice(), WarehouseMaterialHandleType.IN.getValue());
                materialHandle.setVendorName(purchase.getVendorName());
                materialHandle.setUnit(stock.getUnit());
                materialHandle.setHandleYear(c.get(Calendar.YEAR));
                materialHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);

                doctorWarehouseHandlerManager.inStock(stock, Collections.singletonList(purchase), materialHandle, materialCode, materialVendor);
            });

            return Response.ok(true);
        } catch (ServiceException e) {
            log.error("failed to stock in by id:{}, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("failed to stock in by id:{}, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.in.fail");
        }
    }

    @Override
    public Response<Boolean> inventory(WarehouseStockInventoryDto stockInventory) {

        try {
            StockContext context = validAndGetContext(stockInventory.getFarmId(), stockInventory.getWarehouseId(), stockInventory.getDetails());

            for (WarehouseStockInventoryDto.WarehouseStockInventoryDetail detail : stockInventory.getDetails()) {

                //找到对应库存
                DoctorWarehouseStock stock = getStock(stockInventory.getWarehouseId(), detail.getMaterialId(), null);
                if (null == stock)
                    return Response.fail("stock.not.found");

                int compareResult = stock.getQuantity().compareTo(detail.getQuantity());

                if (compareResult == 0) {
                    log.info("盘点库存量与原库存量一致，warehouse[{}],material[{}]", stockInventory.getWarehouseId(), detail.getMaterialId());
                    continue;
                }
                Calendar c = Calendar.getInstance();
                c.setTime(stockInventory.getHandleDate());

                DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
                purchaseCriteria.setWarehouseId(stock.getWarehouseId());
                purchaseCriteria.setMaterialId(stock.getMaterialId());
                purchaseCriteria.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
                List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
                if (null == purchases || purchases.isEmpty())
                    throw new ServiceException("purchase.not.found");
                //根据处理日期倒序，找出最近一次的入库记录
                purchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));
                DoctorWarehousePurchase lastPurchase = purchases.get(purchases.size() - 1);

                DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
                materialHandle.setFarmId(stockInventory.getFarmId());
                materialHandle.setWarehouseId(stockInventory.getWarehouseId());
                materialHandle.setWarehouseName(context.getWareHouse().getWareHouseName());
                materialHandle.setWarehouseType(context.getWareHouse().getType());
                materialHandle.setMaterialId(detail.getMaterialId());
                materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
                materialHandle.setMaterialName(context.getSupportedMaterials().get(detail.getMaterialId()));

                materialHandle.setHandleDate(stockInventory.getHandleDate());
                materialHandle.setHandleYear(c.get(Calendar.YEAR));
                materialHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);
                materialHandle.setUnit(stock.getUnit());
                materialHandle.setOperatorId(stockInventory.getOperatorId());
                materialHandle.setOperatorName(stockInventory.getOperatorName());


                BigDecimal changedQuantity;
                if (compareResult > 0) {
                    //盘亏
                    changedQuantity = stock.getQuantity().subtract(detail.getQuantity());
//                    long averagePrice = handleOutAndCalcAveragePrice(changedQuantity, purchases, stockAndPurchases, stocks, false, null, null);
                    DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = getNeedPurchase(purchases, changedQuantity);
                    materialHandle.setUnitPrice(purchaseHandleContext.getAveragePrice());
                    materialHandle.setType(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
                    materialHandle.setQuantity(changedQuantity);
                    stock.setQuantity(detail.getQuantity());
                    doctorWarehouseHandlerManager.outStock(stock, purchaseHandleContext, materialHandle);
                } else {
                    //盘盈
                    changedQuantity = detail.getQuantity().subtract(stock.getQuantity());
                    DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
                    purchase.setFarmId(stock.getFarmId());
                    purchase.setHandleDate(stockInventory.getHandleDate());
                    purchase.setWarehouseId(stock.getWarehouseId());
                    purchase.setWarehouseName(stock.getWarehouseName());
                    purchase.setWarehouseType(stock.getWarehouseType());
                    purchase.setMaterialId(stock.getMaterialId());
                    purchase.setVendorName(lastPurchase.getVendorName());
                    purchase.setQuantity(changedQuantity);
                    purchase.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
                    purchase.setHandleQuantity(new BigDecimal(0));
                    purchase.setUnitPrice(lastPurchase.getUnitPrice());
                    purchase.setHandleMonth(c.get(Calendar.MONTH) + 1);
                    purchase.setHandleYear(c.get(Calendar.YEAR));

                    materialHandle.setQuantity(changedQuantity);
                    materialHandle.setUnitPrice(purchase.getUnitPrice());
                    materialHandle.setVendorName(purchase.getVendorName());
                    materialHandle.setType(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
                    stock.setQuantity(detail.getQuantity());

                    doctorWarehouseHandlerManager.inStock(stock, Collections.singletonList(purchase), materialHandle, null,null);
                }


//                handleContext.setMaterialHandle(Collections.singletonList(materialHandle));
//                handleContext.setStockAndPurchases(stockAndPurchases);
//                handleContexts.add(handleContext);
            }
//            doctorWarehouseHandlerManager.handle(handleContexts);
            return Response.ok(true);
        } catch (ServiceException e) {
            log.error("failed to stock in by id:{}, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("failed to stock in by id:{}, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.delete.fail");
        }

    }

    @Override
    public Response<Boolean> transfer(WarehouseStockTransferDto stockTransfer) {

        try {
            StockContext context = validAndGetContext(stockTransfer.getFarmId(), stockTransfer.getWarehouseId(), stockTransfer.getDetails());

            List<DoctorWarehouseHandlerManager.StockHandleContext> handleContexts = new ArrayList<>(stockTransfer.getDetails().size());
            for (WarehouseStockTransferDto.WarehouseStockTransferDetail detail : stockTransfer.getDetails()) {

                if (detail.getTransferInWarehouseId() == stockTransfer.getWarehouseId())
                    return Response.fail("transfer.out.warehouse.equals.transfer.in.warehouse");

                DoctorWareHouse targetWareHouse = doctorWareHouseDao.findById(detail.getTransferInWarehouseId());
                if (null == targetWareHouse)
                    return Response.fail("warehouse.not.found");

                if (context.getWareHouse().getType().intValue() != targetWareHouse.getType().intValue())
                    return Response.fail("transfer.warehouse.type.not.equals");

                //找到对应库存
                DoctorWarehouseStock stock = getStock(stockTransfer.getWarehouseId(), detail.getMaterialId(), null);
                if (null == stock)
                    return Response.fail("stock.not.found");

                if (stock.getQuantity().compareTo(detail.getQuantity()) < 0)
                    return Response.fail("stock.not.enough");


                DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
                purchaseCriteria.setWarehouseId(stockTransfer.getWarehouseId());
                purchaseCriteria.setMaterialId(detail.getMaterialId());
                purchaseCriteria.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
                List<DoctorWarehousePurchase> transferOutPurchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
                if (null == transferOutPurchases || transferOutPurchases.isEmpty())
                    throw new ServiceException("purchase.not.found");
                transferOutPurchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));

//                DoctorWarehouseHandlerManager.StockHandleContext handleContext = new DoctorWarehouseHandlerManager.StockHandleContext();
//                Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockAndPurchases = new HashMap<>();
//                handleContext.setStockAndPurchases(stockAndPurchases);

                Calendar c = Calendar.getInstance();
                c.setTime(stockTransfer.getHandleDate());

//                long averagePrice = handleOutAndCalcAveragePrice(detail.getQuantity(), transferOutPurchases, stockAndPurchases, stocks, true, targetWareHouse, c);
                DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = getNeedPurchase(transferOutPurchases, detail.getQuantity());

                //调出
                DoctorWarehouseMaterialHandle outHandle = buildMaterialHandle(stock, stockTransfer, detail.getQuantity(), purchaseHandleContext.getAveragePrice(), WarehouseMaterialHandleType.TRANSFER_OUT.getValue());
                outHandle.setHandleYear(c.get(Calendar.YEAR));
                outHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);
                stock.setQuantity(stock.getQuantity().subtract(detail.getQuantity()));
                doctorWarehouseHandlerManager.outStock(stock, purchaseHandleContext, outHandle);
//                handleContext.addMaterialHandle(outHandle);

                DoctorWarehouseStock transferInStock = getStock(targetWareHouse.getId(), stock.getMaterialId(), null);
                if (null == transferInStock) {
                    transferInStock = new DoctorWarehouseStock();
                    transferInStock.setFarmId(stock.getFarmId());
                    transferInStock.setWarehouseId(targetWareHouse.getId());
                    transferInStock.setWarehouseName(targetWareHouse.getWareHouseName());
                    transferInStock.setWarehouseType(targetWareHouse.getType());

                    transferInStock.setMaterialId(stock.getMaterialId());
                    transferInStock.setMaterialName(stock.getMaterialName());
                    transferInStock.setUnit(stock.getUnit());

                    transferInStock.setQuantity(detail.getQuantity());
                } else
                    transferInStock.setQuantity(transferInStock.getQuantity().add(detail.getQuantity()));
                //构造调入MaterialHandle记录
                DoctorWarehouseMaterialHandle inHandle = buildMaterialHandle(transferInStock, stockTransfer, detail.getQuantity(), purchaseHandleContext.getAveragePrice(), WarehouseMaterialHandleType.TRANSFER_IN.getValue());
                inHandle.setHandleYear(c.get(Calendar.YEAR));
                inHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);
                inHandle.setOtherTrasnferHandleId(outHandle.getId());
                List<DoctorWarehousePurchase> transferInPurchase = new ArrayList<>();
                for (DoctorWarehousePurchase purchase : purchaseHandleContext.getPurchaseQuantity().keySet()) {
                    transferInPurchase.add(copyPurchase(purchase, c, targetWareHouse, purchaseHandleContext.getPurchaseQuantity().get(purchase)));
                }
                doctorWarehouseHandlerManager.inStock(transferInStock, transferInPurchase, inHandle, null,null);
                outHandle.setOtherTrasnferHandleId(inHandle.getId());
                doctorWarehouseMaterialHandleDao.update(outHandle);
//                handleContext.addMaterialHandle(inHandle);

//                handleContexts.add(handleContext);
            }
//            doctorWarehouseHandlerManager.handle(handleContexts);
            return Response.ok(true);
        } catch (ServiceException e) {
            log.error("failed to stock in by id:{}, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("failed to stock in by id:{}, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.delete.fail");
        }
    }

    @Override
    @Transactional
    public Response<Boolean> out(WarehouseStockOutDto stockOut) {

        try {
            StockContext context = validAndGetContext(stockOut.getFarmId(), stockOut.getWarehouseId(), stockOut.getDetails());
            List<DoctorWarehouseHandlerManager.StockHandleContext> handleContexts = new ArrayList<>();
            for (WarehouseStockOutDto.WarehouseStockOutDetail detail : stockOut.getDetails()) {

                DoctorWarehouseStock stock = getStock(stockOut.getWarehouseId(), detail.getMaterialId(), null);
                if (null == stock)
                    return Response.fail("stock.not.found");

                if (stock.getQuantity().compareTo(detail.getQuantity()) < 0)
                    return Response.fail("stock.not.enough");

                stock.setQuantity(stock.getQuantity().subtract(detail.getQuantity()));

                DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
                purchaseCriteria.setWarehouseId(stockOut.getWarehouseId());
                purchaseCriteria.setMaterialId(detail.getMaterialId());
                purchaseCriteria.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());//未出库完的
                List<DoctorWarehousePurchase> materialPurchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
                if (null == materialPurchases || materialPurchases.isEmpty())
                    return Response.fail("purchase.not.found");
                materialPurchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));

                DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = getNeedPurchase(materialPurchases, detail.getQuantity());

                Calendar c = Calendar.getInstance();
                c.setTime(stockOut.getHandleDate());
                DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(stock, stockOut, detail.getQuantity(), purchaseHandleContext.getAveragePrice(), WarehouseMaterialHandleType.OUT.getValue());

                materialHandle.setHandleYear(c.get(Calendar.YEAR));
                materialHandle.setHandleMonth(c.get(Calendar.MONTH) + 1);
                doctorWarehouseHandlerManager.outStock(stock, purchaseHandleContext, materialHandle);

                DoctorWarehouseMaterialApply materialApply = buildMaterialApply(materialHandle);
                materialApply.setMaterialHandleId(materialHandle.getId());
                materialApply.setPigBarnId(detail.getApplyPigBarnId());
                materialApply.setPigBarnName(detail.getApplyPigBarnName());
                materialApply.setPigGroupId(detail.getApplyPigGroupId());
                materialApply.setPigGroupName(detail.getApplyPigGroupName());
                materialApply.setApplyStaffName(detail.getApplyStaffName());
                doctorWarehouseMaterialApplyManager.create(materialApply);

            }
//            doctorWarehouseHandlerManager.handle(handleContexts);
            return Response.ok(true);
        } catch (ServiceException e) {
            log.error("failed to stock out, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("failed to stock out, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.out.fail");
        }
    }

    @Override
    public Response<Boolean> formula(WarehouseFormulaDto formulaDto) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(formulaDto.getHandleDate());


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
                    long thisWarehouseAveragePrice = thisWarehouseAmount.divide(thisWarehouseQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();
                    doctorWarehouseHandlerManager.outStock(stock.get(0), thisWarehousePurchaseContext, DoctorWarehouseMaterialHandle.builder()
                            .farmId(stock.get(0).getFarmId())
                            .warehouseId(stock.get(0).getWarehouseId())
                            .warehouseType(stock.get(0).getWarehouseType())
                            .warehouseName(stock.get(0).getWarehouseName())
                            .materialId(stock.get(0).getMaterialId())
                            .materialName(stock.get(0).getMaterialName())
                            .unit(stock.get(0).getUnit())
                            .unitPrice(thisWarehouseAveragePrice)
                            .type(WarehouseMaterialHandleType.FORMULA_OUT.getValue())
                            .quantity(thisWarehouseQuantity)
                            .operatorId(formulaDto.getOperatorId())
                            .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
                            .operatorName(formulaDto.getOperatorName())
                            .handleDate(formulaDto.getHandleDate())
                            .handleYear(c.get(Calendar.YEAR))
                            .handleMonth(c.get(Calendar.MONTH) + 1)
                            .build());
                    log.debug("仓库{}出库{}，物料{}", warehouseId, thisWarehouseQuantity, stock.get(0).getMaterialId());
                }

//                for (DoctorWarehouseStock stock : stockWithPurchases.keySet()) {
//                    handleContext.addMaterialHandle(DoctorWarehouseMaterialHandle.builder()
//                            .farmId(formulaDto.getFarmId())
//                            .warehouseId(stock.getWarehouseId())
//                            .warehouseName(stock.getWarehouseName())
//                            .warehouseType(stock.getWarehouseType())
//                            .materialId(detail.getMaterialId())
//                            .materialName(stock.getMaterialName())
//                            .unitPrice(averagePrice)
//                            .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
//                            .handleDate(formulaDto.getHandleDate())
//                            .handleYear(c.get(Calendar.YEAR))
//                            .unit(stocks.get(0).getUnit())
//                            .handleMonth(c.get(Calendar.MONTH) + 1)
//                            .type(WarehouseMaterialHandleType.FORMULA_OUT.getValue())
//                            .quantity(detail.getQuantity())
//                            .operatorId(formulaDto.getOperatorId())
//                            .operatorName(formulaDto.getOperatorName())
//                            .build());
//                }

//                handleContexts.add(handleContext);

                totalAmount += detail.getQuantity().multiply(new BigDecimal(purchaseHandleContext.getAveragePrice())).longValue();
                totalQuantity = totalQuantity.add(detail.getQuantity());
            }


//            DoctorWarehouseHandlerManager.StockHandleContext handleContext = new DoctorWarehouseHandlerManager.StockHandleContext();
//            Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockWithPurchase = new HashMap<>();

            //生产出的饲料的仓库编号
            DoctorWareHouse wareHouse = doctorWareHouseDao.findById(formulaDto.getWarehouseId());
            if (null == wareHouse)
                throw new ServiceException("warehouse.not.found");
            DoctorWarehouseStock stock = getStock(formulaDto.getWarehouseId(), formulaDto.getFeedMaterialId(), null);


            if (null == stock) {
                stock = new DoctorWarehouseStock();
                stock.setFarmId(formulaDto.getFarmId());
                stock.setWarehouseId(formulaDto.getWarehouseId());
                stock.setWarehouseName(wareHouse.getWareHouseName());
                stock.setWarehouseType(wareHouse.getType());
                stock.setMaterialId(formulaDto.getFeedMaterialId());
                stock.setMaterialName(formulaDto.getFeedMaterial().getName());
                stock.setUnit(formulaDto.getFeedUnit());
                stock.setQuantity(formulaDto.getFeedMaterialQuantity());
            } else
                stock.setQuantity(stock.getQuantity().add(formulaDto.getFeedMaterialQuantity()));

            long unitPrice = new BigDecimal(totalAmount).divide(totalQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();

            doctorWarehouseHandlerManager.inStock(stock, Collections.singletonList(DoctorWarehousePurchase.builder()
                    .farmId(stock.getFarmId())
                    .warehouseId(stock.getWarehouseId())
                    .warehouseType(stock.getWarehouseType())
                    .warehouseName(stock.getWarehouseName())
                    .materialId(stock.getMaterialId())
                    .vendorName(DoctorWarehouseStockWriteService.DEFAULT_VENDOR_NAME)
                    .handleDate(formulaDto.getHandleDate())
                    .handleMonth(c.get(Calendar.MONTH) + 1)
                    .handleYear(c.get(Calendar.YEAR))
                    .quantity(formulaDto.getFeedMaterialQuantity())
                    .unitPrice(unitPrice)
                    .handleQuantity(new BigDecimal(0))
                    .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
                    .build()), DoctorWarehouseMaterialHandle.builder()
                    .farmId(stock.getFarmId())
                    .warehouseId(stock.getWarehouseId())
                    .warehouseName(stock.getWarehouseName())
                    .warehouseType(stock.getWarehouseType())
                    .materialId(stock.getMaterialId())
                    .materialName(stock.getMaterialName())
                    .unitPrice(unitPrice)
                    .handleDate(formulaDto.getHandleDate())
                    .handleYear(c.get(Calendar.YEAR))
                    .handleMonth(c.get(Calendar.MONTH) + 1)
                    .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
                    .unit(stock.getUnit())
                    .type(WarehouseMaterialHandleType.FORMULA_IN.getValue())
                    .quantity(formulaDto.getFeedMaterialQuantity())
                    .operatorId(formulaDto.getOperatorId())
                    .operatorName(formulaDto.getOperatorName())
                    .build(), null,null);
//            handleContext.addMaterialHandle(DoctorWarehouseMaterialHandle.builder()
//                    .farmId(formulaDto.getFarmId())
//                    .warehouseId(wareHouse.getId())
//                    .warehouseName(wareHouse.getWareHouseName())
//                    .warehouseType(wareHouse.getType())
//                    .materialId(formulaDto.getFeedMaterialId())
//                    .materialName(formulaDto.getFeedMaterial().getName())
//                    .unitPrice(unitPrice)
//                    .handleDate(formulaDto.getHandleDate())
//                    .handleYear(c.get(Calendar.YEAR))
//                    .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
//                    .handleMonth(c.get(Calendar.MONTH) + 1)
//                    .unit(stock.getUnit())
//                    .type(WarehouseMaterialHandleType.FORMULA_IN.getValue())
//                    .quantity(formulaDto.getFeedMaterialQuantity())
//                    .operatorId(formulaDto.getOperatorId())
//                    .operatorName(formulaDto.getOperatorName())
//                    .build());

//            stockWithPurchase.put(stock, Collections.singletonList(DoctorWarehousePurchase.builder()
//                    .farmId(formulaDto.getFarmId())
//                    .warehouseId(formulaDto.getWarehouseId())
//                    .warehouseName(wareHouse.getWareHouseName())
//                    .warehouseType(wareHouse.getType())
//                    .vendorName(DoctorWarehouseStockWriteService.DEFAULT_VENDOR_NAME)
//                    .handleDate(formulaDto.getHandleDate())
//                    .handleYear(c.get(Calendar.YEAR))
//                    .handleMonth(c.get(Calendar.MONTH) + 1)
//                    .handleQuantity(new BigDecimal(0))
//                    .handleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue())
//                    .quantity(formulaDto.getFeedMaterialQuantity())
//                    .unitPrice(unitPrice)
//                    .build()));
//            handleContext.setStockAndPurchases(stockWithPurchase);
//            handleContexts.add(handleContext);

//            doctorWarehouseHandlerManager.handle(handleContexts);
            return Response.ok(true);
        } catch (ServiceException e) {
            log.error("failed to produce formula, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("failed to produce formula, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.produce.formula.fail");
        }
    }


    private void innerOut(DoctorWarehouseStock stock, List<DoctorWarehousePurchase> purchases) {

    }

    @Override
    public Response<Boolean> outAndIn(List<DoctorWarehouseStockHandleDto> inHandles, List<DoctorWarehouseStockHandleDto> outHandles, DoctorWarehouseStockHandler handle) {
        try {
            doctorWarehouseHandlerManager.inAndOutStock(inHandles, outHandles, handle);
        } catch (Exception e) {
            log.error("failed to out of stock,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.out.fail");
        }

        return Response.ok(true);
    }


    private StockContext validAndGetContext(Long farmID, Long warehouseID, List<? extends AbstractWarehouseStockDetail> details) {

        //查询基础数据，农场可添加物料
        Response<DoctorFarmBasic> farmBasicResponse = doctorFarmBasicReadService.findFarmBasicByFarmId(farmID);
        if (!farmBasicResponse.isSuccess())
            throw new ServiceException(farmBasicResponse.getError());
        DoctorFarmBasic farmBasic = farmBasicResponse.getResult();
        if (null == farmBasic)
            throw new ServiceException("farm.basic.not.found");

        List<Long> currentFarmSupportedMaterials = farmBasic.getMaterialIdList();

        DoctorWareHouse wareHouse = doctorWareHouseDao.findById(warehouseID);
        if (null == wareHouse)
            throw new ServiceException("warehouse.not.found");

        //先过滤一遍。
        details.forEach(detail -> {
            if (!currentFarmSupportedMaterials.contains(detail.getMaterialId()))
                throw new ServiceException("material.not.allow.in.this.warehouse");
        });

        if (details.isEmpty())
            throw new ServiceException("stock.material.id.null");

        List<DoctorBasicMaterial> supportedMaterials = doctorBasicMaterialDao.findByIdsAndType(wareHouse.getType().longValue(), details.stream().map(AbstractWarehouseStockDetail::getMaterialId).collect(Collectors.toList()));
        if (null == supportedMaterials)
            throw new ServiceException("material.not.found");
        if (supportedMaterials.isEmpty())
            throw new ServiceException("material.not.allow.in.this.warehouse");

        Map<Long, String> supportedMaterialIds = new HashedMap(supportedMaterials.size());
        supportedMaterials.forEach(material -> {
            supportedMaterialIds.put(material.getId(), material.getName());
        });
        //再过滤一遍，加上type类型条件
        details.forEach(detail -> {
            if (!supportedMaterialIds.containsKey(detail.getMaterialId()))
                throw new ServiceException("material.not.allow.in.this.warehouse");
        });

        StockContext context = new StockContext();
        context.setSupportedMaterials(supportedMaterialIds);
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
//            if (StringUtils.isBlank(detail.getVendorName()))
//                stock.setVendorName(DEFAULT_VENDOR_NAME);
//            else
//                stock.setVendorName(detail.getVendorName());
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
        criteria.setMaterialId(materialID);

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(criteria);
        if (null == stocks)
            return Collections.emptyList();

        return stocks;
    }

    private List<DoctorWarehouseStock> getStockByFarm(Long FarmId, Long materialID) {
        DoctorWarehouseStock criteria = new DoctorWarehouseStock();
        criteria.setFarmId(FarmId);
        criteria.setMaterialId(materialID);

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
        purchase.setFarmId(source.getFarmId());
        return purchase;
    }

    private DoctorWarehouseMaterialHandle buildMaterialHandle(DoctorWarehouseStock stock, AbstractWarehouseStockDto
            stockDto, BigDecimal quantity, long price, int type) {
        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setFarmId(stock.getFarmId());
        materialHandle.setWarehouseId(stock.getWarehouseId());
        materialHandle.setWarehouseName(stock.getWarehouseName());
        materialHandle.setWarehouseType(stock.getWarehouseType());
        materialHandle.setMaterialId(stock.getMaterialId());
        materialHandle.setMaterialName(stock.getMaterialName());
        materialHandle.setUnitPrice(price);
        materialHandle.setType(type);
        materialHandle.setQuantity(quantity);
        materialHandle.setHandleDate(stockDto.getHandleDate());
        materialHandle.setOperatorId(stockDto.getOperatorId());
        materialHandle.setOperatorName(stockDto.getOperatorName());
        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
        materialHandle.setUnit(stock.getUnit());
        return materialHandle;
    }

    private DoctorWarehouseMaterialApply buildMaterialApply(DoctorWarehouseMaterialHandle handle) {
        DoctorWarehouseMaterialApply materialApply = new DoctorWarehouseMaterialApply();
        materialApply.setWarehouseId(handle.getWarehouseId());
        materialApply.setFarmId(handle.getFarmId());
        materialApply.setWarehouseName(handle.getWarehouseName());
        materialApply.setWarehouseType(handle.getWarehouseType());
        materialApply.setMaterialId(handle.getMaterialId());
        materialApply.setMaterialName(handle.getMaterialName());

        materialApply.setType(handle.getWarehouseType());
        materialApply.setUnit(handle.getUnit());
        materialApply.setQuantity(handle.getQuantity());
        materialApply.setUnitPrice(handle.getUnitPrice());
        materialApply.setApplyDate(handle.getHandleDate());
        materialApply.setApplyYear(handle.getHandleYear());
        materialApply.setApplyMonth(handle.getHandleMonth());
        return materialApply;
    }

    private long handleOutAndCalcAveragePrice(BigDecimal totalNeedOutQuantity,
                                              List<DoctorWarehousePurchase> purchases,
                                              Map<DoctorWarehouseStock, List<DoctorWarehousePurchase>> stockAndPurchases,
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
            if (!stockAndPurchases.containsKey(stock)) {
                List<DoctorWarehousePurchase> stockPurchases = new ArrayList<>();
                stockPurchases.add(purchase);
                stockAndPurchases.put(stock, stockPurchases);
            } else
                stockAndPurchases.get(stock).add(purchase);

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
            for (DoctorWarehouseStock stock : stockAndPurchases.keySet()) {
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

                List<DoctorWarehousePurchase> transferOutPurchase = stockAndPurchases.get(stock);
                List<DoctorWarehousePurchase> transferInPurchase = new ArrayList<>(transferOutPurchase.size());
                for (DoctorWarehousePurchase purchase : transferOutPurchase) {
                    transferInPurchase.add(copyPurchase(purchase, handleDate, targetWarehouse, stockChangedQuantity.get(stock)));
                }

                stockAndPurchases.put(transferInStock, transferInPurchase);
            }
        }

        //去除小数部分，四舍五入
        return new BigDecimal(totalHandleMoney).divide(totalHandleQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();
    }

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
        private Map<Long, String> supportedMaterials;
    }
}