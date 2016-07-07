package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 回滚记录表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
@RpcProvider
public class DoctorRevertLogReadServiceImpl implements DoctorRevertLogReadService {

    private final DoctorRevertLogDao doctorRevertLogDao;

    @Autowired
    public DoctorRevertLogReadServiceImpl(DoctorRevertLogDao doctorRevertLogDao) {
        this.doctorRevertLogDao = doctorRevertLogDao;
    }

    @Override
    public Response<DoctorRevertLog> findRevertLogById(Long revertLogId) {
        try {
            return Response.ok(doctorRevertLogDao.findById(revertLogId));
        } catch (Exception e) {
            log.error("find revertLog by id failed, revertLogId:{}, cause:{}", revertLogId, Throwables.getStackTraceAsString(e));
            return Response.fail("revertLog.find.fail");
        }
    }

}
