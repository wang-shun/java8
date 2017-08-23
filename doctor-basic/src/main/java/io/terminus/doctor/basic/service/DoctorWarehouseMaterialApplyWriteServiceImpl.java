package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;

import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialApply;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMaterialApplyWriteServiceImpl implements DoctorWarehouseMaterialApplyWriteService {

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Override
    public Response<Long> create(DoctorWarehouseMaterialApply doctorWarehouseMaterialApply) {
        try{
            doctorWarehouseMaterialApplyDao.create(doctorWarehouseMaterialApply);
            return Response.ok(doctorWarehouseMaterialApply.getId());
        }catch (Exception e){
            log.error("failed to create doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseMaterialApply doctorWarehouseMaterialApply) {
        try{
            return Response.ok(doctorWarehouseMaterialApplyDao.update(doctorWarehouseMaterialApply));
        }catch (Exception e){
            log.error("failed to update doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorWarehouseMaterialApplyDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor warehouse material apply by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.delete.fail");
        }
    }

}