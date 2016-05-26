package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群事件表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupEventWriteServiceImpl implements DoctorGroupEventWriteService {

    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorGroupEventWriteServiceImpl(DoctorGroupEventDao doctorGroupEventDao) {
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    public Response<Long> createGroupEvent(DoctorGroupEvent groupEvent) {
        try {
            doctorGroupEventDao.create(groupEvent);
            return Response.ok(groupEvent.getId());
        } catch (Exception e) {
            log.error("create groupEvent failed, groupEvent:{}, cause:{}", groupEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateGroupEvent(DoctorGroupEvent groupEvent) {
        try {
            return Response.ok(doctorGroupEventDao.update(groupEvent));
        } catch (Exception e) {
            log.error("update groupEvent failed, groupEvent:{}, cause:{}", groupEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteGroupEventById(Long groupEventId) {
        try {
            return Response.ok(doctorGroupEventDao.delete(groupEventId));
        } catch (Exception e) {
            log.error("delete groupEvent failed, groupEventId:{}, cause:{}", groupEventId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.delete.fail");
        }
    }
}
