package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.handler.rollback.DoctorRollbackHandlerChain;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
@Service
@RpcProvider
public class DoctorRollbackServiceImpl implements DoctorRollbackService {

    private final DoctorRollbackHandlerChain doctorRollbackHandlerChain;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorPigEventDao doctorPigEventDao;

    @Autowired
    public DoctorRollbackServiceImpl(DoctorRollbackHandlerChain doctorRollbackHandlerChain,
                                     DoctorGroupEventDao doctorGroupEventDao,
                                     DoctorPigEventDao doctorPigEventDao) {
        this.doctorRollbackHandlerChain = doctorRollbackHandlerChain;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorPigEventDao = doctorPigEventDao;
    }

    @Override
    public Response<Boolean> rollbackGroupEvent(Long eventId) {
        try {
            DoctorGroupEvent groupEvent = doctorGroupEventDao.findById(eventId);
            if (groupEvent != null) {
                //获取拦截器链, 执行回滚操作, 如果成功, 更新报表
                doctorRollbackHandlerChain.getRollbackGroupEventHandlers().stream()
                        .filter(handler -> handler.canRollback(groupEvent))
                        .filter(handler -> handler.rollback(groupEvent))
                        .forEach(handler -> handler.updateReport(groupEvent));
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("rollack group event failed, eventId:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return Response.fail("rollback.event.failed");
        }
    }

    @Override
    public Response<Boolean> rollbackPigEvent(Long eventId) {
        try {
            DoctorPigEvent pigEvent = doctorPigEventDao.findById(eventId);
            if (pigEvent != null) {
                //获取拦截器链, 执行回滚操作, 如果成功, 更新报表
                doctorRollbackHandlerChain.getRollbackPigEventHandlers().stream()
                        .filter(handler -> handler.canRollback(pigEvent))
                        .filter(handler -> handler.rollback(pigEvent))
                        .forEach(handler -> handler.updateReport(pigEvent));
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("rollack pig event failed, eventId:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return Response.fail("rollback.event.failed");
        }
    }
}
