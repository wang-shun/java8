package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.warehouseV2.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.enums.WarehouseSkuStatus;
import io.terminus.doctor.basic.manager.*;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.*;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorFarmBasicReadService;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.print.attribute.SetOfIntegerSyntax;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.Lock;
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
    private DoctorFarmBasicReadService doctorFarmBasicReadService;
    @Autowired
    private WarehouseInStockService warehouseInStockService;
    @Autowired
    private WarehouseOutStockService warehouseOutStockService;
    @Autowired
    private WarehouseRefundStockService warehouseRefundStockService;
    @Autowired
    private WarehouseInventoryStockService warehouseInventoryStockService;
    @Autowired
    private WarehouseTransferStockService warehouseTransferStockService;
    @Autowired
    private WarehouseFormulaStockService warehouseFormulaStockService;

    @Autowired
    private DoctorWarehouseHandlerManager doctorWarehouseHandlerManager;
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
    private DoctorWarehouseMaterialApplyManager doctorWarehouseMaterialApplyManager;
    @Autowired
    private WarehouseInManager warehouseInManager;

    @Autowired
    private DoctorBasicDao doctorBasicDao;
    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;
    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;
    @Autowired
    private DoctorBasicMaterialDao doctorBasicMaterialDao;
    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;
    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;
    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;
    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;

    @Autowired
    private LockRegistry lockRegistry;

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

            if (null == stock)
                return Response.fail("stock.not.found");

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
    public Response<Long> in(WarehouseStockInDto stockIn) {

        return warehouseInStockService.handle(stockIn);

//        List<Lock> locks = lockedIfNecessary(stockIn);
//
//        DoctorWareHouse wareHouse = doctorWareHouseDao.findById(stockIn.getWarehouseId());
//        if (null == wareHouse)
//            throw new ServiceException("warehouse.not.found");
//
//        DoctorWarehouseStockHandle stockHandle;
//        if (null == stockIn.getStockHandleId()) { //新增
//            stockHandle = doctorWarehouseStockHandleManager.create(stockIn, wareHouse, WarehouseMaterialHandleType.IN, null);
//
//            stockIn.getDetails().forEach(detail -> {
//
//                warehouseInManager.create(detail, stockIn, stockHandle, wareHouse);
//                //增加库存
//                doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), wareHouse);
//            });
//        } else { //编辑
//            stockHandle = doctorWarehouseStockHandleDao.findById(stockIn.getStockHandleId());
//
//            List<DoctorWarehouseMaterialHandle> oldMaterialHandle = doctorWarehouseMaterialHandleDao.findByStockHandle(stockIn.getStockHandleId());
//
//            warehouseInManager.getNew(oldMaterialHandle, stockIn.getDetails()).forEach(detail -> {
//                warehouseInManager.create(detail, stockIn, stockHandle, wareHouse);
//                //增加库存
//                doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), wareHouse);
//            });
//
//            warehouseInManager.getDelete(oldMaterialHandle, stockIn.getDetails()).forEach(materialHandle -> {
//                warehouseInManager.delete(materialHandle);
//                doctorWarehouseStockManager.out(materialHandle.getMaterialId(), materialHandle.getQuantity(), wareHouse);
//            });
//
//            warehouseInManager.getUpdate(oldMaterialHandle, stockIn.getDetails()).forEach((detail, materialHandle) -> {
//                if (!detail.getMaterialId().equals(materialHandle.getMaterialId())) {//更换了物料
//                    //如果是更换了物料，就不需要处理是否更换了金额，是否更换了事件日期，是否更换了备注
//                    warehouseInManager.create(detail, stockIn, stockHandle, wareHouse);
//                    doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), wareHouse);
//
//                    warehouseInManager.delete(materialHandle);
//                    doctorWarehouseStockManager.out(materialHandle.getMaterialId(), materialHandle.getQuantity(), wareHouse);
//                } else {
//
//                    if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0
//                            || !DateUtil.inSameDate(stockHandle.getHandleDate(), stockIn.getHandleDate().getTime())) {
//
//                        //更改了数量，或更改了操作日期
//                        warehouseInManager.recalculate(materialHandle);
//                    }
//                    if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0) {
//                        BigDecimal changedQuantity = detail.getQuantity().subtract(materialHandle.getQuantity());
//                        if (changedQuantity.compareTo(new BigDecimal(0)) > 0) {
//                            doctorWarehouseStockManager.in(detail.getMaterialId(), changedQuantity, wareHouse);
//                        } else {
//                            doctorWarehouseStockManager.out(detail.getMaterialId(), changedQuantity, wareHouse);
//                        }
//                        warehouseInManager.updateQuantity(materialHandle, changedQuantity);
//                    }
//                    if (!DateUtil.inSameDate(stockHandle.getHandleDate(), stockIn.getHandleDate().getTime())) {
//                        warehouseInManager.buildNewHandleDateForUpdate(materialHandle, stockIn.getHandleDate());
//                    }
//
//                    if (detail.getQuantity().compareTo(materialHandle.getQuantity()) != 0 ||
//                            !DateUtil.inSameDate(stockHandle.getHandleDate(), stockIn.getHandleDate().getTime())) {
//                        warehouseInManager.recalculate(materialHandle);
//                    }
//
//                    materialHandle.setRemark(detail.getRemark());
//                    doctorWarehouseMaterialHandleDao.update(materialHandle);
//                }
//            });
//
//            doctorWarehouseStockHandleManager.update(stockIn, stockHandle);
//        }
//
//        releaseLocks(locks);
//        return Response.ok(stockHandle.getId());
    }

    @Override
    public Response<Long> out(WarehouseStockOutDto stockOut) {
        return warehouseOutStockService.handle(stockOut);
    }

    @Override
    public Response<Long> refund(WarehouseStockRefundDto stockRefundDto) {
        return warehouseRefundStockService.handle(stockRefundDto);
    }

    private List<Lock> lockedIfNecessary(AbstractWarehouseStockDto stockDto) {

        if (stockDto.getStockHandleId() != null && !stockDto.getHandleDate().equals(Calendar.getInstance())) {

            List<Lock> locks = new ArrayList<>();

            log.info("lock for warehouse :{}", stockDto.getWarehouseId());
            Lock lock = lockRegistry.obtain(stockDto.getWarehouseId().toString());
            if (!lock.tryLock())
                throw new JsonResponseException("stock.handle.in.operation");

            locks.add(lock);
            if (stockDto instanceof WarehouseStockTransferDto) {
                Set<Long> transferInWarehouseIds = new HashSet<>();
                ((WarehouseStockTransferDto) stockDto).getDetails().forEach(d -> {
                    transferInWarehouseIds.add(d.getTransferInWarehouseId());
                });

                transferInWarehouseIds.forEach(id -> {
                    log.info("lock for warehouse :{}", id);
                    Lock l = lockRegistry.obtain(id);
                    if (!l.tryLock())
                        throw new JsonResponseException("stock.handle.in.operation");
                    locks.add(l);
                });
            }

            return locks;
        }
        return Collections.emptyList();
    }

    private void releaseLocks(List<Lock> locks) {
        locks.forEach(l -> {
            l.unlock();
        });
    }


    @Override
    public Response<Long> inventory(WarehouseStockInventoryDto stockInventory) {

        return warehouseInventoryStockService.handle(stockInventory);
    }

    @Override
    public Response<Long> transfer(WarehouseStockTransferDto stockTransfer) {
        return warehouseTransferStockService.handle(stockTransfer);
    }

    @Override
    public Response<Long> formula(WarehouseFormulaDto formulaDto) {
        return warehouseFormulaStockService.handle(formulaDto);
    }

    @Override
    public Response<Long> updateFormula(WarehouseFormulaDto formulaDto) {
        return warehouseFormulaStockService.handle(formulaDto);
    }

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
        DoctorWareHouse wareHouse = doctorWareHouseDao.findById(warehouseId);
        if (null == wareHouse)
            throw new InvalidException("warehouse.not.found", warehouseId);


        StockContext context = new StockContext();
        context.setWareHouse(wareHouse);
        return context;
    }


    private DoctorWarehouseStock getStock(Long warehouseID, Long materialID, String vendorName) {
        DoctorWarehouseStock criteria = new DoctorWarehouseStock();
        criteria.setWarehouseId(warehouseID);
        criteria.setSkuId(materialID);

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
            stockDto, BigDecimal quantity, BigDecimal price, int type) {
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

    /**
     * 出库、入库单据主表、明细表新增,物料领用新增
     *
     * @param doctorWarehouseStockHandle
     * @param list
     * @param doctorWarehouseMaterialApplies
     * @return
     */
    @Override
    @Transactional
    public Response<Long> create(DoctorWarehouseStockHandle doctorWarehouseStockHandle,
                                 List<DoctorWarehouseMaterialHandle> list,
                                 List<DoctorWarehouseMaterialHandle> dblist,
                                 List<DoctorWarehouseMaterialApply> doctorWarehouseMaterialApplies) {
        int count = 1;
        try {
            //先新增主表数据,会自动填充主键id值
            doctorWarehouseStockHandleDao.create(doctorWarehouseStockHandle);

            //填充list集合id值,填充主键关联Id值
            for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : list) {
                doctorWarehouseMaterialHandle.setStockHandleId(doctorWarehouseStockHandle.getId());
            }
            int step = doctorWarehouseMaterialHandleDao.creates(list);
            count += step;

            if (!CollectionUtils.isEmpty(dblist)) {
                for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : dblist) {
                    doctorWarehouseMaterialHandle.setStockHandleId(doctorWarehouseStockHandle.getId());
                }
                step = doctorWarehouseMaterialHandleDao.creates(dblist);
                count += step;
            }

            if (!CollectionUtils.isEmpty(doctorWarehouseMaterialApplies)) {
                for (int i = 0; i < list.size(); i++) {
                    doctorWarehouseMaterialApplies.get(i).setMaterialHandleId(list.get(i).getId());
                }
                step = doctorWarehouseMaterialApplyDao.creates(doctorWarehouseMaterialApplies);
                count += step;
            }
        } catch (Exception e) {
            log.error("create warehouseStock failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("warehouseStock.create.fail");
        }
        return Response.ok(new Long(count));
    }

    @Override
    @Transactional
    public Response<Long> update(DoctorWarehouseStockHandle doctorWarehouseStockHandle,
                                 List<DoctorWarehouseMaterialHandle> list,
                                 List<DoctorWarehouseMaterialHandle> dblist,
                                 List<DoctorWarehouseMaterialApply> doctorWarehouseMaterialApplies) {
        int count = 1;
        try {
            //先新增主表数据,会自动填充主键id值
            doctorWarehouseStockHandleDao.update(doctorWarehouseStockHandle);

            //填充list集合id值,填充主键关联Id值
            for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : list) {
                doctorWarehouseMaterialHandleDao.update(doctorWarehouseMaterialHandle);
                count++;
            }

            if (!CollectionUtils.isEmpty(dblist)) {
                for (DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle : dblist) {
                    doctorWarehouseMaterialHandleDao.update(doctorWarehouseMaterialHandle);
                    count++;
                }
            }

            if (!CollectionUtils.isEmpty(doctorWarehouseMaterialApplies)) {
                for (DoctorWarehouseMaterialApply doctorWarehouseMaterialApply : doctorWarehouseMaterialApplies) {
                    doctorWarehouseMaterialApplyDao.update(doctorWarehouseMaterialApply);
                    count++;
                }
            }
        } catch (Exception e) {
            log.error("update warehouseStock failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("warehouseStock.update.fail");
        }
        return Response.ok(new Long(count));
    }

}