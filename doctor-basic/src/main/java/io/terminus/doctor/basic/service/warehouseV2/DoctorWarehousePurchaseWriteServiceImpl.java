package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehousePurchaseWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 00:18:50
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehousePurchaseWriteServiceImpl implements DoctorWarehousePurchaseWriteService {

    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Override
    public Response<Long> create(DoctorWarehousePurchase doctorWarehousePurchase) {
        try{
            doctorWarehousePurchaseDao.create(doctorWarehousePurchase);
            return Response.ok(doctorWarehousePurchase.getId());
        }catch (Exception e){
            log.error("failed to create doctor warehouse purchase, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.purchase.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehousePurchase doctorWarehousePurchase) {
        try{
            return Response.ok(doctorWarehousePurchaseDao.update(doctorWarehousePurchase));
        }catch (Exception e){
            log.error("failed to update doctor warehouse purchase, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.purchase.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorWarehousePurchaseDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor warehouse purchase by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.purchase.delete.fail");
        }
    }

}