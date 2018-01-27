package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.model.DoctorPigDaily;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-12 17:33:52
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorPigDailyWriteServiceImpl implements DoctorPigDailyWriteService {

    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;

    @Override
    public Response<Long> create(DoctorPigDaily doctorPigDaily) {
        try{
            doctorPigDailyDao.create(doctorPigDaily);
            return Response.ok(doctorPigDaily.getId());
        }catch (Exception e){
            log.error("failed to create doctor pig daily, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.pig.daily.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorPigDaily doctorPigDaily) {
        try{
            return Response.ok(doctorPigDailyDao.update(doctorPigDaily));
        }catch (Exception e){
            log.error("failed to update doctor pig daily, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.pig.daily.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorPigDailyDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor pig daily by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.pig.daily.delete.fail");
        }
    }

}