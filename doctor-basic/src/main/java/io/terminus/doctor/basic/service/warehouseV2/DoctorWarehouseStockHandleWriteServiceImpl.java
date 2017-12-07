package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseSkuDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.DoctorWarehouseMaterialHandleManager;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.exception.InvalidException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 14:49:19
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseStockHandleWriteServiceImpl implements DoctorWarehouseStockHandleWriteService {

    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;
    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;
    @Autowired
    private DoctorWarehouseMaterialHandleManager doctorWarehouseMaterialHandleManager;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;
    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;

    @Override
    public Response<Long> create(DoctorWarehouseStockHandle doctorWarehouseStockHandle) {
        try {
            doctorWarehouseStockHandleDao.create(doctorWarehouseStockHandle);
            return Response.ok(doctorWarehouseStockHandle.getId());
        } catch (Exception e) {
            log.error("failed to create doctor warehouse stock handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.handle.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseStockHandle doctorWarehouseStockHandle) {
        try {
            return Response.ok(doctorWarehouseStockHandleDao.update(doctorWarehouseStockHandle));
        } catch (Exception e) {
            log.error("failed to update doctor warehouse stock handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.handle.update.fail");
        }
    }

    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.stock.handle.delete.fail")
    public Response<Boolean> delete(Long id) {
        List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.findByStockHandle(id);

        Map<Long/*skuId*/, List<DoctorWarehouseMaterialHandle>> needValidSkuHandle = handles.stream()
                .collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));
        for (Long skuId : needValidSkuHandle.keySet()) {

            DoctorWarehouseStock stock = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(needValidSkuHandle.get(skuId).get(0).getMaterialId(), needValidSkuHandle.get(skuId).get(0).getWarehouseId())
                    .orElseThrow(() -> new InvalidException("stock.not.found", needValidSkuHandle.get(skuId).get(0).getWarehouseName(), needValidSkuHandle.get(skuId).get(0).getMaterialName()));
            DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(needValidSkuHandle.get(skuId).get(0).getMaterialId());
            if (null == sku)
                throw new InvalidException("warehouse.sku.not.found", needValidSkuHandle.get(skuId).get(0).getMaterialId());

            List<DoctorWarehouseMaterialHandle> thisSkuHandles = needValidSkuHandle.get(skuId);
            BigDecimal thisSkuAllOutQuantity = thisSkuHandles.stream().filter(h -> WarehouseMaterialHandleType.isBigOut(h.getType())).map(DoctorWarehouseMaterialHandle::getQuantity).reduce((a, b) -> a.add(b)).orElse(new BigDecimal(0));
            BigDecimal thisSkuAllInQuantity = thisSkuHandles.stream().filter(h -> WarehouseMaterialHandleType.isBigIn(h.getType())).map(DoctorWarehouseMaterialHandle::getQuantity).reduce((a, b) -> a.add(b)).orElse(new BigDecimal(0));
            log.debug("check stock is enough for delete,stock[{}],out[{}],in[{}]", stock.getQuantity(), thisSkuAllOutQuantity, thisSkuAllInQuantity);
            if (stock.getQuantity().add(thisSkuAllOutQuantity).compareTo(thisSkuAllInQuantity) < 0)
                throw new InvalidException("stock.not.enough", needValidSkuHandle.get(skuId).get(0).getWarehouseName(), needValidSkuHandle.get(skuId).get(0).getMaterialName(), stock.getQuantity(), sku.getUnit());
        }

        handles.stream().forEach(h -> {
            doctorWarehouseMaterialHandleManager.delete(h);
        });
        doctorWarehouseStockHandleDao.delete(id);
        return Response.ok(true);
    }

}