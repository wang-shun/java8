package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Response<Long> create(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        try{
            doctorWarehouseMaterialHandleDao.create(doctorWarehouseMaterialHandle);
            return Response.ok(doctorWarehouseMaterialHandle.getId());
        }catch (Exception e){
            log.error("failed to create doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        try{
            return Response.ok(doctorWarehouseMaterialHandleDao.update(doctorWarehouseMaterialHandle));
        }catch (Exception e){
            log.error("failed to update doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorWarehouseMaterialHandleDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor warehouse material handle by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.delete.fail");
        }
    }

}