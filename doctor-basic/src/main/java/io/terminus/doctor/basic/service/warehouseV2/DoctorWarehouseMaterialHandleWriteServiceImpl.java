package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.manager.DoctorWarehouseHandlerManager;
import io.terminus.doctor.basic.manager.DoctorWarehouseMaterialHandleManager;
import io.terminus.doctor.basic.manager.DoctorWarehouseStockMonthlyManager;
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
    public Response<Boolean> delete(Long id) {

        DoctorWarehouseMaterialHandle handle = doctorWarehouseMaterialHandleDao.findById(id);
        if (null == handle) {
            log.info("物料处理明细不存在,忽略仓库事件删除操作,id[{}]", id);
            return Response.ok(true);
        }

        doctorWarehouseMaterialHandleManager.delete(handle);

        return Response.ok(true);
    }


}