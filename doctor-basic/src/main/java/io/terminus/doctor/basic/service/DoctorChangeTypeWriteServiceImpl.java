package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorChangeTypeDao;
import io.terminus.doctor.basic.model.DoctorChangeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 变动类型表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorChangeTypeWriteServiceImpl implements DoctorChangeTypeWriteService {

    private final DoctorChangeTypeDao doctorChangeTypeDao;

    @Autowired
    public DoctorChangeTypeWriteServiceImpl(DoctorChangeTypeDao doctorChangeTypeDao) {
        this.doctorChangeTypeDao = doctorChangeTypeDao;
    }

    @Override
    public Response<Long> createChangeType(DoctorChangeType changeType) {
        try {
            doctorChangeTypeDao.create(changeType);
            return Response.ok(changeType.getId());
        } catch (Exception e) {
            log.error("create changeType failed, changeType:{}, cause:{}", changeType, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateChangeType(DoctorChangeType changeType) {
        try {
            return Response.ok(doctorChangeTypeDao.update(changeType));
        } catch (Exception e) {
            log.error("update changeType failed, changeType:{}, cause:{}", changeType, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteChangeTypeById(Long changeTypeId) {
        try {
            return Response.ok(doctorChangeTypeDao.delete(changeTypeId));
        } catch (Exception e) {
            log.error("delete changeType failed, changeTypeId:{}, cause:{}", changeTypeId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.delete.fail");
        }
    }
}
