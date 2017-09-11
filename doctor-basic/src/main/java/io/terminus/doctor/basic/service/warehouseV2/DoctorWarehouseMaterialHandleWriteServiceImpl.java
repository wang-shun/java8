package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseHandleDetailDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.manager.DoctorWarehouseHandlerManager;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseHandleDetail;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialHandleWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 08:56:13
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMaterialHandleWriteServiceImpl implements DoctorWarehouseMaterialHandleWriteService {

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;

    @Autowired
    private DoctorWarehouseHandleDetailDao doctorWarehouseHandleDetailDao;

    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Autowired
    private DoctorWarehouseHandlerManager doctorWarehouseHandlerManager;


    @Override
    public Response<Long> create(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        try {
            doctorWarehouseMaterialHandleDao.create(doctorWarehouseMaterialHandle);
            return Response.ok(doctorWarehouseMaterialHandle.getId());
        } catch (Exception e) {
            log.error("failed to create doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.update(doctorWarehouseMaterialHandle));
        } catch (Exception e) {
            log.error("failed to update doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.update.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long id) {
        try {


            DoctorWarehouseMaterialHandle handle = doctorWarehouseMaterialHandleDao.findById(id);
            if (null == handle) {
                log.info("物料处理明细不存在,忽略仓库事件删除操作,id[{}]", id);
                return Response.ok(true);
            }

            if (WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue() == handle.getType() ||
                    WarehouseMaterialHandleType.IN.getValue() == handle.getType()) {

                reverseIn(handle);

            } else if (WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue() == handle.getType() ||
                    WarehouseMaterialHandleType.OUT.getValue() == handle.getType()) {

                reverseOut(handle);
            } else if (WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == handle.getType()
                    || WarehouseMaterialHandleType.TRANSFER_IN.getValue() == handle.getType()) {
                DoctorWarehouseMaterialHandle otherHandle = doctorWarehouseMaterialHandleDao.findById(handle.getOtherTrasnferHandleId());
                if (null == otherHandle)
                    return Response.fail("other.material.handle.not.found");

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
                return Response.fail("not.support.material.handle.type");


            handle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
            return Response.ok(doctorWarehouseMaterialHandleDao.update(handle));
        } catch (ServiceException e) {
            log.error("failed to delete doctor warehouse material handle by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("failed to delete doctor warehouse material handle by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.delete.fail");
        }
    }

    private void reverseIn(DoctorWarehouseMaterialHandle handle) {

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(DoctorWarehouseStock.builder()
                .warehouseId(handle.getWarehouseId())
                .materialId(handle.getMaterialId())
                .build());
        if (null == stocks || stocks.isEmpty())
            throw new ServiceException("stock.not.found");

        if (stocks.get(0).getQuantity().compareTo(handle.getQuantity()) < 0)
            throw new ServiceException("stock.not.enough");

        //扣减库存
        stocks.get(0).setQuantity(stocks.get(0).getQuantity().subtract(handle.getQuantity()));

        //当时入库记录
        List<DoctorWarehouseHandleDetail> outDetails = doctorWarehouseHandleDetailDao.list(DoctorWarehouseHandleDetail.builder()
                .materialHandleId(handle.getId())
                .build());
        if (null == outDetails || outDetails.isEmpty())
            throw new ServiceException("stock.out.detail.not.found");

        List<DoctorWarehousePurchase> purchases = doctorWarehousePurchaseDao.findByIds(outDetails.stream().map(DoctorWarehouseHandleDetail::getMaterialPurchaseId).collect(Collectors.toList()));
        if (null == purchases || purchases.isEmpty())
            throw new ServiceException("purchase.not.found");

        purchases.get(0).setHandleQuantity(purchases.get(0).getHandleQuantity().add(handle.getQuantity()));
        if (purchases.get(0).getHandleQuantity().compareTo(purchases.get(0).getQuantity()) >= 0)
            purchases.get(0).setHandleFinishFlag(WarehousePurchaseHandleFlag.OUT_FINISH.getValue());

        DoctorWarehouseHandlerManager.PurchaseHandleContext purchaseHandleContext = new DoctorWarehouseHandlerManager.PurchaseHandleContext();
        purchaseHandleContext.setStock(stocks.get(0));
        purchaseHandleContext.setPurchaseQuantity(Collections.singletonMap(purchases.get(0), handle.getQuantity()));
        doctorWarehouseHandlerManager.outStock(stocks.get(0), purchaseHandleContext, null);
    }

    private void reverseOut(DoctorWarehouseMaterialHandle handle) {
        List<DoctorWarehouseStock> stock = doctorWarehouseStockDao.list(DoctorWarehouseStock.builder()
                .warehouseId(handle.getWarehouseId())
                .materialId(handle.getMaterialId())
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
            purchase.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
            BigDecimal quantity = new BigDecimal(0);
            List<DoctorWarehouseHandleDetail> thisOut = purchaseMap.get(purchase.getId());
            for (DoctorWarehouseHandleDetail outDetail : thisOut) {
                quantity = quantity.add(outDetail.getQuantity());
            }
            purchase.setHandleQuantity(purchase.getHandleQuantity().subtract(quantity));
        }

        stock.get(0).setQuantity(stock.get(0).getQuantity().add(handle.getQuantity()));
        doctorWarehouseHandlerManager.inStock(stock.get(0), purchases, null,null);
    }

}