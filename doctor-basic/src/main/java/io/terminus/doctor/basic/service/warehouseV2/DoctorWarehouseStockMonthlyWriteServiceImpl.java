package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorWarehouseStockMonthlyDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 月度结余
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseStockMonthlyWriteServiceImpl implements DoctorWarehouseStockMonthlyWriteService {

    @Autowired
    private DoctorWarehouseStockMonthlyDao doctorWarehouseStockMonthlyDao;

    @Override
    public Response<Long> create(DoctorWarehouseStockMonthly doctorWarehouseStockMonthly) {
        try {
            doctorWarehouseStockMonthlyDao.create(doctorWarehouseStockMonthly);
            return Response.ok(doctorWarehouseStockMonthly.getId());
        } catch (Exception e) {
            log.error("failed to create doctor warehouse stock monthly, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.monthly.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseStockMonthly doctorWarehouseStockMonthly) {
        try {
            return Response.ok(doctorWarehouseStockMonthlyDao.update(doctorWarehouseStockMonthly));
        } catch (Exception e) {
            log.error("failed to update doctor warehouse stock monthly, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.monthly.update.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long id) {
        try {
            return Response.ok(doctorWarehouseStockMonthlyDao.delete(id));
        } catch (Exception e) {
            log.error("failed to delete doctor warehouse stock monthly by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.monthly.delete.fail");
        }
    }

}