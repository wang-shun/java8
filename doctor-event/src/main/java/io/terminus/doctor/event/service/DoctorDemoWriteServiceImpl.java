package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorDemoDao;
import io.terminus.doctor.event.model.DoctorDemo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-29 10:49:19
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDemoWriteServiceImpl implements DoctorDemoWriteService {

    @Autowired
    private DoctorDemoDao doctorDemoDao;

    @Override
    public Response<Long> create(DoctorDemo doctorDemo) {
        try{
            doctorDemoDao.create(doctorDemo);
            return Response.ok(doctorDemo.getId());
        }catch (Exception e){
            log.error("failed to create doctor demo, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.demo.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorDemo doctorDemo) {
        try{
            return Response.ok(doctorDemoDao.update(doctorDemo));
        }catch (Exception e){
            log.error("failed to update doctor demo, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.demo.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorDemoDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor demo by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.demo.delete.fail");
        }
    }

}