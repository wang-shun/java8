package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.AbstractStockManager;
import io.terminus.doctor.basic.manager.DoctorWarehouseMaterialHandleManager;
import io.terminus.doctor.basic.manager.WarehouseInManager;
import io.terminus.doctor.basic.manager.WarehouseOutManager;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.exception.InvalidException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private WarehouseOutManager warehouseOutManager;
    @Autowired
    private WarehouseInManager warehouseInManager;
    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;
    @RpcConsumer
    private DoctorBasicDao doctorBasicDao;

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
        for (DoctorWarehouseMaterialHandle handle : handles) {
            int type = handle.getType();
            if(type != 1 || type != 2 ||type != 3 ||type != 4 ||type != 5 ||type != 6 ||type != 7 ||type != 8 ||type != 9){
                return Response.fail("未知");
            }
            //配方生产
            if (type == 3) {
                return Response.fail("配方生产不支持删除");
            }
            //调拨入库
            if (type == 5) {
                return Response.fail("调拨入库不支持删除");
            }
            //采购入库,退料入库,盘盈入库
            if(type==1 || type == 2 || type==4){
                warehouseInManager.recalculate(handle);
                warehouseInManager.delete(handle);
            }
            //盘亏出库
            if(type==7){
                warehouseOutManager.delete(handle);
            }
            //领料出库
            if(type == 6){
                DoctorWarehouseStockHandle a = doctorWarehouseStockHandleDao.findByRelStockHandleId(id,type);
                if(a != null){
                    return Response.fail("此物料存在退料,不支持删除");
                }
                warehouseOutManager.delete(handle);
            }
            //配方出库,调拨出库
            if (type == 8 || type == 9) {
                DoctorWarehouseStockHandle a = doctorWarehouseStockHandleDao.findByRelStockHandleId(id,type);//被入库的单据表
                if(a != null) {
                    DoctorWarehouseMaterialHandle b = doctorWarehouseMaterialHandleDao.findByStockHandleId(a.getId());//被入库的单据明细表
                    if(b != null) {
                        warehouseInManager.recalculate(b);//校验被入库是否为正
                        warehouseInManager.delete(b);//删除被入库的单据明细表
                    }
                    doctorWarehouseStockHandleDao.delete(a.getId());//删除被入库的单据表
                }
                warehouseOutManager.delete(handle);//删除出库单的单据明细
            }
        }
            /*Map<Long*//*skuId*//*, List<DoctorWarehouseMaterialHandle>> needValidSkuHandle = handles.stream()
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
                if (stock.getQuantity().add(thisSkuAllOutQuantity).compareTo(thisSkuAllInQuantity) < 0) {
                    DoctorBasic unit = doctorBasicDao.findById(Long.parseLong(sku.getUnit()));
                    throw new InvalidException("stock.not.enough.rollback",
                            needValidSkuHandle.get(skuId).get(0).getWarehouseName(),
                            needValidSkuHandle.get(skuId).get(0).getMaterialName(),
                            stock.getQuantity(),
                            null == unit ? "" : unit.getName(), thisSkuAllInQuantity);
                }
            }

            handles.stream().forEach(h -> {
                doctorWarehouseMaterialHandleManager.delete(h);
            });*/
            doctorWarehouseStockHandleDao.delete(id);
            return Response.ok(true);
        }
}