package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBiPagesDao;
import io.terminus.doctor.basic.model.DoctorBiPages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-05 13:13:41
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorBiPagesWriteServiceImpl implements DoctorBiPagesWriteService {

    @Autowired
    private DoctorBiPagesDao doctorBiPagesDao;

    @Override
    public Response<Long> create(DoctorBiPages doctorBiPages) {
        try{
            doctorBiPagesDao.create(doctorBiPages);
            return Response.ok(doctorBiPages.getId());
        }catch (Exception e){
            log.error("failed to create doctor bi pages, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.bi.pages.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorBiPages doctorBiPages) {
        try{
            return Response.ok(doctorBiPagesDao.update(doctorBiPages));
        }catch (Exception e){
            log.error("failed to update doctor bi pages, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.bi.pages.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorBiPagesDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor bi pages by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.bi.pages.delete.fail");
        }
    }

}