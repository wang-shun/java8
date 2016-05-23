package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.model.DoctorChangeReason;
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
public class DoctorChangeReasonWriteServiceImpl implements DoctorChangeReasonWriteService {

    private final DoctorChangeReasonDao doctorChangeReasonDao;

    @Autowired
    public DoctorChangeReasonWriteServiceImpl(DoctorChangeReasonDao doctorChangeReasonDao) {
        this.doctorChangeReasonDao = doctorChangeReasonDao;
    }

    @Override
    public Response<Long> createChangeReason(DoctorChangeReason changeReason) {
        try {
            doctorChangeReasonDao.create(changeReason);
            return Response.ok(changeReason.getId());
        } catch (Exception e) {
            log.error("create changeReason failed, changeReason:{}, cause:{}", changeReason, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateChangeReason(DoctorChangeReason changeReason) {
        try {
            return Response.ok(doctorChangeReasonDao.update(changeReason));
        } catch (Exception e) {
            log.error("update changeReason failed, changeReason:{}, cause:{}", changeReason, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteChangeReasonById(Long changeReasonId) {
        try {
            return Response.ok(doctorChangeReasonDao.delete(changeReasonId));
        } catch (Exception e) {
            log.error("delete changeReason failed, changeReasonId:{}, cause:{}", changeReasonId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.delete.fail");
        }
    }
}
