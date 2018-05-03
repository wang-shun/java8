package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.manager.*;
import io.terminus.doctor.basic.model.warehouseV2.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private DoctorWarehouseStockMonthlyManager doctorWarehouseStockMonthlyManager;
    @Autowired
    private DoctorWarehouseMaterialHandleManager doctorWarehouseMaterialHandleManager;
    @Autowired
    private WarehouseOutManager warehouseOutManager;
    @Autowired
    private WarehouseInManager warehouseInManager;

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
    @Transactional
    @ExceptionHandle("doctor.warehouse.material.handle.delete.fail")
    public Response<String> delete(Long id) {

        DoctorWarehouseMaterialHandle handle = doctorWarehouseMaterialHandleDao.findById(id);
        if (null == handle) {
            log.info("物料处理明细不存在,忽略仓库事件删除操作,id[{}]", id);
            return Response.fail("删除失败，物料明细不存在");
        }

        Boolean flag=false;
        if(flag==false) {
            int type = handle.getType();
            if (type != 1 && type != 2 && type != 3 && type != 4 && type != 5 && type != 10 && type != 7 && type != 8 && type != 9
                    && type != 11 && type != 12 && type != 13) {
                return Response.fail("未知类型");
            }
            //配方生产
            if (type == 5) {
                return Response.fail("配方生产不支持删除");
            }
            //调拨入库
            if (type == 9) {
                return Response.fail("调拨入库不支持删除");
            }
            //采购入库,退料入库,盘盈入库
            if (type == 1 || type == 13 || type == 7) {
                warehouseInManager.recalculate(handle);
                warehouseInManager.delete(handle);
            }
            //盘亏出库
            if (type == 8) {
                warehouseOutManager.delete(handle);
            }
            //领料出库
            if (type == 2) {
                Integer countByRelMaterialHandleId = doctorWarehouseMaterialHandleDao.getCountByRelMaterialHandleId(handle.getRelMaterialHandleId(), 13);
                if (countByRelMaterialHandleId > 0) {
                    return Response.fail("此物料存在退料,不支持删除");
                } else {
                    warehouseOutManager.delete(handle);
                }
            }
            //配方出库,调拨出库
            if (type == 12 || type == 10) {
                int tt=0;
                if(type == 12){
                    tt=11;
                }else if(type==10){
                    tt=9;
                }
                DoctorWarehouseMaterialHandle byRelMaterialHandleId = doctorWarehouseMaterialHandleDao.findByRelMaterialHandleId(handle.getRelMaterialHandleId(), tt);
                if (byRelMaterialHandleId != null) {
                    warehouseInManager.recalculate(byRelMaterialHandleId);//校验被入库是否为正
                    warehouseInManager.delete(byRelMaterialHandleId);//删除被入库的单据明细表
                }
                warehouseOutManager.delete(handle);//删除出库单的单据明细
            }
            flag=true;
        }
//        doctorWarehouseMaterialHandleManager.delete(handle);

        if(flag==true){
            return Response.ok("删除成功");
        }else{
            return Response.fail("删除失败");
        }
    }


}