package io.terminus.doctor.basic.service;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialHandleWriteService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockReadService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public Response<Long> create(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        try {
            doctorWarehouseMaterialHandleDao.create(doctorWarehouseMaterialHandle);
            return Response.ok(doctorWarehouseMaterialHandle.getId());
        } catch (Exception e) {
            log.error("failed to create doctor warehouseV2 material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.material.handle.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.update(doctorWarehouseMaterialHandle));
        } catch (Exception e) {
            log.error("failed to update doctor warehouseV2 material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.material.handle.update.fail");
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


            } else if (WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue() == handle.getType() ||
                    WarehouseMaterialHandleType.OUT.getValue() == handle.getType()) {

                List<DoctorWarehouseStock> stock = doctorWarehouseStockDao.list(DoctorWarehouseStock.builder()
                        .warehouseId(handle.getWarehouseId())
                        .materialId(handle.getMaterialId())
                        .vendorName(handle.getVendorName())
                        .build());
                if (null == stock || stock.isEmpty())
                    throw new ServiceException("stock.not.found");
                //出库，可能出多个供应商的库。应该入哪个供应商的？


            } else if (WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == handle.getType()
                    || WarehouseMaterialHandleType.TRANSFER_IN.getValue() == handle.getType()) {

            } else
                return Response.fail("not.support.material.handle.type");


            handle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
            return Response.ok(doctorWarehouseMaterialHandleDao.update(handle));
        } catch (Exception e) {
            log.error("failed to delete doctor warehouseV2 material handle by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.material.handle.delete.fail");
        }
    }

}