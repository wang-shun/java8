package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBarnDao;
import io.terminus.doctor.basic.model.DoctorBarn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪舍表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorBarnWriteServiceImpl implements DoctorBarnWriteService {

    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorBarnWriteServiceImpl(DoctorBarnDao doctorBarnDao) {
        this.doctorBarnDao = doctorBarnDao;
    }

    @Override
    public Response<Long> createBarn(DoctorBarn barn) {
        try {
            doctorBarnDao.create(barn);
            return Response.ok(barn.getId());
        } catch (Exception e) {
            log.error("create barn failed, barn:{}, cause:{}", barn, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateBarn(DoctorBarn barn) {
        try {
            return Response.ok(doctorBarnDao.update(barn));
        } catch (Exception e) {
            log.error("update barn failed, barn:{}, cause:{}", barn, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteBarnById(Long barnId) {
        try {
            return Response.ok(doctorBarnDao.delete(barnId));
        } catch (Exception e) {
            log.error("delete barn failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.delete.fail");
        }
    }
}
