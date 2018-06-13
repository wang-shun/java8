package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorOrgsLogsDao;
import io.terminus.doctor.user.model.DoctorOrgsLogs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-06-13 19:41:03
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorOrgsLogsWriteServiceImpl implements DoctorOrgsLogsWriteService {

    @Autowired
    private DoctorOrgsLogsDao doctorOrgsLogsDao;

    @Override
    public Response<Long> create(DoctorOrgsLogs doctorOrgsLogs) {
        try{
            doctorOrgsLogsDao.create(doctorOrgsLogs);
            return Response.ok(doctorOrgsLogs.getId());
        }catch (Exception e){
            log.error("failed to create doctor orgs logs, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.orgs.logs.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorOrgsLogs doctorOrgsLogs) {
        try{
            return Response.ok(doctorOrgsLogsDao.update(doctorOrgsLogs));
        }catch (Exception e){
            log.error("failed to update doctor orgs logs, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.orgs.logs.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorOrgsLogsDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor orgs logs by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.orgs.logs.delete.fail");
        }
    }

}