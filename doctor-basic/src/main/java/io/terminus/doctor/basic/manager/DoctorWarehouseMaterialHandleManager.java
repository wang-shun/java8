package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.warehouseV2.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2017/9/12.
 */
@Component
public class DoctorWarehouseMaterialHandleManager {


    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehouseHandleDetailDao doctorWarehouseHandleDetailDao;

    @Autowired
    private DoctorVendorManager doctorVendorManager;
    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;
    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;
    @Autowired
    private DoctorWarehouseStockMonthlyManager doctorWarehouseStockMonthlyManager;
    @Autowired
    private DoctorWarehouseHandlerManager doctorWarehouseHandlerManager;
    @Autowired
    private DoctorWarehousePurchaseManager doctorWarehousePurchaseManager;

    @Autowired
    private DoctorBasicDao doctorBasicDao;

    /**
     * 入库
     */
//    @Transactional
    public void in(MaterialHandleContext materialHandleContext) {
        handle(materialHandleContext, WarehouseMaterialHandleType.IN);
    }

    //    @Transactional(propagation = Propagation.NESTED)
    public DoctorWarehouseMaterialHandle out(MaterialHandleContext materialHandleContext) {
        return handle(materialHandleContext, WarehouseMaterialHandleType.OUT);
    }

    @Transactional
    public DoctorWarehouseMaterialHandle handle(MaterialHandleContext materialHandleContext, WarehouseMaterialHandleType type) {

        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setFarmId(materialHandleContext.getStock().getFarmId());
        materialHandle.setWarehouseId(materialHandleContext.getStock().getWarehouseId());
        materialHandle.setWarehouseName(materialHandleContext.getStock().getWarehouseName());
        materialHandle.setWarehouseType(materialHandleContext.getStock().getWarehouseType());
        materialHandle.setMaterialId(materialHandleContext.getStock().getSkuId());
        materialHandle.setMaterialName(materialHandleContext.getStock().getSkuName());
        materialHandle.setUnitPrice(materialHandleContext.getUnitPrice());
        materialHandle.setType(type.getValue());
        materialHandle.setQuantity(materialHandleContext.getQuantity());
        materialHandle.setHandleDate(materialHandleContext.getStockDto().getHandleDate().getTime());
        materialHandle.setOperatorId(materialHandleContext.getStockDto().getOperatorId());
        materialHandle.setOperatorName(materialHandleContext.getStockDto().getOperatorName());
        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
        DoctorBasic unit = doctorBasicDao.findById(Long.parseLong(materialHandleContext.getSku().getUnit()));
        if (null != unit)
            materialHandle.setUnit(unit.getName());

//        materialHandle.setVendorName(doctorVendorManager.findById(materialHandleContext.getSku().getVendorId()).getName());
        materialHandle.setVendorName(doctorVendorManager.findById(materialHandleContext.getSku().getVendorId()).getShortName());
        materialHandle.setHandleYear(materialHandleContext.getStockDto().getHandleDate().get(Calendar.YEAR));
        materialHandle.setHandleMonth(materialHandleContext.getStockDto().getHandleDate().get(Calendar.MONTH) + 1);
        materialHandle.setRemark(materialHandleContext.getStockDetail().getRemark());

        materialHandle.setStockHandleId(materialHandleContext.getStockHandle().getId());

        doctorWarehouseMaterialHandleDao.create(materialHandle);

        for (DoctorWarehousePurchase purchase : materialHandleContext.getPurchases().keySet()) {
            doctorWarehouseHandleDetailDao.create(DoctorWarehouseHandleDetail.builder()
                    .materialHandleId(materialHandle.getId())
                    .materialPurchaseId(purchase.getId())
                    .quantity(materialHandleContext.getPurchases().get(purchase))
                    .handleYear(materialHandleContext.getStockDto().getHandleDate().get(Calendar.YEAR))
                    .handleMonth(materialHandleContext.getStockDto().getHandleDate().get(Calendar.MONTH) + 1)
                    .build());
        }

        return materialHandle;
    }

    public void delete(DoctorWarehouseMaterialHandle handle) {
        if (WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue() == handle.getType() ||
                WarehouseMaterialHandleType.IN.getValue() == handle.getType()) {

            reverseIn(handle);

        } else if (WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue() == handle.getType() ||
                WarehouseMaterialHandleType.OUT.getValue() == handle.getType()) {

            reverseOut(handle);
        } else if (WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == handle.getType()
                || WarehouseMaterialHandleType.TRANSFER_IN.getValue() == handle.getType()) {
            DoctorWarehouseMaterialHandle otherHandle = doctorWarehouseMaterialHandleDao.findById(handle.getRelMaterialHandleId());
            if (null == otherHandle)
                throw new ServiceException("other.material.handle.not.found");

            if (WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == handle.getType()) {
                reverseIn(otherHandle);
                reverseOut(handle);
            } else {
                reverseIn(handle);
                reverseOut(otherHandle);
            }

            otherHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
            doctorWarehouseMaterialHandleDao.update(otherHandle);

        } else
            throw new ServiceException("not.support.material.handle.type");

        handle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(handle);
    }

