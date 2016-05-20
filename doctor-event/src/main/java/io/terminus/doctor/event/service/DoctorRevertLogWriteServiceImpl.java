package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 回滚记录表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorRevertLogWriteServiceImpl implements DoctorRevertLogWriteService {

    private final DoctorRevertLogDao doctorRevertLogDao;

    @Autowired
    public DoctorRevertLogWriteServiceImpl(DoctorRevertLogDao doctorRevertLogDao) {
        this.doctorRevertLogDao = doctorRevertLogDao;
    }

    @Override
    public Response<Long> createRevertLog(DoctorRevertLog revertLog) {
        try {
            doctorRevertLogDao.create(revertLog);
            return Response.ok(revertLog.getId());
        } catch (Exception e) {
            log.error("create revertLog failed, revertLog:{}, cause:{}", revertLog, Throwables.getStackTraceAsString(e));
            return Response.fail("revertLog.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateRevertLog(DoctorRevertLog revertLog) {
        try {
            return Response.ok(doctorRevertLogDao.update(revertLog));
        } catch (Exception e) {
            log.error("update revertLog failed, revertLog:{}, cause:{}", revertLog, Throwables.getStackTraceAsString(e));
            return Response.fail("revertLog.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteRevertLogById(Long revertLogId) {
        try {
            return Response.ok(doctorRevertLogDao.delete(revertLogId));
        } catch (Exception e) {
            log.error("delete revertLog failed, revertLogId:{}, cause:{}", revertLogId, Throwables.getStackTraceAsString(e));
            return Response.fail("revertLog.delete.fail");
        }
    }
}
