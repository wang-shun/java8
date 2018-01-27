package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorReportFieldsDao;
import io.terminus.doctor.basic.model.DoctorReportFields;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 16:19:39
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorReportFieldsWriteServiceImpl implements DoctorReportFieldsWriteService {

    @Autowired
    private DoctorReportFieldsDao doctorReportFieldsDao;

    @Override
    public Response<Long> create(DoctorReportFields doctorReportFields) {
        try{
            doctorReportFieldsDao.create(doctorReportFields);
            return Response.ok(doctorReportFields.getId());
        }catch (Exception e){
            log.error("failed to create doctor report fields, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.fields.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorReportFields doctorReportFields) {
        try{
            return Response.ok(doctorReportFieldsDao.update(doctorReportFields));
        }catch (Exception e){
            log.error("failed to update doctor report fields, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.fields.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorReportFieldsDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor report fields by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.fields.delete.fail");
        }
    }

}