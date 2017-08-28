package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMonthlyStockDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMonthlyStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-17 15:02:59
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMonthlyStockWriteServiceImpl implements DoctorWarehouseMonthlyStockWriteService {

    @Autowired
    private DoctorWarehouseMonthlyStockDao doctorWarehouseMonthlyStockDao;

    @Override
    public Response<Long> create(DoctorWarehouseMonthlyStock doctorWarehouseMonthlyStock) {
        try{
            doctorWarehouseMonthlyStockDao.create(doctorWarehouseMonthlyStock);
            return Response.ok(doctorWarehouseMonthlyStock.getId());
        }catch (Exception e){
            log.error("failed to create doctor warehouseV2 monthly stock, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.monthly.stock.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseMonthlyStock doctorWarehouseMonthlyStock) {
        try{
            return Response.ok(doctorWarehouseMonthlyStockDao.update(doctorWarehouseMonthlyStock));
        }catch (Exception e){
            log.error("failed to update doctor warehouseV2 monthly stock, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.monthly.stock.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorWarehouseMonthlyStockDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor warehouseV2 monthly stock by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.monthly.stock.delete.fail");
        }
    }

}