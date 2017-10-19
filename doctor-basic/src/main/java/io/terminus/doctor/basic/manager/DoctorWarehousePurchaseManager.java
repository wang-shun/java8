package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.common.exception.InvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/9/12.
 */
@Component
public class DoctorWarehousePurchaseManager {


    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;
    @Autowired
    private DoctorWarehouseStockManager doctorWarehouseStockManager;

    //    @Transactional
    public DoctorWarehousePurchase in(AbstractWarehouseStockDto stockDto, WarehouseStockInDto.WarehouseStockInDetailDto detail, DoctorWarehouseStock stock, DoctorWarehouseSku sku) {

        DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
        purchase.setFarmId(stock.getFarmId());
        purchase.setWarehouseId(stock.getWarehouseId());
        purchase.setWarehouseName(stock.getWarehouseName());
        purchase.setWarehouseType(stock.getWarehouseType());
        purchase.setMaterialId(detail.getMaterialId());
        purchase.setVendorName(sku.getVendorName());
        purchase.setQuantity(detail.getQuantity());
        purchase.setHandleQuantity(new BigDecimal(0));
        purchase.setUnitPrice(detail.getUnitPrice());
        purchase.setHandleDate(stockDto.getHandleDate().getTime());
        purchase.setHandleYear(stockDto.getHandleDate().get(Calendar.YEAR));
        purchase.setHandleMonth(stockDto.getHandleDate().get(Calendar.MONTH) + 1);
        purchase.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
        doctorWarehousePurchaseDao.create(purchase);
        return purchase;
    }

    //    @Transactional(propagation = Propagation.NESTED)
    public DoctorWarehouseHandlerManager.PurchaseHandleContext out(DoctorWarehouseStock stock, BigDecimal quantity) {

        DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
        purchaseCriteria.setWarehouseId(stock.getWarehouseId());
        purchaseCriteria.setMaterialId(stock.getSkuId());
        purchaseCriteria.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());//未出库完的
        List<DoctorWarehousePurchase> materialPurchases = doctorWarehousePurchaseDao.list(purchaseCriteria);
        if (null == materialPurchases || materialPurchases.isEmpty())
            throw new InvalidException("purchase.not.found", stock.getWarehouseName(), stock.getSkuName());
        materialPurchases.sort(Comparator.comparing(DoctorWarehousePurchase::getHandleDate));

        DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = new DoctorWarehouseHandlerManager.PurchaseHandleContext();

        BigDecimal needPurchaseQuantity = quantity;
        BigDecimal totalHandleQuantity = new BigDecimal(0);
        long totalHandleMoney = 0L;
        for (DoctorWarehousePurchase purchase : materialPurchases) {
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

        for (DoctorWarehousePurchase purchase : materialPurchases) {
            doctorWarehousePurchaseDao.update(purchase);
        }

        return purchaseHandleContext;
    }


    public long calculateUnitPrice(Long warehouseId, Long materialId) {
        return calculateUnitPrice(doctorWarehouseStockManager.getStock(warehouseId, materialId).orElseThrow(() ->
                new InvalidException("stock.not.found", warehouseId, materialId)
        ));
    }

    /**
     * 计算单价
     * 先找本月物料所有的入库价格记录，加权平均算单价
     * 如果本月没有入库记录，取最近一次入库记录的单价
     *
     * @param stock
     * @return
     * @throws ServiceException 如果物料没有任何入库记录
     */
    public long calculateUnitPrice(DoctorWarehouseStock stock) {
        Calendar thisMonth = Calendar.getInstance();
//        lastMonth.add(Calendar.MONTH, -1);//上一个月
        List<DoctorWarehousePurchase> thisMonthPurchases = doctorWarehousePurchaseDao.list(DoctorWarehousePurchase.builder()
                .warehouseId(stock.getWarehouseId())
                .materialId(stock.getSkuId())
                .handleYear(thisMonth.get(Calendar.YEAR))
                .handleMonth(thisMonth.get(Calendar.MONTH) + 1)//Calendar第一个月以0开始
                .build());

        if (thisMonthPurchases.isEmpty()) {
            PageInfo pageInfo = new PageInfo(1, 1);
            Paging<DoctorWarehousePurchase> lastOnePurchases = doctorWarehousePurchaseDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), DoctorWarehousePurchase.builder()
                    .warehouseId(stock.getWarehouseId())
                    .materialId(stock.getSkuId())
                    .build());
            if (lastOnePurchases.isEmpty())
                throw new InvalidException("purchase.not.found", stock.getWarehouseName(), stock.getSkuName());
            return lastOnePurchases.getData().get(0).getUnitPrice();
        } else {

            long totalPrice = 0;
            BigDecimal totalQuantity = new BigDecimal(0);
            for (DoctorWarehousePurchase purchase : thisMonthPurchases) {
                totalPrice = totalPrice + purchase.getQuantity().multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
                totalQuantity = totalQuantity.add(purchase.getQuantity());
            }
            return new BigDecimal(totalPrice).divide(totalQuantity, 0, BigDecimal.ROUND_HALF_UP).longValue();
        }
    }
}