    private void reverseIn(DoctorWarehouseMaterialHandle handle) {

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(DoctorWarehouseStock.builder()
                .warehouseId(handle.getWarehouseId())
                .skuId(handle.getMaterialId())
                .build());
        if (null == stocks || stocks.isEmpty())
            throw new ServiceException("stock.not.found");

        if (stocks.get(0).getQuantity().compareTo(handle.getQuantity()) < 0)
            throw new ServiceException("stock.not.enough");

        //扣减库存
        stocks.get(0).setQuantity(stocks.get(0).getQuantity().subtract(handle.getQuantity()));
        doctorWarehouseStockDao.update(stocks.get(0));


        doctorWarehousePurchaseManager.delete(handle);
        //当时入库记录
//        List<DoctorWarehouseHandleDetail> outDetails = doctorWarehouseHandleDetailDao.list(DoctorWarehouseHandleDetail.builder()
//                .materialHandleId(handle.getId())
//                .build());
//        if (null == outDetails || outDetails.isEmpty())
//            throw new ServiceException("stock.out.detail.not.found");
//
//        List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.findByIds(outDetails.stream().map(DoctorWarehouseHandleDetail::getMaterialPurchaseId).collect(Collectors.toList()));
//        if (null == purchases || purchases.isEmpty())
//            throw new ServiceException("purchase.not.found");
//
//        purchases.get(0).setHandleQuantity(purchases.get(0).getHandleQuantity().add(handle.getQuantity()));
//        if (purchases.get(0).getHandleQuantity().compareTo(purchases.get(0).getQuantity()) >= 0)
//            purchases.get(0).setHandleFinishFlag(WarehousePurchaseHandleFlag.OUT_FINISH.getValue());
//
//        DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = new DoctorWarehouseHandlerManager.PurchaseHandleContext();
//        purchaseHandleContext.setStock(stocks.get(0));
//        purchaseHandleContext.setPurchaseQuantity(Collections.singletonMap(purchases.get(0), handle.getQuantity()));
//        doctorWarehouseHandlerManager.outStock(stocks.get(0), purchaseHandleContext, null);
        doctorWarehouseStockMonthlyManager.count(handle.getWarehouseId(),
                handle.getMaterialId(),
                handle.getHandleYear(),
                handle.getHandleMonth(),
                handle.getQuantity(),
                handle.getUnitPrice(),
                false);
    }

    private void reverseOut(DoctorWarehouseMaterialHandle handle) {
        List<DoctorWarehouseStock> stock = doctorWarehouseStockDao.list(DoctorWarehouseStock.builder()
                .warehouseId(handle.getWarehouseId())
                .skuId(handle.getMaterialId())
                .build());
        if (null == stock || stock.isEmpty())
            throw new ServiceException("stock.not.found");

        List<DoctorWarehouseHandleDetail> outDetails = doctorWarehouseHandleDetailDao.list(DoctorWarehouseHandleDetail.builder()
                .materialHandleId(handle.getId())
                .build());

        if (null == outDetails || outDetails.isEmpty())
            throw new ServiceException("stock.out.detail.not.found");

        Map<Long, List<DoctorWarehouseHandleDetail>> purchaseMap = outDetails.stream().collect(Collectors.groupingBy(DoctorWarehouseHandleDetail::getMaterialPurchaseId));

        //找出出库对应的入库
        List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.findByIds(outDetails.stream().map(DoctorWarehouseHandleDetail::getMaterialPurchaseId).collect(Collectors.toList()));
        if (null == purchases || purchases.isEmpty())
            throw new ServiceException("purchase.not.found");

        for (DoctorWarehousePurchase purchase : purchases) {
            BigDecimal quantity = new BigDecimal(0);
            List<DoctorWarehouseHandleDetail> thisOut = purchaseMap.get(purchase.getId());
            for (DoctorWarehouseHandleDetail outDetail : thisOut) {
                quantity = quantity.add(outDetail.getQuantity());
            }

            purchase.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
            purchase.setHandleQuantity(purchase.getHandleQuantity().subtract(quantity));
        }

        stock.get(0).setQuantity(stock.get(0).getQuantity().add(handle.getQuantity()));

        if (handle.getType().intValue() == WarehouseMaterialHandleType.OUT.getValue()) {
            //还需要回滚领用记录
            doctorWarehouseMaterialApplyDao.list(DoctorWarehouseMaterialApply.builder()
                    .materialHandleId(handle.getId())
                    .build()).stream().forEach(apply -> {
                doctorWarehouseMaterialApplyDao.delete(apply.getId());
            });
        }

        doctorWarehouseHandlerManager.inStock(stock.get(0), purchases, null, null, null);
        doctorWarehouseStockMonthlyManager.count(handle.getWarehouseId(),
                handle.getMaterialId(),
                handle.getHandleYear(),
                handle.getHandleMonth(),
                handle.getQuantity(),
                handle.getUnitPrice(),
                true);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaterialHandleContext {
        private AbstractWarehouseStockDto stockDto;
        private AbstractWarehouseStockDetail stockDetail;
        private DoctorWarehouseStock stock;
        private Map<DoctorWarehousePurchase, BigDecimal/*quantity*/> purchases;
        private BigDecimal unitPrice;
        private String vendorName;
        private BigDecimal quantity;
        private DoctorWarehouseSku sku;
        private DoctorWarehouseStockHandle stockHandle;
    }
}
